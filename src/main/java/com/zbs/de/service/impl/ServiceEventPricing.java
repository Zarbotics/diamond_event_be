package com.zbs.de.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.EventDecorCategorySelection;
import com.zbs.de.model.EventDecorExtrasSelection;
import com.zbs.de.model.EventDecorPropertySelection;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.EventPriceLine;
import com.zbs.de.model.MenuItem;
import com.zbs.de.repository.RepositoryEventPriceLine;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.enums.EnmPriceMultiplierType;

/**
 * What a booking costs, worked out by the server.
 *
 * <h2>Why this exists</h2>
 *
 * Nothing on the server worked out what an event cost. Every figure stored
 * against a booking arrived from a browser and was written down as given: the
 * office's rules lived in five hundred lines of JavaScript, the customer
 * journey had its own separate arithmetic, and the two had never been checked
 * against each other because there was nowhere they could be.
 *
 * <p>
 * Two things followed from that. A customer's browser could post any price it
 * liked. And the journey never posted a quoted total at all, so every enquiry a
 * customer submitted was stored quoted at zero and re-priced by hand afterwards.
 *
 * <h2>The rule this works to</h2>
 *
 * The server prices the booking. A price arriving from a browser is a request,
 * not a fact.
 *
 * <p>
 * The exception is a price a member of staff has deliberately set — agreed on
 * the telephone, matched against a competitor, discounted for a repeat
 * customer. Those are kept, and marked as overridden, with what the rules would
 * have said recorded beside them. The point is never to discard a decision a
 * person made; it is to make the difference visible.
 *
 * <h2>Why every figure carries a sentence</h2>
 *
 * A total nobody can take apart is a total nobody can defend to a customer who
 * queries it. Each line says what it was for, what the unit price was, what it
 * was multiplied by and why — in the same words on the quote, in the office and
 * in the journey, because they are built here rather than three times over.
 *
 * <h2>What this does not do</h2>
 *
 * It does not decide whether its own answer is used. That is
 * {@code pricing.server.authoritative}, which is off until somebody turns it
 * on. Until then this computes, records, and says so in the log when it
 * disagrees with the figure the client sent — which is how the two get
 * compared on real bookings before anything depends on the result.
 */
