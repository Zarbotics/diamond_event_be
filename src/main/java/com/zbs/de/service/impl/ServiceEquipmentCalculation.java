package com.zbs.de.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.EquipmentItem;
import com.zbs.de.model.EquipmentRequirement;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoEquipmentLine;
import com.zbs.de.model.dto.DtoEquipmentRequirementList;
import com.zbs.de.model.dto.DtoEquipmentSource;
import com.zbs.de.repository.RepositoryEquipmentRequirement;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventMenuFoodSelection;
import com.zbs.de.util.enums.EnmEquipmentBasis;

/**
 * What an event needs on the day, worked out from what was sold.
 *
 * <h3>The arithmetic</h3>
 *
 * Every rule that fires produces a number, and the numbers for the same item
 * are added together:
 *
 * <pre>
 *   PER_GUEST    quantity x guests
 *   PER_TABLE    quantity x tables
 *   PER_STATION  quantity x stations, where stations = ceil(guests / guestsPerStation)
 *   PER_EVENT    quantity
 * </pre>
 *
 * then spares are added, then the total is rounded <em>up</em>.
 *
 * <h3>Why it rounds up, and only at the end</h3>
 *
 * Half a plate is not a thing anybody can put on a table, and rounding down
 * sends a wedding one short. Rounding up is the only direction that cannot
 * ruin a Saturday.
 *
 * <p>
 * It rounds once, after everything has been added, rather than per rule.
 * Rounding each rule first turns three rules asking for 0.4 each into three
 * separate 1s — three plates where the arithmetic said two.
 *
 * <h3>Why it is computed and never stored</h3>
 *
 * Because it is derived, and derived data that is stored goes stale silently.
 * The guest count changes on Tuesday, a dish is swapped on Wednesday, and a
 * stored answer from Monday is still sitting there looking authoritative. This
 * is cheap to recompute — two queries and some multiplication — and always
 * agrees with the booking it came from.
 *
 * <p>
 * The one thing that argues for storing it is a run sheet that must not change
 * after it is sent to the kitchen. That is a snapshot taken deliberately at a
 * moment somebody chooses, not a cache, and it should be built when somebody
 * asks for it rather than guessed at now.
 *
 * <h3>Why each line says what asked for it</h3>
 *
 * A number with no explanation cannot be argued with. "315 dinner plates" is
 * useless to somebody who thinks it should be 280; "300 covers of Chicken
 * Karahi at one each, plus 5% spares" is a sentence they can check and
 * correct. Every line carries its sources.
 */
