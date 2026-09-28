package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import com.zbs.de.model.dto.DtoResult;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.model.dto.DtoEventType;
import com.zbs.de.service.ServiceEventType;

/**
 * Which moments a running order has, answered by the business's own data.
 *
 * <h2>What this replaces</h2>
 *
 * A hardcoded map in the control panel's booking form, keyed by
 * {@code ser_event_type_id} — a surrogate key the business assigns by adding
 * rows on the Event Types screen:
 *
 * <pre>
 *   const RUNNING_ORDER_FIELD_MAPPING = {
 *     2: [ …sixteen wedding moments… ],
 *     3: ['txtGuestArrival', 'txtBrideEntrance', 'txtGroomEntrance', …],
 *     4: […], 5: […],
 *   };
 * </pre>
 *
 * Behaviour keyed to a business-editable id drifts, and it had. In this
 * database id 2 is <b>Corporate Event</b>, so the sixteen <em>wedding</em>
 * moments are filed under the corporate event; id 3 is <b>Wedding</b>, given a
 * six-moment list with no Nikah and no Barat arrival; 4 and 5 are <b>Mehndi</b>
 * and <b>Awards Dinner</b>, both offered a "Bride entrance".
 *
 * <p>
 * It never showed, because the line that read the map used the literal 2 with
 * the line that uses the booking's own type commented out directly above it —
 * so every booking got the sixteen whatever it was for. That is the only reason
 * the drift did no harm, and exactly why "just uncomment the line" would have
 * caused some: wedding bookings would have started being asked for a day with
 * no Nikah in it.
 *
 * <h2>Why every type is seeded with all sixteen</h2>
 *
 * Because narrowing is what loses data. The form submits only the fields it has
 * rendered, so the first save of a booking whose list has shrunk clears
 * whatever was stored in the moments that stopped being shown. Seeding the full
 * list reproduces exactly what is on screen today, so the change is invisible —
 * and narrowing becomes the business's decision, made per type, rather than a
 * code change that silently drops stored times.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class RunningOrderMomentsIT {

	/** The sixteen, in the order the day runs. */
	private static final List<String> THE_WHOLE_DAY = List.of(
			"txtGuestArrival", "txtBrideGuestArrival", "txtGroomGuestArrival", "txtBaratArrival",
			"txtNikah", "txtBrideEntrance", "txtGroomEntrance", "txtCouplesEntrance",
			"txtDua", "txtRingExchange", "txtCakeCutting", "txtRams",
			"txtSpeeches", "txtDance", "txtMeal", "txtEndOfNight");

	@Autowired
	private ServiceEventType serviceEventType;

	/** Put back after each test, so narrowing one does not narrow the next. */
	@AfterEach
	void giveThemTheirDayBack() {
		for (Integer type : jdbcTemplate.queryForList(
				"SELECT ser_event_type_id FROM event_type WHERE COALESCE(bln_is_deleted,false)=false",
				Integer.class)) {
			serviceEventType.setRunningOrderMoments(type, THE_WHOLE_DAY);
		}
	}

	private Integer someEventType() {
		return jdbcTemplate.queryForObject(
				"SELECT ser_event_type_id FROM event_type"
						+ " WHERE COALESCE(bln_is_deleted,false)=false ORDER BY ser_event_type_id LIMIT 1",
				Integer.class);
	}

	private List<String> momentsOf(Integer type) {
		return jdbcTemplate.queryForList(
				"SELECT txt_field FROM event_type_running_order_moment"
						+ " WHERE ser_event_type_id = ? AND bln_is_deleted = false"
						+ " ORDER BY num_display_order",
				String.class, type);
	}

	private long countOf(String table) {
		return jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Long.class);
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeAll
	static void requireDatabase() {
		try (Connection ignored = DriverManager.getConnection(url(), user(), password())) {
			// reachable
		} catch (Exception e) {
			Assumptions.abort("No test database at " + url() + " — skipping. (" + e.getMessage() + ")");
		}
	}

	private static String url() {
		return System.getenv().getOrDefault("TEST_DB_URL", "jdbc:postgresql://localhost:5432/diamond_ev_test");
	}

	private static String user() {
		return System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres");
	}

	private static String password() {
		return System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres");
	}

	/**
	 * The one that makes the change safe to ship. Every type keeps the full
	 * sixteen, so no booking's running order narrows and nothing stored in a
	 * moment is dropped on its next save.
	 */
	@Test
	@DisplayName("every event type is seeded with the whole day, so nothing narrows")
	void nothingNarrows() {
		List<Integer> typesWithoutTheFullDay = jdbcTemplate.queryForList(
				"SELECT t.ser_event_type_id FROM event_type t"
						+ " WHERE COALESCE(t.bln_is_deleted, false) = false"
						+ " AND (SELECT count(*) FROM event_type_running_order_moment m"
						+ "      WHERE m.ser_event_type_id = t.ser_event_type_id"
						+ "        AND m.bln_is_deleted = false) <> ?",
				Integer.class, THE_WHOLE_DAY.size());

		assertThat(typesWithoutTheFullDay)
				.as("a type with fewer moments than are on screen today would lose the rest on its next save")
				.isEmpty();
	}

	@Test
	@DisplayName("the moments come back in the order the day runs")
	void inDayOrder() {
		Integer anyType = jdbcTemplate.queryForObject(
				"SELECT ser_event_type_id FROM event_type"
						+ " WHERE COALESCE(bln_is_deleted, false) = false ORDER BY ser_event_type_id LIMIT 1",
				Integer.class);

		List<String> moments = jdbcTemplate.queryForList(
				"SELECT txt_field FROM event_type_running_order_moment"
						+ " WHERE ser_event_type_id = ? AND bln_is_deleted = false"
						+ " ORDER BY num_display_order",
				String.class, anyType);

		assertThat(moments)
				.as("a running order read out of chronological order is one nobody can follow")
				.containsExactlyElementsOf(THE_WHOLE_DAY);
	}

	/**
	 * The booking form flattens the sub-events and works from those, so a type
	 * carrying its moments only at the top would arrive with none and the whole
	 * section would render empty.
	 */
	/*
	 * Transactional, because this calls the service directly rather than
	 * through a request. The mapper reads each type's documents, which are
	 * lazy; in production `spring.jpa.open-in-view=true` keeps the session open
	 * for the whole request, and a test has no request.
	 */
	@Test
	@Transactional
	@DisplayName("the moments reach the sub-events, which is what the booking form reads")
	void subEventsCarryThemToo() {
		List<DtoEventType> types = serviceEventType.getAllActiveEventTypesWithSubEvents();

		assertThat(types).isNotEmpty();
		assertThat(types).allSatisfy(type -> {
			assertThat(type.getTxtRunningOrderMoments())
					.as("the main type " + type.getTxtEventTypeName() + " has no moments")
					.isNotEmpty();

			if (type.getSubEvents() != null) {
				assertThat(type.getSubEvents()).allSatisfy(sub -> assertThat(sub.getTxtRunningOrderMoments())
						.as("the sub-event " + sub.getTxtEventTypeName() + " has no moments")
						.isNotEmpty());
			}
		});
	}

	/**
	 * Narrowing what a kind of event asks for.
	 *
	 * <p>
	 * A corporate dinner does not have a Barat arrival or a Nikah, and until now
	 * it was asked for both.
	 */
	@Test
	@DisplayName("a kind of event can be narrowed to the moments it actually has")
	void canBeNarrowed() {
		Integer type = someEventType();

		serviceEventType.setRunningOrderMoments(type,
				List.of("txtGuestArrival", "txtSpeeches", "txtMeal", "txtEndOfNight"));

		assertThat(momentsOf(type))
				.as("and kept in the order the day runs, whatever order they were sent in")
				.containsExactly("txtGuestArrival", "txtSpeeches", "txtMeal", "txtEndOfNight");
	}

	/**
	 * The guarantee the whole design rests on.
	 *
	 * <p>
	 * Unticking a moment changes what the booking screen <em>asks for</em>. It
	 * must not touch a booking: a form submits only the fields it has rendered,
	 * so a time that stopped being shown would be cleared on that booking's next
	 * save — for an unrelated reason, months later, by somebody who did nothing
	 * wrong. The screen goes on showing it instead, marked as unusual.
	 */
	@Test
	@DisplayName("narrowing a kind of event does not touch a single booking")
	void narrowingTouchesNoBooking() {
		Integer type = someEventType();

		long runningOrdersBefore = countOf("event_running_order");
		long eventsBefore = countOf("event_master");

		serviceEventType.setRunningOrderMoments(type, List.of("txtGuestArrival"));

		assertThat(countOf("event_running_order"))
				.as("a recorded time is a fact about a booking, not about its kind of event")
				.isEqualTo(runningOrdersBefore);
		assertThat(countOf("event_master")).isEqualTo(eventsBefore);
	}

	@Test
	@DisplayName("a field name the screen invented is refused rather than stored")
	void onlyRealMoments() {
		Integer type = someEventType();

		serviceEventType.setRunningOrderMoments(type,
				List.of("txtGuestArrival", "txtSomethingNobodyHasAColumnFor"));

		assertThat(momentsOf(type)).containsExactly("txtGuestArrival");
	}

	@Test
	@DisplayName("a kind of event can be given the whole day back")
	void canBeWidenedAgain() {
		Integer type = someEventType();

		serviceEventType.setRunningOrderMoments(type, List.of("txtMeal"));
		serviceEventType.setRunningOrderMoments(type, THE_WHOLE_DAY);

		assertThat(momentsOf(type)).containsExactlyElementsOf(THE_WHOLE_DAY);
	}

	@Test
	@DisplayName("refuses a kind of event that does not exist, in words")
	void refusesAnUnknownType() {
		DtoResult result = serviceEventType.setRunningOrderMoments(987654, List.of("txtMeal"));

		assertThat(result.getTxtMessage()).isEqualTo("Failure");
		assertThat(String.valueOf(result.getResult())).contains("no longer exists");
	}

	/**
	 * A type cannot list the same moment twice — the running order would show
	 * the field twice and the second would win the save.
	 */
	@Test
	@DisplayName("a type cannot be given the same moment twice")
	void noDuplicates() {
		Integer duplicates = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM (SELECT ser_event_type_id, txt_field"
						+ "  FROM event_type_running_order_moment"
						+ "  GROUP BY 1, 2 HAVING count(*) > 1) AS d",
				Integer.class);

		assertThat(duplicates).isZero();
	}
}
