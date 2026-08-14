package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.zbs.de.model.PriceVersion;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryPriceVersion;
import com.zbs.de.util.UtilTransaction;

/**
 * Proves that a service method which catches its own exception does not commit
 * the writes it managed to make before failing.
 *
 * <p>
 * This is the fault {@link UtilTransaction} exists to close, and it is not
 * visible in any single method's source. The service layer is written so that a
 * method does several writes inside one {@code try} and its {@code catch}
 * returns a {@code DtoResult} carrying an error message. That reads as handled.
 * But Spring rolls back only on an exception that <em>propagates out</em> of the
 * transactional method, and a caught one does not propagate — so the transaction
 * commits, every write made before the failure becomes permanent, and the
 * response tells the caller the operation failed.
 *
 * <p>
 * The two are consistent with each other and both individually correct. Only
 * running the pair against a real transaction shows the disagreement, which is
 * why this is an integration test and not a unit test with a mocked repository:
 * a mock has no transaction, so a mocked version of this test passes whether the
 * fix is present or not.
 *
 * <p>
 * {@code setAsDefault} is the case used here because its invariant is the
 * plainest in the codebase — <em>there is at most one default price version</em>
 * — and because breaking it is silent. Pricing does not error; it returns
 * nothing, and the customer sees a blank total.
 *
 * <p>
 * Skips itself where there is no database, like the other integration tests.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class TransactionRollbackIT {

	@Autowired
	private ServicePriceVersion servicePriceVersion;

	/*
	 * A spy, not a mock: every method behaves normally except the one save this
	 * test makes fail. Mocking the repository outright would mean the service
	 * never touches the database, and the database is the whole point.
	 */
	@MockitoSpyBean
	private RepositoryPriceVersion repository;

	private Long originalDefaultId;
	private Long challengerId;

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
	void seedTwoVersions() {
		originalDefaultId = save("IT-DEFAULT", true).getSerPriceVersionId();
		challengerId = save("IT-CHALLENGER", false).getSerPriceVersionId();
	}

	@AfterEach
	void removeSeed() {
		repository.findAll().stream()
				.filter(v -> v.getTxtVersionCode() != null && v.getTxtVersionCode().startsWith("IT-"))
				.forEach(repository::delete);
	}

	private PriceVersion save(String code, boolean isDefault) {
		PriceVersion version = new PriceVersion();
		version.setTxtVersionCode(code);
		version.setTxtName(code);
		version.setBlnIsDefault(isDefault);
		version.setBlnIsActive(true);
		version.setBlnIsDeleted(false);
		return repository.saveAndFlush(version);
	}

	private List<PriceVersion> seededVersions() {
		return repository.findAll().stream()
				.filter(v -> v.getTxtVersionCode() != null && v.getTxtVersionCode().startsWith("IT-"))
				.toList();
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("a failed setAsDefault leaves the existing default in place")
	void failedSetAsDefaultDoesNotClearTheExistingDefault() {
		/*
		 * setAsDefault clears the flag on the current default and then sets it
		 * on the new one. Failing the second save is precisely the sequence
		 * that used to leave the menu with no default at all.
		 */
		doThrow(new RuntimeException("simulated failure setting the new default"))
				.when(repository).save(argThat(
						(PriceVersion v) -> v != null && challengerId.equals(v.getSerPriceVersionId())));

		DtoResult result = servicePriceVersion.setAsDefault(challengerId);

		// The caller is still told it failed. Rolling back must not change that.
		assertThat(result.getTxtMessage()).contains("Failed to set price version as default");

		List<PriceVersion> versions = seededVersions();
		assertThat(versions)
				.as("the clear of the old default must have been rolled back with the failed set")
				.filteredOn(v -> Boolean.TRUE.equals(v.getBlnIsDefault()))
				.extracting(PriceVersion::getSerPriceVersionId)
				.containsExactly(originalDefaultId);
	}

	@Test
	@DisplayName("a successful setAsDefault moves the default, leaving exactly one")
	void successfulSetAsDefaultMovesTheDefault() {
		DtoResult result = servicePriceVersion.setAsDefault(challengerId);

		assertThat(result.getTxtMessage()).contains("successfully");
		assertThat(seededVersions())
				.filteredOn(v -> Boolean.TRUE.equals(v.getBlnIsDefault()))
				.extracting(PriceVersion::getSerPriceVersionId)
				.containsExactly(challengerId);
	}

	@Test
	@DisplayName("markRollbackOnly is harmless when nothing is running a transaction")
	void markRollbackOnlyOutsideATransactionDoesNotThrow() {
		/*
		 * The helper is called from catch blocks in methods that are also
		 * reachable from paths that were never transactional. A helper whose
		 * job is to make failure handling correct must not itself become a
		 * second failure that masks the first.
		 */
		UtilTransaction.markRollbackOnly();
	}
}
