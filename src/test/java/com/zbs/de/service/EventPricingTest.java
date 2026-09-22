package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.EventDecorPropertySelection;
import com.zbs.de.model.DecorExtrasMaster;
import com.zbs.de.model.EventDecorCategorySelection;
import com.zbs.de.model.EventDecorExtrasSelection;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.MenuItem;
import com.zbs.de.repository.RepositoryEventPriceLine;
import com.zbs.de.service.impl.ServiceAppSettings;
import com.zbs.de.service.impl.ServiceEventPricing;
import com.zbs.de.util.enums.EnmPriceMultiplierType;

/**
 * What a booking costs.
 *
 * <h2>Why this is tested this closely</h2>
 *
 * It is money. Every other calculation in this codebase can be wrong and
 * somebody notices; a pricing rule that is wrong by a few pence per head is
 * invisible on one booking and material across a year of them, and the person
 * who eventually finds it will be a customer.
 *
 * <p>
 * So the figures here are written out in full rather than derived in the test.
 * A test that computes its own expectation with the same arithmetic as the code
 * proves only that the arithmetic is consistent with itself.
 *
 * <h2>What these were written against</h2>
 *
 * The behaviour the JavaScript had, because that is what the business is
 * running on today and the engine has to reproduce it before anybody would be
 * right to trust it. Where the engine deliberately differs — VAT per line
 * rather than on a lump, stations, tables — that is stated in the test that
 * covers it.
 */
@ExtendWith(MockitoExtension.class)
class EventPricingTest {

	@Mock
	private ServiceAppSettings serviceAppSettings;

	@Mock
	private RepositoryEventPriceLine repositoryEventPriceLine;

	@InjectMocks
	private ServiceEventPricing pricing;

	/** Today's rules: 20%, on décor and extras, and on nothing else. */
	private void todaysVatRules() {
		lenient().when(serviceAppSettings.getVatPercent()).thenReturn(new BigDecimal("20"));
		lenient().when(serviceAppSettings.doesDecorCategoryPriceReplaceProperties()).thenReturn(true);
		lenient().when(serviceAppSettings.isVatChargedOn(anyString())).thenReturn(false);
		lenient().when(serviceAppSettings.isVatChargedOn("DECOR")).thenReturn(true);
		lenient().when(serviceAppSettings.isVatChargedOn("DECOR_PROPERTY")).thenReturn(true);
		lenient().when(serviceAppSettings.isVatChargedOn("EXTRA")).thenReturn(true);
	}

	@Nested
	@DisplayName("how a dish is multiplied")
	class Multipliers {

		@Test
		@DisplayName("a dish priced per guest is charged for every guest")
		void perGuest() {
			todaysVatRules();
			EventMaster event = eventFor(250, 25);
			event.getFoodSelections().add(dishOn(event, "Chicken Karahi", "25.00",
					EnmPriceMultiplierType.PER_GUEST, null, null));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getFood()).isEqualByComparingTo("6250.00");
			assertThat(priced.getLines().get(0).getTxtReason())
					.isEqualTo("£25.00 per guest × 250 guests");
		}

