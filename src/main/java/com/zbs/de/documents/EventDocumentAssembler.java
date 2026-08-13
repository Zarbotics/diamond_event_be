package com.zbs.de.documents;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.zbs.de.documents.EventDocumentView.Group;
import com.zbs.de.documents.EventDocumentView.Item;
import com.zbs.de.documents.EventDocumentView.TimelineEntry;
import com.zbs.de.model.dto.DtoEventDecorCategorySelection;
import com.zbs.de.model.dto.DtoEventDecorExtrasSelection;
import com.zbs.de.model.dto.DtoEventDecorPropertySelection;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventRunningOrder;
import com.zbs.de.model.dto.menu.DtoCustomerMenuCategory;
import com.zbs.de.model.dto.menu.DtoCustomerMenuSubCategory;

/**
 * Turns the event DTO into the flat, formatted view the document renders.
 *
 * <p>
 * All formatting lives here so the template contains no logic, and so this can
 * be unit tested without a database, a servlet or a PDF engine.
 */
@Component
public class EventDocumentAssembler {

	private static final Locale UK = Locale.UK;

	private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", UK);

	/** The formats this field actually arrives in, tried in order. */
	private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
			DateTimeFormatter.ofPattern("dd/MM/yyyy", UK),
			DateTimeFormatter.ofPattern("dd-MM-yyyy", UK),
			DateTimeFormatter.ofPattern("yyyy-MM-dd", UK));

	/**
	 * The running order, in the order the day actually happens.
	 *
	 * <p>
	 * The customer journey collects all of these; only some are persisted today
	 * because {@code event_running_order} still has fixed columns. Anything not
	 * stored simply does not appear, and this map needs no change when the
	 * itinerary table lands.
	 */
	private static final Map<String, String> RUNNING_ORDER_LABELS = new LinkedHashMap<>();
	static {
		RUNNING_ORDER_LABELS.put("txtGuestArrival", "Guest arrival");
		RUNNING_ORDER_LABELS.put("txtBrideGuestArrival", "Bride's guests arrive");
		RUNNING_ORDER_LABELS.put("txtGroomGuestArrival", "Groom's guests arrive");
		RUNNING_ORDER_LABELS.put("txtBaratArrival", "Barat arrival");
		RUNNING_ORDER_LABELS.put("txtNikah", "Nikah");
		RUNNING_ORDER_LABELS.put("txtBrideEntrance", "Bride's entrance");
		RUNNING_ORDER_LABELS.put("txtGroomEntrance", "Groom's entrance");
		RUNNING_ORDER_LABELS.put("txtCouplesEntrance", "Couple's entrance");
		RUNNING_ORDER_LABELS.put("txtRingExchange", "Ring exchange");
		RUNNING_ORDER_LABELS.put("txtDua", "Dua");
		RUNNING_ORDER_LABELS.put("txtRams", "Rams");
		RUNNING_ORDER_LABELS.put("txtMeal", "Meal");
		RUNNING_ORDER_LABELS.put("txtCakeCutting", "Cake cutting");
		RUNNING_ORDER_LABELS.put("txtSpeeches", "Speeches");
		RUNNING_ORDER_LABELS.put("txtDance", "Dancing");
		RUNNING_ORDER_LABELS.put("txtEndOfNight", "End of night");
	}

	public EventDocumentView assemble(DtoEventMaster event) {
		EventDocumentView doc = new EventDocumentView();

		doc.setReference(trimToNull(event.getTxtEventMasterCode()));
		doc.setEventTypeName(firstNonBlank(event.getTxtEventTypeName(), event.getTxtOtherEventType()));
		doc.setEventDate(formatEventDate(event.getDteEventDate()));
		doc.setGeneratedOn(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", UK)));

		doc.setBrideName(fullName(event.getTxtBrideFirstName(), event.getTxtBrideLastName(), event.getTxtBrideName()));
		doc.setGroomName(fullName(event.getTxtGroomFirstName(), event.getTxtGroomLastName(), event.getTxtGroomName()));
		doc.setHeadline(headline(doc, event));

		doc.setVenueName(trimToNull(event.getTxtVenueName()));
		doc.setVenueLine(venueLine(event));
		doc.setGuestCount(event.getNumNumberOfGuests() == null ? null : String.valueOf(event.getNumNumberOfGuests()));
		doc.setTableCount(event.getNumNumberOfTables() == null ? null : String.valueOf(event.getNumNumberOfTables()));

		doc.setContactName(fullName(event.getTxtContactPersonFirstName(), event.getTxtContactPersonLastName(),
				event.getTxtCustName()));
		doc.setContactPhone(trimToNull(event.getTxtContactPersonPhoneNo()));

		doc.setEventNotes(trimToNull(event.getTxtEventRemarks()));
		doc.setCateringNotes(trimToNull(event.getTxtCateringRemarks()));
		doc.setDecorNotes(trimToNull(event.getTxtDecoreRemarks()));
		doc.setServicesNotes(trimToNull(event.getTxtEventServicesRemarks()));
		doc.setExtrasNotes(trimToNull(event.getTxtEventExtrasRemarks()));
		doc.setSupplierNotes(trimToNull(event.getTxtExternalSupplierRemarks()));

		doc.setRunningOrder(runningOrder(event.getDtoEventRunningOrder()));
		doc.setMenuCourses(menuCourses(event.getMenuCategoriesSelection()));
		doc.setDecorGroups(decorGroups(event.getDtoEventDecorSelections()));
		doc.setServices(extrasItems(event.getServicesSelections()));
		doc.setExtras(extrasItems(event.getExtrasSelections()));

		return doc;
	}

	// --- sections --------------------------------------------------------

	private List<TimelineEntry> runningOrder(DtoEventRunningOrder ro) {
		List<TimelineEntry> entries = new ArrayList<>();
		if (ro == null) {
			return entries;
		}

		RUNNING_ORDER_LABELS.forEach((field, label) -> {
			String raw = readRunningOrderField(ro, field);
			String time = formatTime(raw);
			if (time != null) {
				entries.add(new TimelineEntry(time, label));
			}
		});
		return entries;
	}

	/**
	 * Reflective read so that adding a ceremony to the labels map above is the
	 * only change needed — there is no switch to keep in step.
	 */
	private String readRunningOrderField(DtoEventRunningOrder ro, String field) {
		try {
			String getter = "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
			Object value = ro.getClass().getMethod(getter).invoke(ro);
			return value == null ? null : value.toString();
		} catch (ReflectiveOperationException | RuntimeException e) {
			// A label with no matching field yet is expected, not an error.
			return null;
		}
	}

	private List<Group> menuCourses(List<DtoCustomerMenuCategory> categories) {
		List<Group> groups = new ArrayList<>();
		if (categories == null) {
			return groups;
		}

		for (DtoCustomerMenuCategory category : categories) {
			if (category.getSubCategories() == null) {
				continue;
			}
			for (DtoCustomerMenuSubCategory sub : category.getSubCategories()) {
				List<Item> items = new ArrayList<>();

				if (sub.getItems() != null) {
					sub.getItems().forEach(i -> items
							.add(new Item(trimToNull(i.getTxtName()), trimToNull(i.getTxtDescription()))));
				}
				if (sub.getCompositeItems() != null) {
					sub.getCompositeItems().forEach(i -> {
						String name = trimToNull(readString(i, "getTxtparentMenuItemName"));
						if (name == null) {
							name = trimToNull(readString(i, "getTxtName"));
						}
						if (name != null) {
							items.add(new Item(name, trimToNull(readString(i, "getTxtparentMenuItemDesc"))));
						}
					});
				}

				items.removeIf(i -> i.name() == null);
				if (!items.isEmpty()) {
					// Named for the course, prefixed by the category when they differ, so
					// "Buffet — Starters" reads correctly on the page.
					String name = sub.getSubCategoryName();
					if (category.getCategoryName() != null
							&& !category.getCategoryName().equalsIgnoreCase(name)) {
						name = category.getCategoryName() + " — " + name;
					}
					groups.add(new Group(name, items));
				}
			}
		}
		return groups;
	}

	private List<Group> decorGroups(List<DtoEventDecorCategorySelection> selections) {
		List<Group> groups = new ArrayList<>();
		if (selections == null) {
			return groups;
		}

		for (DtoEventDecorCategorySelection selection : selections) {
			List<Item> items = new ArrayList<>();
			if (selection.getSelectedProperties() != null) {
				for (DtoEventDecorPropertySelection property : selection.getSelectedProperties()) {
					String value = trimToNull(property.getTxtPropertyValue());
					String label = trimToNull(property.getTxtPropertyName());
					if (label == null && value == null) {
						continue;
					}
					items.add(new Item(label != null ? label : value, label != null ? value : null));
				}
			}
			if (!items.isEmpty() || trimToNull(selection.getTxtRemarks()) != null) {
				if (items.isEmpty()) {
					items.add(new Item(trimToNull(selection.getTxtRemarks()), null));
				}
				groups.add(new Group(trimToNull(selection.getTxtDecorCategoryName()), items));
			}
		}
		return groups;
	}

	private List<Item> extrasItems(List<DtoEventDecorExtrasSelection> selections) {
		List<Item> items = new ArrayList<>();
		if (selections == null) {
			return items;
		}
		for (DtoEventDecorExtrasSelection selection : selections) {
			String name = firstNonBlank(selection.getTxtOptionName(), selection.getTxtExtrasName());
			if (name != null) {
				items.add(new Item(name, null));
			}
		}
		return items;
	}

	// --- formatting ------------------------------------------------------

	/**
	 * The cover line. A wedding is the couple; anything else is the event's own
	 * name, then the occasion, then a neutral fallback — never a blank cover.
	 */
	private String headline(EventDocumentView doc, DtoEventMaster event) {
		if (doc.getBrideName() != null && doc.getGroomName() != null) {
			return doc.getBrideName() + " & " + doc.getGroomName();
		}
		String name = firstNonBlank(event.getTxtEventMasterName(), event.getTxtCustName(),
				doc.getEventTypeName());
		return name != null ? name : "Your event";
	}

	private String venueLine(DtoEventMaster event) {
		String venue = trimToNull(event.getTxtVenueName());
		String hall = event.getDtoEventVenue() == null ? null
				: trimToNull(readString(event.getDtoEventVenue(), "getTxtHallName"));
		if (venue == null) {
			return hall;
		}
		return hall == null ? venue : venue + ", " + hall;
	}

	/**
	 * "Saturday 15 August 2026" — never a guess.
	 *
	 * <p>
	 * Parses strictly, and only the formats that actually occur on this field.
	 * Deliberately does not use {@code UtilDateAndTime}, whose parser falls
	 * through several formats and then to a different method again — the same
	 * loose handling that let the old confirmation screen substitute the current
	 * year for an unreadable value and print a confidently wrong date.
	 */
	private String formatEventDate(String raw) {
		String value = trimToNull(raw);
		if (value == null) {
			return null;
		}

		// Trim an ISO timestamp down to its date part.
		int t = value.indexOf('T');
		String datePart = t > 0 ? value.substring(0, t) : value;

		for (DateTimeFormatter format : DATE_FORMATS) {
			try {
				return LocalDate.parse(datePart, format).format(DISPLAY_DATE);
			} catch (java.time.format.DateTimeParseException ignored) {
				// try the next format
			}
		}
		return null;
	}

	/** 24-hour, which is the UK convention for a schedule. */
	private String formatTime(String raw) {
		String value = trimToNull(raw);
		if (value == null) {
			return null;
		}

		// Already "HH:mm"?
		if (value.matches("^\\d{1,2}:\\d{2}$")) {
			String[] parts = value.split(":");
			return String.format("%02d:%s", Integer.parseInt(parts[0]), parts[1]);
		}

		for (String pattern : new String[] { "yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ss", "HH:mm:ss" }) {
			try {
				SimpleDateFormat in = new SimpleDateFormat(pattern, UK);
				return new SimpleDateFormat("HH:mm", UK).format(in.parse(value));
			} catch (java.text.ParseException ignored) {
				// try the next pattern
			}
		}
		return value;
	}

	private String fullName(String first, String last, String fallback) {
		String f = trimToNull(first);
		String l = trimToNull(last);
		if (f != null && l != null) {
			return f + " " + l;
		}
		if (f != null) {
			return f;
		}
		if (l != null) {
			return l;
		}
		return trimToNull(fallback);
	}

	private String readString(Object target, String getter) {
		try {
			Object value = target.getClass().getMethod(getter).invoke(target);
			return value == null ? null : value.toString();
		} catch (ReflectiveOperationException | RuntimeException e) {
			return null;
		}
	}

	private String firstNonBlank(String... values) {
		for (String value : values) {
			String trimmed = trimToNull(value);
			if (trimmed != null) {
				return trimmed;
			}
		}
		return null;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed) ? null : trimmed;
	}
}