@Service
public class ServiceEventPricing {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventPricing.class);

	/** Money. Two places, half up, everywhere, without exception. */
	private static final int MONEY_SCALE = 2;
	private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

	private static final String FOOD = "FOOD";
	private static final String DECOR = "DECOR";
	private static final String DECOR_PROPERTY = "DECOR_PROPERTY";
	private static final String EXTRA = "EXTRA";
	private static final String SERVICE = "SERVICE";
	private static final String ITINERARY = "ITINERARY";
	private static final String SERVING_DISHES = "SERVING_DISHES";

	@Autowired
	private ServiceAppSettings serviceAppSettings;

	@Autowired
	private RepositoryEventPriceLine repositoryEventPriceLine;

	/**
	 * What the whole booking comes to, and how.
	 *
	 * <p>
	 * A value rather than something written down, so that the caller decides
	 * whether it is stored. Pricing something and recording the price are two
	 * different acts and a screen that only wants to show a figure should not
	 * have to write to the database to see one.
	 */
	public static final class Priced {

		private final List<EventPriceLine> lines;
		private final BigDecimal food;
		private final BigDecimal decor;
		private final BigDecimal extras;
		private final BigDecimal services;
		private final BigDecimal vat;
		private final BigDecimal subtotal;
		private final BigDecimal total;

		Priced(List<EventPriceLine> lines, BigDecimal food, BigDecimal decor, BigDecimal extras,
				BigDecimal services, BigDecimal vat, BigDecimal subtotal, BigDecimal total) {
			this.lines = lines;
			this.food = food;
			this.decor = decor;
			this.extras = extras;
			this.services = services;
			this.vat = vat;
			this.subtotal = subtotal;
			this.total = total;
		}

		public List<EventPriceLine> getLines() {
			return lines;
		}

		public BigDecimal getFood() {
			return food;
		}

		/**
		 * Décor alone. Extras are their own total now, because the office can
		 * charge VAT on one and not the other.
		 */
		public BigDecimal getDecor() {
			return decor;
		}

		public BigDecimal getExtras() {
			return extras;
		}

		public BigDecimal getServices() {
			return services;
		}

		public BigDecimal getVat() {
			return vat;
		}

		/** Everything chargeable, before VAT and before any discount. */
		public BigDecimal getSubtotal() {
			return subtotal;
		}

		/** What the customer is asked for, after VAT and after the discount. */
		public BigDecimal getTotal() {
			return total;
		}
	}

	/**
	 * Prices a booking from what is stored against it.
	 *
	 * <p>
	 * From the stored selections rather than from the payload, deliberately.
	 * The payload is what somebody is asking for; the selections are what the
	 * booking is. Pricing the second means the figure always describes the
	 * booking as it now stands, whichever screen saved it and whatever that
	 * screen chose to send.
	 */
	@Transactional(readOnly = true)
	public Priced priceFor(EventMaster event) {
		List<EventPriceLine> lines = new ArrayList<>();

		int guests = countOf(event.getNumNumberOfGuests());
		int tables = countOf(event.getNumNumberOfTables());

		BigDecimal food = priceFood(event, guests, tables, lines);
		BigDecimal decor = priceDecor(event, guests, tables, lines);
		BigDecimal extras = priceExtras(event, guests, tables, lines, false, EXTRA);
		BigDecimal services = priceExtras(event, guests, tables, lines, true, SERVICE);

		/*
		 * The two the office types straight onto the booking. They are figures
		 * rather than selections, so there is nothing to work out — but they are
		 * charged for, and they are lines rather than a lump added to the
		 * subtotal so that the office can put VAT on them like anything else.
		 */
		BigDecimal itinerary = flatCharge(ITINERARY, "Itinerary", event.getNumItineraryPrice(), lines);
		BigDecimal servingDishes = flatCharge(SERVING_DISHES, "Serving dishes",
				event.getNumServingDishesPrice(), lines);

		BigDecimal subtotal = money(food.add(decor).add(extras).add(services)
				.add(itinerary).add(servingDishes));

		/*
		 * VAT is worked out line by line and added up, not taken on the
		 * subtotal. The office can charge it on some parts of a bill and not
		 * others, so a single percentage of the whole would be wrong the moment
		 * anybody used that — and a total that cannot show which lines carried
		 * the VAT is one nobody can check against a return.
		 */
		BigDecimal vat = BigDecimal.ZERO;
		for (EventPriceLine line : lines) {
			vat = vat.add(applyVatTo(line));
		}
		vat = money(vat);

		/*
		 * A discount comes off the whole bill including VAT, which is what the
		 * office's JavaScript does today. It cannot take the total below zero: a
		 * discount larger than the bill is a data-entry slip, and invoicing a
		 * customer a negative amount is not something that should happen
		 * quietly.
		 */
		BigDecimal discount = safe(event.getNumDiscount());
		BigDecimal total = money(subtotal.add(vat).subtract(discount));
		if (total.signum() < 0) {
			LOGGER.warn("Event {} has a discount of {} against a bill of {} — the total is held at zero",
					event.getSerEventMasterId(), discount, money(subtotal.add(vat)));
			total = BigDecimal.ZERO.setScale(MONEY_SCALE);
		}

		for (EventPriceLine line : lines) {
			line.setSerEventMasterId(event.getSerEventMasterId());
		}

		return new Priced(lines, money(food), money(decor), money(extras), money(services),
				vat, subtotal, total);
	}

	/**
	 * Decides whether a line carries VAT, and how much, and records both on it.
	 *
	 * @return the VAT for this line, to be added to the bill's
	 */
	private BigDecimal applyVatTo(EventPriceLine line) {
		boolean vatable = serviceAppSettings.isVatChargedOn(line.getTxtSection());
		line.setBlnIsVatable(vatable);

		if (!vatable || line.getNumLineTotal() == null || line.getNumLineTotal().signum() == 0) {
			line.setNumVat(BigDecimal.ZERO.setScale(MONEY_SCALE));
			return BigDecimal.ZERO;
		}

		BigDecimal vat = line.getNumLineTotal()
				.multiply(serviceAppSettings.getVatPercent())
				.divide(BigDecimal.valueOf(100), MONEY_SCALE, MONEY_ROUNDING);
		line.setNumVat(vat);
		return vat;
	}

	/**
	 * A charge the office typed rather than chose: a line with nothing to
	 * multiply.
	 *
	 * <p>
	 * Nothing is recorded when the figure is absent or zero, because a quote
	 * listing "Itinerary — £0.00" invites the question of what it was for.
	 */
	private BigDecimal flatCharge(String section, String description, BigDecimal amount,
			List<EventPriceLine> lines) {

		BigDecimal charge = safe(amount);
		if (charge.signum() == 0) {
			return BigDecimal.ZERO;
		}

		EventPriceLine line = new EventPriceLine();
		line.setTxtSection(section);
		line.setTxtDescription(description);
		line.setNumUnitPrice(money(charge));
		line.setEnmBasis(EnmPriceMultiplierType.FLAT);
		line.setNumQuantity(BigDecimal.ONE);
		line.setNumLineTotal(money(charge));
		line.setNumCatalogueTotal(money(charge));
		line.setBlnIsOverridden(false);
		line.setTxtReason("Set on the booking by the office");
		stamp(line);

		lines.add(line);
		return money(charge);
	}

	/**
	 * Prices the booking and writes the working down.
	 *
	 * <p>
	 * The previous working is deleted rather than added to. These rows are the
	 * current answer, not a history — what a customer was quoted last month is
	 * recorded by the quote that was sent them, which is a different fact kept
	 * somewhere else.
	 */
	@Transactional
	public Priced priceAndRecord(EventMaster event) {
		Priced priced = priceFor(event);

		if (event.getSerEventMasterId() == null) {
			// Nothing to hang the working off yet. The figures are still
			// returned, so a caller mid-save can use them.
			return priced;
		}

		repositoryEventPriceLine.deleteForEvent(event.getSerEventMasterId());
		if (!priced.getLines().isEmpty()) {
			repositoryEventPriceLine.saveAll(priced.getLines());
		}
		return priced;
	}

	// ── the sections ─────────────────────────────────────────────────────

	/**
	 * The food.
	 *
	 * <h4>Why the stored line price wins over the catalogue</h4>
	 *
	 * Because it may be an override. The office can agree £22 a head on a dish
	 * the catalogue prices at £25, and the engine must not quietly put it back
	 * up on the next save of the same booking. Where the stored figure matches
	 * what the rules produce it is not an override at all, and the line says so.
	 */
	private BigDecimal priceFood(EventMaster event, int guests, int tables, List<EventPriceLine> lines) {
		if (event.getFoodSelections() == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal total = BigDecimal.ZERO;
		for (EventMenuFoodSelection selection : event.getFoodSelections()) {
			if (Boolean.TRUE.equals(selection.getBlnIsDeleted())) {
				continue;
			}

			MenuItem dish = selection.getMenuItem();
			if (dish == null) {
				continue;
			}

			EnmPriceMultiplierType basis = dish.getEnmPriceMultiplierType() == null
					? EnmPriceMultiplierType.PER_GUEST
					: dish.getEnmPriceMultiplierType();

			BigDecimal unit = safe(dish.getNumPrice());
			BigDecimal quantity = quantityFor(basis, guests, tables, dish.getNumGuestsPerStation());
			BigDecimal catalogueTotal = money(unit.multiply(quantity));

			// What is actually charged: the office's figure if it set one,
			// otherwise what the rules make it.
			BigDecimal charged = selection.getNumFinalPrice() != null
					? money(selection.getNumFinalPrice())
					: catalogueTotal;

			EventPriceLine line = lineOf(FOOD, nameOf(dish), idOf(dish), unit, basis, quantity,
					charged, catalogueTotal, guests, tables, dish.getNumGuestsPerStation());
			lines.add(line);
			total = total.add(charged);
		}
		return total;
	}

	/**
	 * The décor: the categories, and the options chosen beneath them.
	 *
	 * <h4>The double-charging rule</h4>
	 *
	 * A category carries a price and so does each option under it. Charging
	 * both bills the customer twice for one stage. Where a category has a price
	 * of its own, the options beneath it are listed at zero and said to be
	 * included — listed rather than hidden, because a customer reading the quote
	 * chose those options and needs to see that they are on it.
	 */
	private BigDecimal priceDecor(EventMaster event, int guests, int tables, List<EventPriceLine> lines) {
		BigDecimal total = BigDecimal.ZERO;
		boolean categoryReplaces = serviceAppSettings.doesDecorCategoryPriceReplaceProperties();

		if (event.getDecorSelections() != null) {
			for (EventDecorCategorySelection selection : event.getDecorSelections()) {
				if (Boolean.TRUE.equals(selection.getBlnIsDeleted())) {
					continue;
				}

				DecorCategoryMaster category = selection.getDecorCategory();
				String name = category != null && category.getTxtDecorCategoryName() != null
						? category.getTxtDecorCategoryName()
						: "Décor";

				BigDecimal catalogueUnit = category != null ? safe(category.getNumPrice()) : BigDecimal.ZERO;
				BigDecimal charged = selection.getNumPrice() != null
						? money(selection.getNumPrice())
						: money(catalogueUnit);

				boolean categoryIsPriced = charged.signum() > 0;

				EventPriceLine line = lineOf(DECOR, name, idOf(category), charged,
						EnmPriceMultiplierType.FLAT, BigDecimal.ONE, charged, money(catalogueUnit),
						guests, tables, null);
				lines.add(line);
				total = total.add(charged);

				if (selection.getSelectedProperties() == null) {
					continue;
				}

				for (EventDecorPropertySelection property : selection.getSelectedProperties()) {
					if (Boolean.TRUE.equals(property.getBlnIsDeleted())) {
						continue;
					}

					String propertyName = property.getProperty() != null
							&& property.getProperty().getTxtPropertyName() != null
									? property.getProperty().getTxtPropertyName()
									: "Option";

					BigDecimal propertyCatalogue = property.getProperty() != null
							? money(safe(property.getProperty().getNumPrice()))
							: BigDecimal.ZERO.setScale(MONEY_SCALE);

					boolean included = categoryReplaces && categoryIsPriced;
					BigDecimal propertyCharged = included
							? BigDecimal.ZERO.setScale(MONEY_SCALE)
							: (property.getNumPrice() != null ? money(property.getNumPrice()) : propertyCatalogue);

					EventPriceLine propertyLine = new EventPriceLine();
					propertyLine.setTxtSection(DECOR_PROPERTY);
					propertyLine.setTxtDescription(name + " — " + propertyName);
					propertyLine.setNumSourceId(property.getProperty() == null
							|| property.getProperty().getSerPropertyId() == null
									? null
									: Long.valueOf(property.getProperty().getSerPropertyId()));
					propertyLine.setNumUnitPrice(included ? BigDecimal.ZERO.setScale(MONEY_SCALE) : propertyCharged);
					propertyLine.setEnmBasis(EnmPriceMultiplierType.FLAT);
					propertyLine.setNumQuantity(BigDecimal.ONE);
					propertyLine.setNumLineTotal(propertyCharged);
					propertyLine.setNumCatalogueTotal(propertyCatalogue);
					propertyLine.setBlnIsOverridden(!included && propertyCharged.compareTo(propertyCatalogue) != 0);
					propertyLine.setTxtReason(included
							? "Included in the price of " + name
							: sentenceFor(propertyCharged, EnmPriceMultiplierType.FLAT, BigDecimal.ONE,
									guests, tables, null));
					stamp(propertyLine);
					lines.add(propertyLine);
					total = total.add(propertyCharged);
				}
			}
		}

		return total;
	}

	/**
	 * Extras and services, which are the same table told apart by a flag.
	 *
	 * <p>
	 * They are priced identically and separated only because VAT treats them
	 * differently — extras go with décor, services stand on their own.
	 */
	private BigDecimal priceExtras(EventMaster event, int guests, int tables, List<EventPriceLine> lines,
			boolean wantServices, String section) {

		List<EventDecorExtrasSelection> selections = wantServices
				? event.getServicesSelections()
				: event.getExtrasSelections();

		if (selections == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal total = BigDecimal.ZERO;
		for (EventDecorExtrasSelection selection : selections) {
			if (Boolean.TRUE.equals(selection.getBlnIsDeleted())) {
				continue;
			}

			String name = selection.getDecorExtrasMaster() != null
					&& selection.getDecorExtrasMaster().getTxtExtrasName() != null
							? selection.getDecorExtrasMaster().getTxtExtrasName()
							: (wantServices ? "Service" : "Extra");

			BigDecimal catalogue = selection.getDecorExtrasMaster() != null
					? money(safe(selection.getDecorExtrasMaster().getNumPrice()))
					: BigDecimal.ZERO.setScale(MONEY_SCALE);

			BigDecimal charged = selection.getNumPrice() != null ? money(selection.getNumPrice()) : catalogue;

			EventPriceLine line = new EventPriceLine();
			line.setTxtSection(section);
			line.setTxtDescription(name);
			line.setNumSourceId(selection.getDecorExtrasMaster() == null
					|| selection.getDecorExtrasMaster().getSerExtrasId() == null
							? null
							: Long.valueOf(selection.getDecorExtrasMaster().getSerExtrasId()));
			line.setNumUnitPrice(charged);
			line.setEnmBasis(EnmPriceMultiplierType.FLAT);
			line.setNumQuantity(BigDecimal.ONE);
			line.setNumLineTotal(charged);
			line.setNumCatalogueTotal(catalogue);
			line.setBlnIsOverridden(charged.compareTo(catalogue) != 0);
			line.setTxtReason(sentenceFor(charged, EnmPriceMultiplierType.FLAT, BigDecimal.ONE,
					guests, tables, null));
			stamp(line);

			lines.add(line);
			total = total.add(charged);
		}
		return total;
	}

	// ── the arithmetic ───────────────────────────────────────────────────

	/**
	 * How many of a thing are charged for.
	 *
	 * <p>
	 * Stations round up, because half a grazing bar is not a thing that can be
	 * supplied. A station size of nothing means one station: a business that has
	 * not said how many guests a bar serves has not said it needs two.
	 */
	private BigDecimal quantityFor(EnmPriceMultiplierType basis, int guests, int tables, BigDecimal guestsPerStation) {
		switch (basis) {
			case PER_GUEST:
				return BigDecimal.valueOf(guests);
			case PER_TABLE:
				return BigDecimal.valueOf(tables);
			case PER_STATION:
				if (guestsPerStation == null || guestsPerStation.signum() <= 0) {
					return BigDecimal.ONE;
				}
				return BigDecimal.valueOf(guests).divide(guestsPerStation, 0, RoundingMode.CEILING).max(BigDecimal.ONE);
			case FLAT:
			default:
				return BigDecimal.ONE;
		}
	}

	// ── building a line ──────────────────────────────────────────────────

	private EventPriceLine lineOf(String section, String description, Long sourceId, BigDecimal unit,
			EnmPriceMultiplierType basis, BigDecimal quantity, BigDecimal charged, BigDecimal catalogueTotal,
			int guests, int tables, BigDecimal guestsPerStation) {

		EventPriceLine line = new EventPriceLine();
		line.setTxtSection(section);
		line.setTxtDescription(description);
		line.setNumSourceId(sourceId);
		line.setNumUnitPrice(money(unit));
		line.setEnmBasis(basis);
		line.setNumQuantity(quantity);
		line.setNumLineTotal(money(charged));
		line.setNumCatalogueTotal(money(catalogueTotal));
		line.setBlnIsOverridden(money(charged).compareTo(money(catalogueTotal)) != 0);
		line.setTxtReason(sentenceFor(money(unit), basis, quantity, guests, tables, guestsPerStation));
		stamp(line);
		return line;
	}

	/**
	 * The sentence beside the number.
	 *
	 * <p>
	 * Built here so the quote, the office and the journey cannot each phrase it
	 * differently — which they currently do, because each builds its own.
	 */
	private String sentenceFor(BigDecimal unit, EnmPriceMultiplierType basis, BigDecimal quantity,
			int guests, int tables, BigDecimal guestsPerStation) {

		String money = "£" + unit.setScale(MONEY_SCALE, MONEY_ROUNDING).toPlainString();
		switch (basis) {
			case PER_GUEST:
				return money + " per guest × " + guests + (guests == 1 ? " guest" : " guests");
			case PER_TABLE:
				return money + " per table × " + tables + (tables == 1 ? " table" : " tables");
			case PER_STATION:
				String stations = quantity.stripTrailingZeros().toPlainString();
				if (guestsPerStation == null || guestsPerStation.signum() <= 0) {
					return money + " per station × " + stations;
				}
				return money + " per station × " + stations + " ("
						+ "one per " + guestsPerStation.stripTrailingZeros().toPlainString() + " guests)";
			case FLAT:
			default:
				return money + ", a fixed price";
		}
	}

	/**
	 * Says so in the log when the engine and the client disagree.
	 *
	 * <p>
	 * This is the whole point of the engine running before it is trusted. Two
	 * sets of rules that have never been compared will differ, and the place to
	 * find out is a log on real bookings rather than a customer's invoice.
	 */
	public void reportDisagreement(Integer eventId, BigDecimal fromClient, BigDecimal fromEngine) {
		if (fromClient == null || fromEngine == null) {
			return;
		}
		if (money(fromClient).compareTo(money(fromEngine)) == 0) {
			return;
		}
		LOGGER.warn("Event {}: the booking screen says the total is {} and the pricing rules make it {}"
				+ " — a difference of {}. The screen's figure is being used"
				+ " (pricing.server.authoritative is off).",
				eventId, money(fromClient), money(fromEngine),
				money(fromClient.subtract(fromEngine)).abs());
	}

	// ── small things ─────────────────────────────────────────────────────

	private void stamp(EventPriceLine line) {
		line.setBlnIsActive(true);
		line.setBlnIsDeleted(false);
		line.setCreatedDate(UtilDateAndTime.getCurrentDate());
	}

	private static String nameOf(MenuItem dish) {
		return dish.getTxtName() != null ? dish.getTxtName() : "Dish";
	}

	private static Long idOf(MenuItem dish) {
		return dish.getSerMenuItemId();
	}

	private static Long idOf(DecorCategoryMaster category) {
		return category == null || category.getSerDecorCategoryId() == null
				? null
				: Long.valueOf(category.getSerDecorCategoryId());
	}

	private static int countOf(Integer given) {
		return given == null || given < 0 ? 0 : given;
	}

	private static BigDecimal safe(BigDecimal given) {
		return given == null ? BigDecimal.ZERO : given;
	}

	private static BigDecimal money(BigDecimal given) {
		return safe(given).setScale(MONEY_SCALE, MONEY_ROUNDING);
	}
}