		@Test
		@DisplayName("a dish priced per table is charged for every table")
		void perTable() {
			todaysVatRules();
			EventMaster event = eventFor(250, 25);
			event.getFoodSelections().add(dishOn(event, "Table centrepiece platter", "18.00",
					EnmPriceMultiplierType.PER_TABLE, null, null));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getFood()).isEqualByComparingTo("450.00");
			assertThat(priced.getLines().get(0).getTxtReason())
					.isEqualTo("£18.00 per table × 25 tables");
		}

		/**
		 * Stations round up, because half a grazing bar cannot be supplied.
		 *
		 * <p>
		 * 250 guests at one station per 80 is 3.125, which is four stations. A
		 * business that rounded that down would be sending three bars to feed
		 * 250 people.
		 */
		@Test
		@DisplayName("a station is charged for whole, never in fractions")
		void perStationRoundsUp() {
			todaysVatRules();
			EventMaster event = eventFor(250, 25);
			event.getFoodSelections().add(dishOn(event, "Grazing bar", "400.00",
					EnmPriceMultiplierType.PER_STATION, new BigDecimal("80"), null));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getFood()).isEqualByComparingTo("1600.00");
			assertThat(priced.getLines().get(0).getTxtReason())
					.isEqualTo("£400.00 per station × 4 (one per 80 guests)");
		}

		/**
		 * A station with no size is one station.
		 *
		 * <p>
		 * The safe reading. A business that has not said how many guests a bar
		 * serves has not said it needs two, and guessing upwards would quietly
		 * inflate every quote carrying one.
		 */
		@Test
		@DisplayName("a station with no stated size is charged once")
		void perStationWithoutASizeIsOne() {
			todaysVatRules();
			EventMaster event = eventFor(250, 25);
			event.getFoodSelections().add(dishOn(event, "Dessert table", "600.00",
					EnmPriceMultiplierType.PER_STATION, null, null));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getFood()).isEqualByComparingTo("600.00");
		}

		@Test
		@DisplayName("a flat price is the price, whoever comes")
		void flat() {
			todaysVatRules();
			EventMaster event = eventFor(250, 25);
			event.getFoodSelections().add(dishOn(event, "Cake cutting", "150.00",
					EnmPriceMultiplierType.FLAT, null, null));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getFood()).isEqualByComparingTo("150.00");
		}

		/**
		 * A dish that does not say how it is priced is charged per head.
		 *
		 * <p>
		 * Which is what the rest of the codebase already assumes. It is not a
		 * good state and it is logged elsewhere, but changing the assumption
		 * here would re-price every dish in the catalogue that has not been
		 * given a rule.
		 */
		@Test
		@DisplayName("a dish with no stated rule is charged per guest")
		void unstatedMeansPerGuest() {
			todaysVatRules();
			EventMaster event = eventFor(100, 10);
			event.getFoodSelections().add(dishOn(event, "Unstated", "10.00", null, null, null));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getFood()).isEqualByComparingTo("1000.00");
		}
	}

	@Nested
	@DisplayName("VAT, as the office has set it")
	class Vat {

		/**
		 * The rules as they are today, reproduced exactly.
		 *
		 * <p>
		 * £6,250 of food carries no VAT; £1,000 of décor carries £200. If this
		 * test ever changes, the business's invoices change with it.
		 */
		@Test
		@DisplayName("by default VAT falls on décor and extras and nothing else")
		void todaysRules() {
			todaysVatRules();
			EventMaster event = eventFor(250, 25);
			event.getFoodSelections().add(dishOn(event, "Chicken Karahi", "25.00",
					EnmPriceMultiplierType.PER_GUEST, null, null));
			event.getDecorSelections().add(decorOn(event, "Stage", "1000.00"));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getFood()).isEqualByComparingTo("6250.00");
			assertThat(priced.getDecor()).isEqualByComparingTo("1000.00");
			assertThat(priced.getVat())
					.as("VAT should be 20% of the décor alone")
					.isEqualByComparingTo("200.00");
			assertThat(priced.getTotal()).isEqualByComparingTo("7450.00");
		}

		/**
		 * The office switches VAT onto everything.
		 *
		 * <p>
		 * The case a standard-rated business wants, and the one that was
		 * impossible before: the rate was a constant and what it applied to was
		 * decided by which variable it happened to be multiplied by.
		 */
		@Test
		@DisplayName("VAT can be charged on the whole bill")
		void onEverything() {
			lenient().when(serviceAppSettings.getVatPercent()).thenReturn(new BigDecimal("20"));
			lenient().when(serviceAppSettings.doesDecorCategoryPriceReplaceProperties()).thenReturn(true);
			when(serviceAppSettings.isVatChargedOn(anyString())).thenReturn(true);

			EventMaster event = eventFor(250, 25);
			event.getFoodSelections().add(dishOn(event, "Chicken Karahi", "25.00",
					EnmPriceMultiplierType.PER_GUEST, null, null));
			event.getDecorSelections().add(decorOn(event, "Stage", "1000.00"));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getVat())
					.as("20% of £7,250")
					.isEqualByComparingTo("1450.00");
			assertThat(priced.getTotal()).isEqualByComparingTo("8700.00");
		}

		@Test
		@DisplayName("VAT can be switched off entirely")
		void none() {
			lenient().when(serviceAppSettings.getVatPercent()).thenReturn(new BigDecimal("20"));
			lenient().when(serviceAppSettings.doesDecorCategoryPriceReplaceProperties()).thenReturn(true);
			when(serviceAppSettings.isVatChargedOn(anyString())).thenReturn(false);

			EventMaster event = eventFor(250, 25);
			event.getDecorSelections().add(decorOn(event, "Stage", "1000.00"));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getVat()).isEqualByComparingTo("0.00");
			assertThat(priced.getTotal()).isEqualByComparingTo("1000.00");
		}

		/**
		 * Each line says whether it carried VAT.
		 *
		 * <p>
		 * The reason the total can be checked against a return. A single VAT
		 * figure on a bill where some parts are liable and some are not cannot
		 * be taken apart by anybody afterwards.
		 */
		@Test
		@DisplayName("each line records whether VAT fell on it")
		void perLineRecord() {
			todaysVatRules();
			EventMaster event = eventFor(100, 10);
			event.getFoodSelections().add(dishOn(event, "Biryani", "20.00",
					EnmPriceMultiplierType.PER_GUEST, null, null));
			event.getDecorSelections().add(decorOn(event, "Stage", "500.00"));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getLines())
					.filteredOn(line -> "FOOD".equals(line.getTxtSection()))
					.allSatisfy(line -> {
						assertThat(line.getBlnIsVatable()).isFalse();
						assertThat(line.getNumVat()).isEqualByComparingTo("0.00");
					});
			assertThat(priced.getLines())
					.filteredOn(line -> "DECOR".equals(line.getTxtSection()))
					.allSatisfy(line -> {
						assertThat(line.getBlnIsVatable()).isTrue();
						assertThat(line.getNumVat()).isEqualByComparingTo("100.00");
					});
		}

		@Test
		@DisplayName("the rate is whatever the office set")
		void rateIsConfigurable() {
			lenient().when(serviceAppSettings.doesDecorCategoryPriceReplaceProperties()).thenReturn(true);
			when(serviceAppSettings.getVatPercent()).thenReturn(new BigDecimal("5"));
			lenient().when(serviceAppSettings.isVatChargedOn(anyString())).thenReturn(false);
			when(serviceAppSettings.isVatChargedOn("DECOR")).thenReturn(true);

			EventMaster event = eventFor(100, 10);
			event.getDecorSelections().add(decorOn(event, "Stage", "1000.00"));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getVat()).isEqualByComparingTo("50.00");
		}
	}

	@Nested
	@DisplayName("what the office decided")
	class Overrides {

		/**
		 * A price a person set is kept, and said to have been set.
		 *
		 * <p>
		 * The engine must never quietly put a negotiated price back up to the
		 * catalogue figure on the next save of the same booking — which is
		 * exactly what a naive "recalculate everything" would do.
		 */
		@Test
		@DisplayName("a price agreed by the office survives, and is marked")
		void anOverrideIsKept() {
			todaysVatRules();
			EventMaster event = eventFor(100, 10);
			EventMenuFoodSelection selection = dishOn(event, "Lamb Biryani", "25.00",
					EnmPriceMultiplierType.PER_GUEST, null, new BigDecimal("2000.00"));
			event.getFoodSelections().add(selection);

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getFood())
					.as("the office agreed £2,000, not the catalogue's £2,500")
					.isEqualByComparingTo("2000.00");
			assertThat(priced.getLines().get(0).getBlnIsOverridden()).isTrue();
			assertThat(priced.getLines().get(0).getNumCatalogueTotal())
					.as("what the rules would have said, kept beside what was charged")
					.isEqualByComparingTo("2500.00");
		}

		@Test
		@DisplayName("a price that matches the rules is not called an override")
		void agreementIsNotAnOverride() {
			todaysVatRules();
			EventMaster event = eventFor(100, 10);
			event.getFoodSelections().add(dishOn(event, "Lamb Biryani", "25.00",
					EnmPriceMultiplierType.PER_GUEST, null, new BigDecimal("2500.00")));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getLines().get(0).getBlnIsOverridden()).isFalse();
		}
	}

	@Nested
	@DisplayName("décor and its options")
	class Decor {

		/**
		 * A priced category swallows its options.
		 *
		 * <p>
		 * Both carry prices, and charging both bills the customer twice for one
		 * stage. The options stay on the quote at zero rather than disappearing,
		 * because the customer chose them and needs to see they are included.
		 */
		@Test
		@DisplayName("options under a priced category are included, not charged again")
		void categoryPriceReplacesOptions() {
			todaysVatRules();
			EventMaster event = eventFor(100, 10);
			EventDecorCategorySelection stage = decorOn(event, "Stage", "1000.00");
			withOption(stage, "Ivory drape", "250.00");
			event.getDecorSelections().add(stage);

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getDecor())
					.as("the stage at £1,000, and its £250 drape included in that")
					.isEqualByComparingTo("1000.00");
		}

		/**
		 * The included option still appears on the quote.
		 *
		 * <p>
		 * At zero rather than hidden. The customer chose it and needs to see it
		 * is on the booking; a quote that silently drops a chosen option reads
		 * as though it was forgotten.
		 */
		@Test
		@DisplayName("an included option is listed at nothing rather than dropped")
		void includedOptionsAreStillListed() {
			todaysVatRules();
			EventMaster event = eventFor(100, 10);
			EventDecorCategorySelection stage = decorOn(event, "Stage", "1000.00");
			withOption(stage, "Ivory drape", "250.00");
			event.getDecorSelections().add(stage);

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getLines())
					.filteredOn(line -> "DECOR_PROPERTY".equals(line.getTxtSection()))
					.singleElement()
					.satisfies(line -> {
						assertThat(line.getTxtDescription()).isEqualTo("Stage — Ivory drape");
						assertThat(line.getNumLineTotal()).isEqualByComparingTo("0.00");
						assertThat(line.getTxtReason()).isEqualTo("Included in the price of Stage");
					});
		}

		/**
		 * An option under an unpriced category is charged on its own.
		 *
		 * <p>
		 * The other half of the rule. A category with no price of its own is a
		 * grouping rather than a product, and what the customer is buying is
		 * the options beneath it.
		 */
		@Test
		@DisplayName("options under an unpriced category are charged")
		void optionsAreChargedWhenTheCategoryIsNot() {
			todaysVatRules();
			EventMaster event = eventFor(100, 10);
			EventDecorCategorySelection stage = decorOn(event, "Stage", null);
			withOption(stage, "Ivory drape", "250.00");
			event.getDecorSelections().add(stage);

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getDecor()).isEqualByComparingTo("250.00");
		}

		@Test
		@DisplayName("a décor category with no price costs nothing")
		void unpricedDecor() {
			todaysVatRules();
			EventMaster event = eventFor(100, 10);
			event.getDecorSelections().add(decorOn(event, "Stage", null));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getDecor()).isEqualByComparingTo("0.00");
		}
	}

	@Nested
	@DisplayName("the bottom of the bill")
	class Totals {

		@Test
		@DisplayName("a discount comes off the whole bill, VAT included")
		void discount() {
			todaysVatRules();
			EventMaster event = eventFor(250, 25);
			event.getDecorSelections().add(decorOn(event, "Stage", "1000.00"));
			event.setNumDiscount(new BigDecimal("200.00"));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getSubtotal()).isEqualByComparingTo("1000.00");
			assertThat(priced.getVat()).isEqualByComparingTo("200.00");
			assertThat(priced.getTotal()).isEqualByComparingTo("1000.00");
		}

		/**
		 * A discount bigger than the bill does not invoice a negative amount.
		 *
		 * <p>
		 * It is a data-entry slip — a stray zero on a discount field — and the
		 * failure that costs money is the one nobody notices. Held at zero and
		 * logged.
		 */
		@Test
		@DisplayName("a discount larger than the bill holds the total at zero")
		void discountCannotGoNegative() {
			todaysVatRules();
			EventMaster event = eventFor(100, 10);
			event.getDecorSelections().add(decorOn(event, "Stage", "500.00"));
			event.setNumDiscount(new BigDecimal("9000.00"));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getTotal()).isEqualByComparingTo("0.00");
		}

		/**
		 * The two charges the office types straight onto the booking.
		 *
		 * <p>
		 * They were being left out of every server-side total because they are
		 * figures rather than selections. They are lines here so the office can
		 * put VAT on them like anything else.
		 */
		@Test
		@DisplayName("the itinerary and serving dish charges are on the bill")
		void flatChargesAreCounted() {
			todaysVatRules();
			EventMaster event = eventFor(100, 10);
			event.setNumItineraryPrice(new BigDecimal("75.00"));
			event.setNumServingDishesPrice(new BigDecimal("40.00"));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getSubtotal()).isEqualByComparingTo("115.00");
			assertThat(priced.getLines())
					.extracting(line -> line.getTxtSection())
					.contains("ITINERARY", "SERVING_DISHES");
		}

		@Test
		@DisplayName("a charge of nothing is not listed")
		void zeroFlatChargesAreNotListed() {
			todaysVatRules();
			EventMaster event = eventFor(100, 10);
			event.setNumItineraryPrice(BigDecimal.ZERO);

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getLines())
					.as("a quote reading 'Itinerary — £0.00' invites the question of what it was for")
					.extracting(line -> line.getTxtSection())
					.doesNotContain("ITINERARY");
		}

		@Test
		@DisplayName("a booking with nothing on it costs nothing")
		void emptyBooking() {
			todaysVatRules();

			ServiceEventPricing.Priced priced = pricing.priceFor(eventFor(100, 10));

			assertThat(priced.getSubtotal()).isEqualByComparingTo("0.00");
			assertThat(priced.getVat()).isEqualByComparingTo("0.00");
			assertThat(priced.getTotal()).isEqualByComparingTo("0.00");
			assertThat(priced.getLines()).isEmpty();
		}

		/**
		 * A booking nobody has said the size of is priced at nothing per head.
		 *
		 * <p>
		 * Rather than throwing. An enquiry arrives before the guest count is
		 * known, and refusing to price it would mean refusing to save it.
		 */
		@Test
		@DisplayName("a booking with no guest count prices to nothing rather than failing")
		void noGuestCount() {
			todaysVatRules();
			EventMaster event = eventFor(null, null);
			event.getFoodSelections().add(dishOn(event, "Chicken Karahi", "25.00",
					EnmPriceMultiplierType.PER_GUEST, null, null));

			ServiceEventPricing.Priced priced = pricing.priceFor(event);

			assertThat(priced.getFood()).isEqualByComparingTo("0.00");
		}
	}

	// ── fixture ──────────────────────────────────────────────────────────

	private static EventMaster eventFor(Integer guests, Integer tables) {
		EventMaster event = new EventMaster();
		event.setSerEventMasterId(1);
		event.setNumNumberOfGuests(guests);
		event.setNumNumberOfTables(tables);
		event.setFoodSelections(new ArrayList<>());
		event.setDecorSelections(new ArrayList<>());
		event.setExtrasSelections(new ArrayList<>());
		event.setServicesSelections(new ArrayList<>());
		return event;
	}

	/**
	 * @param agreedTotal what the office settled on, or null to let the rules
	 *                    decide.
	 */
	private static EventMenuFoodSelection dishOn(EventMaster event, String name, String unitPrice,
			EnmPriceMultiplierType basis, BigDecimal guestsPerStation, BigDecimal agreedTotal) {

		MenuItem dish = new MenuItem();
		dish.setSerMenuItemId(1L);
		dish.setTxtName(name);
		dish.setNumPrice(unitPrice == null ? null : new BigDecimal(unitPrice));
		dish.setEnmPriceMultiplierType(basis);
		dish.setNumGuestsPerStation(guestsPerStation);

		EventMenuFoodSelection selection = new EventMenuFoodSelection();
		selection.setEventMaster(event);
		selection.setMenuItem(dish);
		selection.setBlnIsDeleted(false);
		selection.setNumFinalPrice(agreedTotal);
		return selection;
	}

	private static void withOption(EventDecorCategorySelection selection, String name, String price) {
		DecorCategoryPropertyMaster master = new DecorCategoryPropertyMaster();
		master.setSerPropertyId(1);
		master.setTxtPropertyName(name);
		master.setNumPrice(new BigDecimal(price));

		EventDecorPropertySelection chosen = new EventDecorPropertySelection();
		chosen.setProperty(master);
		chosen.setBlnIsDeleted(false);
		chosen.setNumPrice(new BigDecimal(price));

		selection.getSelectedProperties().add(chosen);
	}

	private static EventDecorCategorySelection decorOn(EventMaster event, String name, String price) {
		DecorCategoryMaster category = new DecorCategoryMaster();
		category.setSerDecorCategoryId(1);
		category.setTxtDecorCategoryName(name);
		category.setNumPrice(price == null ? null : new BigDecimal(price));

		EventDecorCategorySelection selection = new EventDecorCategorySelection();
		selection.setEventMaster(event);
		selection.setDecorCategory(category);
		selection.setBlnIsDeleted(false);
		selection.setNumPrice(price == null ? null : new BigDecimal(price));
		selection.setSelectedProperties(new ArrayList<>());
		return selection;
	}
}
