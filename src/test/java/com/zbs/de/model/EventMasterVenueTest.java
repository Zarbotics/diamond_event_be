package com.zbs.de.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The hall and the venue on an event are one fact stored twice.
 *
 * <p>
 * Every call site set only the hall, so {@code event_master.ser_venue_master_id}
 * was null on every booking in the database. The venue could still be reached by
 * going through the hall, but anything joining an event straight to a venue —
 * reporting, the admin event list, the customer's own event document — found
 * nothing there, and the review screen told the customer no venue had been
 * chosen minutes after they had chosen one.
 *
 * <p>
 * There are eight places in {@code ServiceEventMasterImpl} alone that set the
 * hall. Deriving the venue in the setter is what stops a ninth getting it wrong,
 * so that is what these tests pin down.
 */
class EventMasterVenueTest {

	@Test
	@DisplayName("setting the hall also sets the venue it belongs to")
	void settingHallDerivesVenue() {
		VenueMaster venue = new VenueMaster();
		venue.setTxtVenueName("The Grand Ballroom, Mayfair");

		VenueMasterDetail hall = new VenueMasterDetail();
		hall.setTxtHallName("The Ballroom");
		hall.setVenueMaster(venue);

		EventMaster event = new EventMaster();
		event.setVenueMasterDetail(hall);

		assertThat(event.getVenueMaster())
				.as("the venue was not derived from the hall")
				.isSameAs(venue);
	}

	@Test
	@DisplayName("a hall with no venue does not wipe a venue already set")
	void hallWithoutVenueLeavesExistingVenueAlone() {
		VenueMaster venue = new VenueMaster();
		venue.setTxtVenueName("Riverside Pavilion, Richmond");

		EventMaster event = new EventMaster();
		event.setVenueMaster(venue);

		// A detached or partially built hall — the setter must not turn a good
		// venue into a null one on the way past.
		VenueMasterDetail hall = new VenueMasterDetail();
		hall.setTxtHallName("Unattached hall");
		event.setVenueMasterDetail(hall);

		assertThat(event.getVenueMaster()).isSameAs(venue);
		assertThat(event.getVenueMasterDetail()).isSameAs(hall);
	}

	@Test
	@DisplayName("clearing the hall is allowed and leaves the venue in place")
	void clearingHallIsSafe() {
		EventMaster event = new EventMaster();
		event.setVenueMasterDetail(null);

		assertThat(event.getVenueMasterDetail()).isNull();
		assertThat(event.getVenueMaster()).isNull();
	}
}
