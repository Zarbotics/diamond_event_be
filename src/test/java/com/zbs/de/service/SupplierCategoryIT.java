package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.controller.ControllerExternalSupplierCategory;
import com.zbs.de.model.ExternalSupplierCategory;
import com.zbs.de.model.dto.DtoExternalSupplierCategory;
import com.zbs.de.repository.RepositoryExternalSupplierCategory;
import com.zbs.de.util.ResponseMessage;

/**
 * The kinds of supplier a customer may declare.
 *
 * <h2>What these are written from</h2>
 *
 * {@code event_external_supplier.txt_supplier_type} was free text. Free text
 * gives you "DJ", "dj", "Disc Jockey", "Music" and "dj + sound" as five
 * different things, so nothing can be counted, filtered or planned from the
 * kind of supplier it is — which is exactly what the office wants to do with
 * it: every photographer arriving on Saturday, different access instructions
 * for the cake maker than for the mehndi artist.
 *
 * <h2>What is worth pinning</h2>
 *
 * Two things, and they are the two that would quietly cost the business
 * something.
 *
 * <p>
 * A retired category must keep its bookings. A supplier declared under one is
 * a fact about a day that is going to happen, and a photographer vanishing
 * from next Saturday's run sheet because somebody tidied a lookup list is the
 * failure this table exists to avoid.
 *
 * <p>
 * And a duplicate name must be refused as a sentence. The unique index is on
 * {@code LOWER(txt_name)}, so a save that did not check would reach the office
 * as a constraint violation naming {@code ux_external_supplier_category_name}
 * rather than as something they can act on.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class SupplierCategoryIT {

	/** Distinctive enough that nothing else in the suite collides with it. */
	private static final String MARKER = "IT-CATEGORY";

	@Autowired
	private ControllerExternalSupplierCategory controller;

	@Autowired
	private RepositoryExternalSupplierCategory repository;

	@BeforeEach
	@AfterEach
	void removeOurRows() {
		List<ExternalSupplierCategory> ours = repository.findAll().stream()
				.filter(c -> c.getTxtName() != null && c.getTxtName().contains(MARKER))
				.toList();
		repository.deleteAll(ours);
	}

	@Test
	@DisplayName("the journey is offered active categories, in order, and not the retired ones")
	void offersActiveCategoriesInOrder() {
		save(MARKER + " Zebra handler", 20, true);
		save(MARKER + " Apiarist", 10, true);
		ExternalSupplierCategory retired = save(MARKER + " Town crier", 15, true);

		retired.setBlnIsActive(false);
		repository.save(retired);

		List<String> offered = namesOf(controller.offered());

		assertThat(offered).containsSubsequence(MARKER + " Apiarist", MARKER + " Zebra handler");
		assertThat(offered).doesNotContain(MARKER + " Town crier");
	}

	/**
	 * Null display order sorts last, not first.
	 *
	 * <p>
	 * In PostgreSQL a null sorts first in an ascending order, so a category
	 * added without one would arrive at the top of the customer's dropdown —
	 * which is precisely where "Other" must not be.
	 */
	@Test
	@DisplayName("a category with no display order goes to the end of the list")
	void unorderedCategoriesSortLast() {
		save(MARKER + " Other", null, true);
		save(MARKER + " Aardvark wrangler", 900000, true);

		List<String> offered = namesOf(controller.offered()).stream()
				.filter(name -> name.contains(MARKER))
				.toList();

		assertThat(offered).containsExactly(MARKER + " Aardvark wrangler", MARKER + " Other");
	}

	@Test
	@DisplayName("a duplicate name is refused as a sentence, ignoring case")
	void refusesDuplicateNames() {
		save(MARKER + " Falconer", 10, true);

		DtoExternalSupplierCategory clash = new DtoExternalSupplierCategory();
		clash.setTxtName(MARKER.toLowerCase() + " falconer");

		ResponseMessage response = controller.save(clash);

		assertThat(response.getCode()).isEqualTo(HttpStatus.CONFLICT.value());
		assertThat(response.getMessage()).contains("already a category called");
		// Not a constraint violation leaking the index name at the office.
		assertThat(response.getMessage()).doesNotContain("ux_external_supplier_category_name");
	}

	@Test
	@DisplayName("renaming a category to its own name is not a clash with itself")
	void renamingToItsOwnNameIsAllowed() {
		ExternalSupplierCategory existing = save(MARKER + " Calligrapher", 10, true);

		DtoExternalSupplierCategory same = new DtoExternalSupplierCategory();
		same.setSerSupplierCategoryId(existing.getSerSupplierCategoryId());
		same.setTxtName(MARKER + " Calligrapher");
		same.setTxtDescription("Now with an explanation.");

		ResponseMessage response = controller.save(same);

		assertThat(response.getCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(repository.findById(existing.getSerSupplierCategoryId()))
				.get()
				.extracting(ExternalSupplierCategory::getTxtDescription)
				.isEqualTo("Now with an explanation.");
	}

	/**
	 * The one that would cost the business a supplier on the day.
	 *
	 * <p>
	 * Deleting must retire, never remove. A hard delete would either orphan
	 * every supplier declared under the category or, with a cascade, take them
	 * with it.
	 */
	@Test
	@DisplayName("deleting a category retires it and leaves the row in place")
	void deletingRetiresRatherThanRemoves() {
		ExternalSupplierCategory doomed = save(MARKER + " Ice sculptor", 10, true);
		Long id = doomed.getSerSupplierCategoryId();

		DtoExternalSupplierCategory request = new DtoExternalSupplierCategory();
		request.setSerSupplierCategoryId(id);

		ResponseMessage response = controller.delete(request);

		assertThat(response.getCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(repository.findById(id))
				.as("the row must survive, because bookings point at it")
				.isPresent()
				.get()
				.satisfies(category -> {
					assertThat(category.getBlnIsActive()).isFalse();
					assertThat(category.getBlnIsDeleted()).isTrue();
				});

		assertThat(namesOf(controller.offered())).doesNotContain(MARKER + " Ice sculptor");
	}

	@Test
	@DisplayName("a category needs a name")
	void refusesABlankName() {
		DtoExternalSupplierCategory blank = new DtoExternalSupplierCategory();
		blank.setTxtName("   ");

		assertThat(controller.save(blank).getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	// ── helpers ──────────────────────────────────────────────────────────

	private ExternalSupplierCategory save(String name, Integer order, boolean active) {
		ExternalSupplierCategory category = new ExternalSupplierCategory();
		category.setTxtName(name);
		category.setNumDisplayOrder(order);
		category.setBlnIsActive(active);
		category.setBlnIsDeleted(false);
		category.setCreatedDate(new Date());
		return repository.save(category);
	}

	@SuppressWarnings("unchecked")
	private static List<String> namesOf(ResponseMessage response) {
		return ((List<DtoExternalSupplierCategory>) response.getResult()).stream()
				.map(DtoExternalSupplierCategory::getTxtName)
				.toList();
	}
}
