package com.zbs.de.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A menu posted without prices does not lose the booking.
 *
 * <h2>What happened</h2>
 *
 * {@code event_menu_category_selection} and
 * {@code event_menu_sub_category_selection} both hold {@code num_total_price}
 * and {@code num_final_price} as {@code NOT NULL}, and both were written
 * straight from the payload. A client that left either out — the customer
 * journey posting a menu it has not priced, an integration sending only the
 * dishes, a test fixture — did not get a booking priced at zero. It got a
 * constraint violation, and because the whole save is one transaction, the
 * rollback took the customer, the date, the venue, the running order and every
 * other choice with it.
 *
 * <p>
 * The mapping code even knew the figure could be absent: it guarded the running
 * total with {@code if (getNumFinalPrice() != null)} and then wrote the null
 * into the column anyway.
 *
 * <h2>Why zero rather than a refusal</h2>
 *
 * The selection is the part the business cannot replace. The price is
 * recomputed by the pricing engine on every save from the dish rows — which are
 * nullable, precisely because a price can legitimately be unknown — so a
 * defaulted figure here is not one anybody quotes from.
 *
 * <h2>Why the charge falls back to the listed price</h2>
 *
 * Because an empty price box means "charge the usual" everywhere else in the
 * portal; that is what the field's own placeholder says. Defaulting the charge
 * to zero instead would quietly give the category away.
 */
class MenuSelectionPricesAreNeverNullTest {

	@Test
	@DisplayName("a category posted with no prices at all is recorded at zero, not refused")
	void categoryWithNothing() {
		EventMenuCategorySelection selection = new EventMenuCategorySelection();

		selection.priceAs(null, null);

		assertThat(selection.getNumTotalPrice()).isEqualByComparingTo("0.00");
		assertThat(selection.getNumFinalPrice()).isEqualByComparingTo("0.00");
	}

	@Test
	@DisplayName("a category with a listed price and no override is charged the listed price")
	void categoryFallsBackToListed() {
		EventMenuCategorySelection selection = new EventMenuCategorySelection();

		selection.priceAs(new BigDecimal("450.00"), null);

		assertThat(selection.getNumTotalPrice()).isEqualByComparingTo("450.00");
		assertThat(selection.getNumFinalPrice())
				.as("an empty price box means charge the usual, not charge nothing")
				.isEqualByComparingTo("450.00");
	}

	@Test
	@DisplayName("an override is charged instead of the listed price")
	void categoryKeepsItsOverride() {
		EventMenuCategorySelection selection = new EventMenuCategorySelection();

		selection.priceAs(new BigDecimal("450.00"), new BigDecimal("399.00"));

		assertThat(selection.getNumTotalPrice()).isEqualByComparingTo("450.00");
		assertThat(selection.getNumFinalPrice()).isEqualByComparingTo("399.00");
	}

	/**
	 * A deliberate zero is not the same as an absent figure, and the office is
	 * allowed to give a category away. Nothing may turn that back into the
	 * listed price.
	 */
	@Test
	@DisplayName("a category deliberately charged at nothing stays at nothing")
	void categoryMayBeGivenAway() {
		EventMenuCategorySelection selection = new EventMenuCategorySelection();

		selection.priceAs(new BigDecimal("450.00"), BigDecimal.ZERO);

		assertThat(selection.getNumFinalPrice()).isEqualByComparingTo("0.00");
	}

	@Test
	@DisplayName("the same rules hold for a course beneath the category")
	void subCategoryBehavesTheSame() {
		EventMenuSubCategorySelection starters = new EventMenuSubCategorySelection();
		EventMenuSubCategorySelection desserts = new EventMenuSubCategorySelection();

		starters.priceAs(null, null);
		desserts.priceAs(new BigDecimal("120.00"), null);

		assertThat(starters.getNumTotalPrice()).isEqualByComparingTo("0.00");
		assertThat(starters.getNumFinalPrice()).isEqualByComparingTo("0.00");
		assertThat(desserts.getNumFinalPrice()).isEqualByComparingTo("120.00");
	}

	/**
	 * The setters are the backstop. `priceAs` is what the save paths call, but
	 * a column that cannot be null should not depend on every future caller
	 * remembering which method to use.
	 */
	@Test
	@DisplayName("no setter can leave a NOT NULL price column null")
	void settersRefuseToLeaveNull() {
		EventMenuCategorySelection category = new EventMenuCategorySelection();
		EventMenuSubCategorySelection course = new EventMenuSubCategorySelection();

		category.setNumTotalPrice(null);
		category.setNumFinalPrice(null);
		course.setNumTotalPrice(null);
		course.setNumFinalPrice(null);

		assertThat(category.getNumTotalPrice()).isNotNull();
		assertThat(category.getNumFinalPrice()).isNotNull();
		assertThat(course.getNumTotalPrice()).isNotNull();
		assertThat(course.getNumFinalPrice()).isNotNull();
	}
}