@Service
public class ServiceEquipmentCalculation {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEquipmentCalculation.class);

	@Autowired
	private RepositoryEquipmentRequirement repositoryEquipmentRequirement;

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	@Autowired
	private RepositoryEventMenuFoodSelection repositoryEventMenuFoodSelection;

	/**
	 * The equipment list for one event.
	 *
	 * @return the list, or one carrying an explanation when it cannot be
	 *         worked out. Never null, and never throws for missing data — an
	 *         event with no guest count yet is an ordinary state on the way to
	 *         a booking, not an error.
	 */
	public DtoEquipmentRequirementList calculateFor(Integer eventMasterId) {
		DtoEquipmentRequirementList list = new DtoEquipmentRequirementList();
		list.setSerEventMasterId(eventMasterId);

		EventMaster event = eventMasterId == null ? null
				: repositoryEventMaster.findById(eventMasterId).orElse(null);

		if (event == null) {
			list.setTxtExplanation("That booking could not be found.");
			return list;
		}

		int guests = guestsOf(event);
		int tables = event.getNumNumberOfTables() == null ? 0 : event.getNumNumberOfTables();

		list.setNumGuests(guests);
		list.setNumTables(tables);

		/*
		  Said plainly rather than shown as a list of zeroes. An equipment
		  sheet reading "0 dinner plates" looks like a calculation that ran and
		  found nothing, which is a different and much more alarming thing than
		  a booking that has not been told how many people are coming.
		*/
		if (guests <= 0) {
			list.setTxtExplanation(
					"This booking has no guest count yet, so there is nothing to work out. "
							+ "Add the number of guests and the equipment appears.");
			return list;
		}

		List<MenuItem> chosen = dishesOn(event);
		List<EquipmentRequirement> rules = rulesFor(chosen);

		if (rules.isEmpty()) {
			list.setTxtExplanation(chosen.isEmpty()
					? "No food has been chosen for this booking yet."
					: "Nothing on this menu has equipment rules against it yet. "
							+ "Add them under Equipment and this fills in.");
			return list;
		}

		/*
		  Accumulated per equipment item, in the order the rules arrive, so the
		  same item asked for by three dishes becomes one line of three
		  sources rather than three lines nobody can add up by eye.
		*/
		Map<Integer, DtoEquipmentLine> byItem = new LinkedHashMap<>();
		Map<Integer, BigDecimal> runningTotals = new LinkedHashMap<>();

		for (EquipmentRequirement rule : rules) {
			EquipmentItem item = rule.getEquipmentItem();
			if (item == null) {
				continue; // a rule with no item cannot ask for anything
			}

			BigDecimal amount = amountFor(rule, guests, tables);
			if (amount.signum() <= 0) {
				continue;
			}

			Integer key = item.getSerEquipmentItemId();
			DtoEquipmentLine line = byItem.computeIfAbsent(key, k -> describe(item));
			runningTotals.merge(key, amount, BigDecimal::add);
			line.getSources().add(explain(rule, guests, tables, amount));
		}

		/*
		  Rounded once, here, after everything asking for this item has had its
		  say. Rounding inside the loop would turn three rules wanting 0.4 each
		  into three separate 1s.
		*/
		List<DtoEquipmentLine> lines = new ArrayList<>();
		for (Map.Entry<Integer, DtoEquipmentLine> entry : byItem.entrySet()) {
			DtoEquipmentLine line = entry.getValue();
			BigDecimal total = runningTotals.getOrDefault(entry.getKey(), BigDecimal.ZERO);
			line.setNumQuantity(total.setScale(0, RoundingMode.CEILING).intValueExact());
			lines.add(line);
		}

		lines.sort(Comparator
				.comparing((DtoEquipmentLine l) -> l.getNumCategoryOrder() == null ? Integer.MAX_VALUE
						: l.getNumCategoryOrder())
				.thenComparing(l -> l.getTxtCategoryName() == null ? "" : l.getTxtCategoryName())
				.thenComparing(l -> l.getTxtItemName() == null ? "" : l.getTxtItemName()));

		list.setLines(lines);
		LOGGER.debug("Event {} needs {} kinds of equipment", eventMasterId, lines.size());
		return list;
	}

	// ── the arithmetic ───────────────────────────────────────────────────

	/**
	 * How much of the item this one rule asks for, before rounding.
	 *
	 * <p>
	 * Spares are applied here rather than at the end because they belong to
	 * the rule: a 5% breakage allowance on plates should not inflate the
	 * cake knife that happens to share the line.
	 */
	private static BigDecimal amountFor(EquipmentRequirement rule, int guests, int tables) {
		BigDecimal quantity = rule.getNumQuantity() == null ? BigDecimal.ONE : rule.getNumQuantity();
		EnmEquipmentBasis basis = rule.getEnmBasis() == null ? EnmEquipmentBasis.PER_GUEST : rule.getEnmBasis();

		BigDecimal amount;
		switch (basis) {
		case PER_GUEST:
			amount = quantity.multiply(BigDecimal.valueOf(guests));
			break;
		case PER_TABLE:
			amount = quantity.multiply(BigDecimal.valueOf(tables));
			break;
		case PER_STATION:
			amount = quantity.multiply(BigDecimal.valueOf(stationsFor(rule, guests)));
			break;
		case PER_EVENT:
		default:
			amount = quantity;
			break;
		}

		BigDecimal spare = rule.getNumSparePercent();
		if (spare != null && spare.signum() > 0) {
			amount = amount.add(amount.multiply(spare).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
		}

		return amount;
	}

	/**
	 * How many stations of this kind the guest count calls for.
	 *
	 * <p>
	 * Rounded up: 180 guests at one station per 50 is four stations, not
	 * three and a half, and certainly not three — the fourth is exactly the
	 * one the last thirty guests queue at.
	 */
	private static int stationsFor(EquipmentRequirement rule, int guests) {
		Integer per = rule.getNumGuestsPerStation();
		if (per == null || per <= 0) {
			return 1; // one station however many come
		}
		return BigDecimal.valueOf(guests)
				.divide(BigDecimal.valueOf(per), 0, RoundingMode.CEILING)
				.intValueExact();
	}

	// ── gathering ────────────────────────────────────────────────────────

	/**
	 * The guest count, preferring the number over the text.
	 *
	 * <p>
	 * The event carries both {@code numNumberOfGuests} and
	 * {@code txtNumberOfGuests}, and which one is filled in depends on which
	 * screen took the booking. Falling back to parsing the text is what stops
	 * an office-entered booking calculating nothing.
	 */
	private static int guestsOf(EventMaster event) {
		if (event.getNumNumberOfGuests() != null && event.getNumNumberOfGuests() > 0) {
			return event.getNumNumberOfGuests();
		}

		String text = event.getTxtNumberOfGuests();
		if (text != null) {
			try {
				return Integer.parseInt(text.trim());
			} catch (NumberFormatException ignored) {
				// "around 300" — not a number, and not worth guessing at.
			}
		}
		return 0;
	}

	private List<MenuItem> dishesOn(EventMaster event) {
		List<MenuItem> dishes = new ArrayList<>();
		for (EventMenuFoodSelection selection : repositoryEventMenuFoodSelection
				.findByEventId(event.getSerEventMasterId())) {
			if (selection.getMenuItem() != null) {
				dishes.add(selection.getMenuItem());
			}
		}
		return dishes;
	}

	/**
	 * Every rule that fires for this booking: the ones against the dishes
	 * chosen, plus the ones that apply to every event.
	 */
	private List<EquipmentRequirement> rulesFor(List<MenuItem> dishes) {
		List<EquipmentRequirement> rules = new ArrayList<>(
				repositoryEquipmentRequirement.findAppliedToEveryEvent());

		if (!dishes.isEmpty()) {
			List<Long> ids = dishes.stream().map(MenuItem::getSerMenuItemId).distinct().toList();
			rules.addAll(repositoryEquipmentRequirement.findForMenuItems(ids));
		}

		return rules;
	}

	// ── describing ───────────────────────────────────────────────────────

	private static DtoEquipmentLine describe(EquipmentItem item) {
		DtoEquipmentLine line = new DtoEquipmentLine();
		line.setSerEquipmentItemId(item.getSerEquipmentItemId());
		line.setTxtItemName(item.getTxtName());
		line.setTxtUnit(item.getTxtUnit());
		if (item.getEquipmentCategory() != null) {
			line.setTxtCategoryName(item.getEquipmentCategory().getTxtName());
			line.setNumCategoryOrder(item.getEquipmentCategory().getNumDisplayOrder());
		}
		return line;
	}

	/**
	 * Why this rule contributed what it did, in words.
	 *
	 * <p>
	 * "Chicken Karahi — 1 per guest x 300 guests = 300". The point is that
	 * somebody who thinks the number is wrong can see which rule to change
	 * without reading the code.
	 */
	private static DtoEquipmentSource explain(EquipmentRequirement rule, int guests, int tables,
			BigDecimal amount) {
		DtoEquipmentSource source = new DtoEquipmentSource();

		source.setTxtSourceName(Boolean.TRUE.equals(rule.getBlnAppliesToEveryEvent()) ? "Every event"
				: rule.getMenuItem() == null ? "Unknown" : rule.getMenuItem().getTxtName());

		EnmEquipmentBasis basis = rule.getEnmBasis() == null ? EnmEquipmentBasis.PER_GUEST : rule.getEnmBasis();
		BigDecimal quantity = rule.getNumQuantity() == null ? BigDecimal.ONE : rule.getNumQuantity();
		String qty = quantity.stripTrailingZeros().toPlainString();

		String reason;
		switch (basis) {
		case PER_GUEST:
			reason = qty + " per guest x " + guests + " guests";
			break;
		case PER_TABLE:
			reason = qty + " per table x " + tables + " tables";
			break;
		case PER_STATION:
			int stations = stationsFor(rule, guests);
			reason = qty + " per station x " + stations + (stations == 1 ? " station" : " stations");
			break;
		case PER_EVENT:
		default:
			reason = qty + " for the event";
			break;
		}

		if (rule.getNumSparePercent() != null && rule.getNumSparePercent().signum() > 0) {
			reason += ", plus " + rule.getNumSparePercent().stripTrailingZeros().toPlainString() + "% spares";
		}

		source.setTxtReason(reason);
		source.setNumContributed(amount.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
		return source;
	}
}
