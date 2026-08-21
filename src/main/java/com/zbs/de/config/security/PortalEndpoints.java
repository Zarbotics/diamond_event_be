package com.zbs.de.config.security;

/**
 * The endpoints the customer-facing booking portal is allowed to call.
 *
 * <p>
 * Authorisation in this application is <em>default-deny</em>: anything not
 * listed here requires {@link SecurityRoles#ADMIN}. That inversion is
 * deliberate. Previously the security chain ended in
 * {@code anyRequest().authenticated()}, which meant every new controller was
 * reachable by every signed-in customer the moment it was written. Now a new
 * controller is admin-only until someone consciously adds it below.
 *
 * <p>
 * Paths are relative to the servlet context path ({@code /diamond}).
 *
 * <p>
 * <strong>Being on this list is not sufficient on its own.</strong> Endpoints
 * that read or write a specific customer's data must additionally enforce
 * ownership through {@code AccessGuard} — the list controls which
 * <em>operations</em> a customer may invoke, not which <em>rows</em> they may
 * touch.
 */
public final class PortalEndpoints {

	/**
	 * Fully public — no authentication at all.
	 */
	public static final String[] PUBLIC = {
			"/", "/error",
			"/auth/**",
			"/login/**", "/oauth2/**",
			"/deimg/**", "/deimg",
			// Cancelling from the link in a confirmation email. The customer may
			// not be signed in — they may not have an account at all — so the
			// unguessable single-use token is what authorises it.
			"/consultation/cancel",
			// Where Google and Microsoft redirect the administrator's browser
			// after they grant access to a calendar. Public of necessity: it is
			// a redirect, so it carries no bearer token and there is no way to
			// ask for one. The signed `state` parameter is what identifies the
			// request, and it is verified before anything is done — see
			// CalendarOAuthState for what that is defending against.
			"/calendar/oauth/callback",
			// Where a browser reports a failure it could not recover from. Public
			// because the failures most worth hearing about are the ones that
			// stop somebody signing in — a report that requires a session would
			// miss exactly those. ControllerClientError bounds what that opens
			// up: every field truncated, control characters stripped so a report
			// cannot forge log lines, and a ceiling on reports per minute.
			"/clientError",
			"/public/**",
			"/actuator/health", "/actuator/health/**",
	};

	/**
	 * Catalogue reads. Non-sensitive reference data the journey needs in order to
	 * present choices: event types, venues, menus, decor, extras, suppliers.
	 */
	public static final String[] CATALOGUE_READ = {
			"/eventType/getAllActiveEventTypesWithSubEvents",
			"/eventType/getAllActiveSubEventsOnlyCP",
			"/venueMaster/getAllActiveVenuesGroupedByActiveCities",
			"/menuFoodMaster/getAllActiveFoodsByType",
			"/decorCategoryMaster/getAllActiveDecorMasterData",
			"/decorExtras/getAllActiveExtrasData",
			"/decorExtras/getAllActiveServicesData",
			"/vendorMaster/getAllActiveData",
			// Menu reads live under an /admin/menu prefix for historical reasons; they are
			// read-only catalogue queries that the customer journey depends on.
			"/admin/menu/getMenu",
			"/admin/menu/getCateringMenu",
			"/admin/menu/getMenuWithPrices",
			"/admin/menu/getCateringMenuWithPricing",
			// What consultations are on offer and when. Reference data: no
			// customer's details are readable through either.
			"/consultation/types",
			"/consultation/slots",
	};

	/**
	 * Operations on the signed-in customer's own booking. Every one of these is
	 * additionally ownership-checked in the service layer.
	 */
	public static final String[] OWN_BOOKING = {
			"/customerMaster/saveOrUpdate",
			"/customerMaster/getByEmail",
			"/eventMaster/saveWithDocs",
			"/eventMaster/getEventById",
			"/eventMaster/getByCustomerId",
			"/eventMaster/getSummariesByCustomerId",
			"/eventMaster/getByEventIdAndCustomerId",
			"/eventMaster/generateEventCode",
			"/eventMaster/getAlreadyBookedDates",
			"/eventMaster/isDateAlreadyBooked",
			"/cateringDelivery/getByCustomerId",
			"/cateringDelivery/saveOrUpdate",
			// Booking a consultation and looking up the one attached to an
			// event. Cancellation is not here: it is reached from a link in an
			// email, by a customer who may well not be signed in, and is
			// authorised by the single-use token instead.
			"/consultation/book",
			"/consultation/forEvent",
			// Single-segment wildcard for the {eventId} path variable. Ownership of that
			// event is asserted in ControllerReport. The other report endpoints
			// (/report/event/*, /report/kitchen_itinerary/*) are deliberately absent and
			// so remain administrator-only.
			"/report/eventClientSide/*",
			// The customer's event document, HTML and PDF. Ownership asserted in
			// ControllerEventDocument.
			"/documents/event/*",
			"/documents/event/*/pdf",
	};

	/** Everything a signed-in customer may call. */
	public static String[] allCustomerAccessible() {
		String[] all = new String[CATALOGUE_READ.length + OWN_BOOKING.length];
		System.arraycopy(CATALOGUE_READ, 0, all, 0, CATALOGUE_READ.length);
		System.arraycopy(OWN_BOOKING, 0, all, CATALOGUE_READ.length, OWN_BOOKING.length);
		return all;
	}

	private PortalEndpoints() {
	}
}
