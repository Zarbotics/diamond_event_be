package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.dto.DtoCustomerMaster;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.repository.RepositoryCustomerMaster;

/**
 * The customer list must not return the whole table.
 *
 * <p>
 * {@code getAllData} does, and the admin's customer table called it to render
 * ten rows — every visit transferred and mapped every customer the business
 * has ever had, then threw away all but a page of it in the browser. That is
 * invisible at fifty customers and fatal well before the number a growing
 * business wants to reach, which is the whole difficulty with it: nothing goes
 * wrong until a lot goes wrong at once.
 *
 * <p>
 * The cap is the part worth testing hardest. Paging that a client can opt out
 * of by asking for a million rows is not paging.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class CustomerPaginationIT {

	private static final int SEEDED = 45;
	private static final String PREFIX = "PAGING-IT-";

	@Autowired
	private ServiceCustomerMaster serviceCustomerMaster;

	@Autowired
	private RepositoryCustomerMaster repository;

	@BeforeAll
	static void requireDatabase() {
		String url = System.getenv().getOrDefault("TEST_DB_URL",
				"jdbc:postgresql://localhost:5432/diamond_ev_test");
		String user = System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres");
		String password = System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres");

		try (Connection ignored = DriverManager.getConnection(url, user, password)) {
			// reachable
		} catch (Exception e) {
			Assumptions.abort("No test database at " + url + " — skipping. (" + e.getMessage() + ")");
		}
	}

	@BeforeEach
	void seedCustomers() {
		removeSeed();
		for (int i = 0; i < SEEDED; i++) {
			CustomerMaster customer = new CustomerMaster();
			customer.setTxtCustCode(PREFIX + String.format("%03d", i));
			customer.setTxtCustName(PREFIX + "Customer " + i);
			customer.setTxtEmail("paging-it-" + i + "@example.com");
			customer.setBlnIsDeleted(false);
			customer.setBlnIsActive(true);
			repository.save(customer);
		}
		repository.flush();
	}

	@AfterEach
	void removeSeed() {
		List<CustomerMaster> seeded = repository.findByBlnIsDeleted(false).stream()
				.filter(c -> c.getTxtCustCode() != null && c.getTxtCustCode().startsWith(PREFIX))
				.toList();
		repository.deleteAll(seeded);
		repository.flush();
	}

	private DtoSearch request(Integer page, Integer size, String term) {
		DtoSearch criteria = new DtoSearch();
		criteria.setPageNumber(page);
		criteria.setPageSize(size);
		criteria.setSearchKeyword(term);
		return criteria;
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("a page holds only the rows asked for")
	void aPageIsOnePage() {
		Page<DtoCustomerMaster> page = serviceCustomerMaster.search(request(0, 10, PREFIX));

		assertThat(page.getContent()).hasSize(10);
		assertThat(page.getTotalElements()).isEqualTo(SEEDED);
		assertThat(page.getTotalPages()).isEqualTo(5);
	}

	@Test
	@DisplayName("asking for a later page returns different customers")
	void pagesDoNotOverlap() {
		List<Integer> first = serviceCustomerMaster.search(request(0, 10, PREFIX))
				.map(DtoCustomerMaster::getSerCustId).getContent();
		List<Integer> second = serviceCustomerMaster.search(request(1, 10, PREFIX))
				.map(DtoCustomerMaster::getSerCustId).getContent();

		assertThat(second).hasSize(10).doesNotContainAnyElementsOf(first);
	}

	@Test
	@DisplayName("an oversized request is capped, not obeyed")
	void theSizeIsCapped() {
		/*
		 * The assertion that makes this paging rather than a suggestion. Without
		 * the cap a client can ask for everything and the endpoint is exactly as
		 * unbounded as the one it replaced — only now it looks paginated.
		 */
		Page<DtoCustomerMaster> page = serviceCustomerMaster.search(request(0, 1_000_000, PREFIX));

		assertThat(page.getSize())
				.as("a request for a million rows must not be honoured")
				.isEqualTo(250);
	}

	@Test
	@DisplayName("no paging details means a sensible default, not everything")
	void thereIsADefault() {
		Page<DtoCustomerMaster> page = serviceCustomerMaster.search(new DtoSearch());

		assertThat(page.getSize()).isEqualTo(20);
	}

	@Test
	@DisplayName("the search term narrows by the things a person searches by")
	void theTermMatchesNameCodeAndEmail() {
		assertThat(serviceCustomerMaster.search(request(0, 50, PREFIX + "Customer 7")).getContent())
				.as("by name").hasSize(1);
		assertThat(serviceCustomerMaster.search(request(0, 50, PREFIX + "007")).getContent())
				.as("by code").hasSize(1);
		assertThat(serviceCustomerMaster.search(request(0, 50, "paging-it-7@example.com")).getContent())
				.as("by email").hasSize(1);
	}

	@Test
	@DisplayName("an empty term does not filter everything out")
	void anEmptyTermReturnsEveryone() {
		assertThat(serviceCustomerMaster.search(request(0, 250, "")).getTotalElements())
				.isGreaterThanOrEqualTo(SEEDED);
		assertThat(serviceCustomerMaster.search(request(0, 250, null)).getTotalElements())
				.isGreaterThanOrEqualTo(SEEDED);
	}

	@Test
	@DisplayName("a deleted customer stays out of the list")
	void deletedCustomersAreExcluded() {
		CustomerMaster victim = repository.findByBlnIsDeleted(false).stream()
				.filter(c -> c.getTxtCustCode() != null && c.getTxtCustCode().startsWith(PREFIX))
				.findFirst().orElseThrow();
		victim.setBlnIsDeleted(true);
		repository.saveAndFlush(victim);

		assertThat(serviceCustomerMaster.search(request(0, 250, PREFIX)).getTotalElements())
				.isEqualTo(SEEDED - 1);
	}
}
