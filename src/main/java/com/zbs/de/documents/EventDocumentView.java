package com.zbs.de.documents;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything the customer's event document displays, already formatted.
 *
 * <p>
 * Deliberately a flat, presentation-shaped view model rather than the entity
 * or the API DTO. The template does no formatting, no null-juggling and no
 * business logic — which is what keeps the document readable and its rendering
 * testable without a database.
 */
public class EventDocumentView {

	private String reference;
	private String headline;
	private String eventTypeName;
	private String eventDate;
	private String generatedOn;

	private String venueName;
	private String venueLine;
	private String guestCount;
	private String tableCount;

	private String brideName;
	private String groomName;
	private String contactName;
	private String contactPhone;

	private String eventNotes;
	private String cateringNotes;
	private String decorNotes;
	private String servicesNotes;
	private String extrasNotes;
	private String supplierNotes;

	private List<TimelineEntry> runningOrder = new ArrayList<>();
	private List<Group> menuCourses = new ArrayList<>();
	private List<Group> decorGroups = new ArrayList<>();
	private List<Item> services = new ArrayList<>();
	private List<Item> extras = new ArrayList<>();

	/** One line of the running order. */
	public record TimelineEntry(String time, String label) {
		public String getTime() {
			return time;
		}

		public String getLabel() {
			return label;
		}
	}

	/** A named item, optionally with a short qualifier. */
	public record Item(String name, String note) {
		public String getName() {
			return name;
		}

		public String getNote() {
			return note;
		}
	}

	/** A course of the menu, or a décor category. */
	public record Group(String name, List<Item> items) {
		public String getName() {
			return name;
		}

		public List<Item> getItems() {
			return items;
		}
	}

	// --- accessors -------------------------------------------------------

	public String getReference() { return reference; }
	public void setReference(String reference) { this.reference = reference; }

	public String getHeadline() { return headline; }
	public void setHeadline(String headline) { this.headline = headline; }

	public String getEventTypeName() { return eventTypeName; }
	public void setEventTypeName(String eventTypeName) { this.eventTypeName = eventTypeName; }

	public String getEventDate() { return eventDate; }
	public void setEventDate(String eventDate) { this.eventDate = eventDate; }

	public String getGeneratedOn() { return generatedOn; }
	public void setGeneratedOn(String generatedOn) { this.generatedOn = generatedOn; }

	public String getVenueName() { return venueName; }
	public void setVenueName(String venueName) { this.venueName = venueName; }

	public String getVenueLine() { return venueLine; }
	public void setVenueLine(String venueLine) { this.venueLine = venueLine; }

	public String getGuestCount() { return guestCount; }
	public void setGuestCount(String guestCount) { this.guestCount = guestCount; }

	public String getTableCount() { return tableCount; }
	public void setTableCount(String tableCount) { this.tableCount = tableCount; }

	public String getBrideName() { return brideName; }
	public void setBrideName(String brideName) { this.brideName = brideName; }

	public String getGroomName() { return groomName; }
	public void setGroomName(String groomName) { this.groomName = groomName; }

	public String getContactName() { return contactName; }
	public void setContactName(String contactName) { this.contactName = contactName; }

	public String getContactPhone() { return contactPhone; }
	public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

	public String getEventNotes() { return eventNotes; }
	public void setEventNotes(String eventNotes) { this.eventNotes = eventNotes; }

	public String getCateringNotes() { return cateringNotes; }
	public void setCateringNotes(String cateringNotes) { this.cateringNotes = cateringNotes; }

	public String getDecorNotes() { return decorNotes; }
	public void setDecorNotes(String decorNotes) { this.decorNotes = decorNotes; }

	public String getServicesNotes() { return servicesNotes; }
	public void setServicesNotes(String servicesNotes) { this.servicesNotes = servicesNotes; }

	public String getExtrasNotes() { return extrasNotes; }
	public void setExtrasNotes(String extrasNotes) { this.extrasNotes = extrasNotes; }

	public String getSupplierNotes() { return supplierNotes; }
	public void setSupplierNotes(String supplierNotes) { this.supplierNotes = supplierNotes; }

	public List<TimelineEntry> getRunningOrder() { return runningOrder; }
	public void setRunningOrder(List<TimelineEntry> runningOrder) { this.runningOrder = runningOrder; }

	public List<Group> getMenuCourses() { return menuCourses; }
	public void setMenuCourses(List<Group> menuCourses) { this.menuCourses = menuCourses; }

	public List<Group> getDecorGroups() { return decorGroups; }
	public void setDecorGroups(List<Group> decorGroups) { this.decorGroups = decorGroups; }

	public List<Item> getServices() { return services; }
	public void setServices(List<Item> services) { this.services = services; }

	public List<Item> getExtras() { return extras; }
	public void setExtras(List<Item> extras) { this.extras = extras; }
}
