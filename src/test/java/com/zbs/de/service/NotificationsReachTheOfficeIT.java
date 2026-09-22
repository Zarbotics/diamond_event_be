package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.Notification;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.service.impl.ServiceNotifications;
import com.zbs.de.util.enums.EnmNotificationCategory;
import com.zbs.de.util.enums.EnmNotificationPriority;

/**
 * A notification reaches the people who need to know.
 *
 * <h2>What this was written from</h2>
 *
 * {@code notification_master} holds zero rows in a production database with 296
 * bookings on it. Not zero unread — zero.
 *
 * <p>
 * The cause was in the address, not in the plumbing. Every notification went to
 * {@code getCurrentUserId()}: the person who had just done the thing. Telling
 * somebody what they themselves have this second done is a receipt, and when
 * the thing was done in the customer journey that person was the customer — so
 * a booking arriving from the website notified the customer about their own
 * booking and the office was told nothing at all.
 *
 * <p>
 * So the thing to pin is not "a notification is created". It is <em>who gets
 * it</em>, and every test here is about that.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class NotificationsReachTheOfficeIT {

	private static final String MARKER = "IT-NOTIFY";

	@Autowired
	private ServiceNotifications serviceNotifications;

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private UserMaster firstStaff;
	private UserMaster secondStaff;
	private UserMaster customer;

	@BeforeAll
	static void requireDatabase() {
		String url = System.getenv().getOrDefault("TEST_DB_URL",
				"jdbc:postgresql://localhost:5432/diamond_ev_test");
		String user = System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres");
		String password = System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres");

		try (Connection ignored = DriverManager.getConnection(url, user, password)) {
			// reachable
		} catch (Exception e) {
			Assumptions.abort("No test database at " + url + " — skipping. (" + e.getMessage() + ")");
		}
	}

	@BeforeEach
	void seedPeople() {
		clearDown();
		firstStaff = staff("Office one");
		secondStaff = staff("Office two");
		customer = person("Customer", "ROLE_USER");
	}

	@AfterEach
	void clearDown() {
		jdbcTemplate.update("DELETE FROM notification WHERE ser_user_id IN ("
				+ "  SELECT ser_user_id FROM user_master WHERE txt_name LIKE ?)", MARKER + "%");
		jdbcTemplate.update("DELETE FROM user_master WHERE txt_name LIKE ?", MARKER + "%");
	}

	/**
	 * The defect, stated as a requirement.
	 *
	 * <p>
	 * A booking taken by a customer tells the office. It does not tell the
	 * customer, and it does not tell nobody.
	 */
	@Test
	@DisplayName("a booking taken by a customer tells the office, not the customer")
	void theOfficeIsTold() {
		serviceNotifications.raise(
				new ServiceNotifications.Event(EnmNotificationCategory.BOOKING_TAKEN, "Walima booked for 12-06-2027")
						.body("Reference EV-1234.")
						.causedBy(customer.getSerUserId()));

		assertThat(titlesFor(firstStaff))
				.as("the office was not told a booking had been taken")
				.containsExactly("Walima booked for 12-06-2027");
		assertThat(titlesFor(secondStaff))
				.as("only one member of staff was told")
				.hasSize(1);
		assertThat(titlesFor(customer))
				.as("the customer was notified about their own booking, which is a receipt not a notification")
				.isEmpty();
	}

	/**
	 * Nobody is told what they themselves just did.
	 *
	 * <p>
	 * An administrator editing a booking does not need telling that they edited
	 * a booking. Their colleague does.
	 */
	@Test
	@DisplayName("the person who did it is not told about it")
	void theActorIsNotTold() {
		serviceNotifications.raise(
				new ServiceNotifications.Event(EnmNotificationCategory.BOOKING_CHANGED, "Guest count changed")
						.causedBy(firstStaff.getSerUserId()));

		assertThat(titlesFor(firstStaff))
				.as("somebody was told about their own action")
				.isEmpty();
		assertThat(titlesFor(secondStaff))
				.as("their colleague should have been told")
				.hasSize(1);
	}

	/**
	 * Something the system noticed reaches everybody.
	 *
	 * <p>
	 * With no actor there is nobody to exclude — a day going full is not
	 * anybody's action.
	 */
	@Test
	@DisplayName("something nobody did reaches the whole office")
	void systemEventsReachEverybody() {
		serviceNotifications.raise(
				new ServiceNotifications.Event(EnmNotificationCategory.CAPACITY_REACHED,
						"Saturday 12 June is now full")
						.priority(EnmNotificationPriority.URGENT));

		assertThat(titlesFor(firstStaff)).hasSize(1);
		assertThat(titlesFor(secondStaff)).hasSize(1);
	}

	/**
	 * Repeats collapse rather than piling up.
	 *
	 * <p>
	 * Twelve payments against one booking in an afternoon is one line saying
	 * twelve, not twelve lines. The difference is a bell somebody reads and a
	 * bell somebody turns off.
	 */
	@Test
	@DisplayName("the same thing happening twice is one line, not two")
	void repeatsCollapse() {
		for (int i = 1; i <= 3; i++) {
			serviceNotifications.raise(
					new ServiceNotifications.Event(EnmNotificationCategory.PAYMENT_RECEIVED,
							i + " payments against EV-1234")
							.groupedBy("payment:EV-1234"));
		}

		assertThat(titlesFor(firstStaff))
				.as("three payments produced three lines instead of one")
				.containsExactly("3 payments against EV-1234");
	}

	/**
	 * A repeat comes back unread.
	 *
	 * <p>
	 * It has changed since it was read, so leaving it marked read would hide
	 * the second payment behind the first.
	 */
	@Test
	@DisplayName("an updated repeat is unread again")
	void aRepeatIsUnreadAgain() {
		serviceNotifications.raise(
				new ServiceNotifications.Event(EnmNotificationCategory.PAYMENT_RECEIVED, "1 payment")
						.groupedBy("payment:EV-1234"));

		Notification first = serviceNotifications.inboxFor(firstStaff.getSerUserId()).get(0);
		serviceNotifications.markRead(first.getSerNotificationId(), firstStaff.getSerUserId());
		assertThat(serviceNotifications.unreadCountFor(firstStaff.getSerUserId())).isZero();

		serviceNotifications.raise(
				new ServiceNotifications.Event(EnmNotificationCategory.PAYMENT_RECEIVED, "2 payments")
						.groupedBy("payment:EV-1234"));

		assertThat(serviceNotifications.unreadCountFor(firstStaff.getSerUserId()))
				.as("a second payment stayed hidden behind a notification already read")
				.isEqualTo(1);
	}

	/**
	 * Read state is personal.
	 *
	 * <p>
	 * One member of staff reading something does not read it for everybody.
	 */
	@Test
	@DisplayName("one person reading it does not read it for the other")
	void readStateIsPersonal() {
		serviceNotifications.raise(
				new ServiceNotifications.Event(EnmNotificationCategory.BOOKING_TAKEN, "A booking"));

		Notification mine = serviceNotifications.inboxFor(firstStaff.getSerUserId()).get(0);
		serviceNotifications.markRead(mine.getSerNotificationId(), firstStaff.getSerUserId());

		assertThat(serviceNotifications.unreadCountFor(firstStaff.getSerUserId())).isZero();
		assertThat(serviceNotifications.unreadCountFor(secondStaff.getSerUserId()))
				.as("reading it marked it read for somebody else too")
				.isEqualTo(1);
	}

	/**
	 * Somebody cannot read another person's notification.
	 *
	 * <p>
	 * A small thing to get wrong and an unpleasant one to explain.
	 */
	@Test
	@DisplayName("a notification can only be read by the person it is for")
	void cannotReadSomebodyElses() {
		serviceNotifications.raise(
				new ServiceNotifications.Event(EnmNotificationCategory.BOOKING_TAKEN, "A booking"));

		Notification theirs = serviceNotifications.inboxFor(secondStaff.getSerUserId()).get(0);

		assertThat(serviceNotifications.markRead(theirs.getSerNotificationId(), firstStaff.getSerUserId()))
				.as("one member of staff marked another's notification read")
				.isFalse();
		assertThat(serviceNotifications.unreadCountFor(secondStaff.getSerUserId())).isEqualTo(1);
	}

	/**
	 * A low-priority notification does not light the badge.
	 *
	 * <p>
	 * A badge that is permanently non-zero is one nobody looks at.
	 */
	@Test
	@DisplayName("something filed for reference does not light the badge")
	void lowPriorityDoesNotCount() {
		serviceNotifications.raise(
				new ServiceNotifications.Event(EnmNotificationCategory.SYSTEM, "Nightly figures updated")
						.priority(EnmNotificationPriority.LOW));

		assertThat(serviceNotifications.inboxFor(firstStaff.getSerUserId()))
				.as("it should still be findable")
				.hasSize(1);
		assertThat(serviceNotifications.unreadCountFor(firstStaff.getSerUserId()))
				.as("but it should not be demanding attention")
				.isZero();
	}

	/** Dismissing takes it out of the list, having implied it was seen. */
	@Test
	@DisplayName("dismissing one takes it out of the bell")
	void dismissing() {
		serviceNotifications.raise(
				new ServiceNotifications.Event(EnmNotificationCategory.BOOKING_TAKEN, "A booking"));

		Notification mine = serviceNotifications.inboxFor(firstStaff.getSerUserId()).get(0);
		serviceNotifications.dismiss(mine.getSerNotificationId(), firstStaff.getSerUserId());

		assertThat(serviceNotifications.inboxFor(firstStaff.getSerUserId())).isEmpty();
		assertThat(serviceNotifications.unreadCountFor(firstStaff.getSerUserId())).isZero();
	}

	// ── fixture ──────────────────────────────────────────────────────────

	private List<String> titlesFor(UserMaster user) {
		return jdbcTemplate.queryForList(
				"SELECT txt_title FROM notification WHERE ser_user_id = ? ORDER BY ser_notification_id",
				String.class, user.getSerUserId());
	}

	private UserMaster staff(String name) {
		return person(name, "ROLE_ADMIN");
	}

	private UserMaster person(String name, String role) {
		UserMaster user = new UserMaster();
		user.setTxtName(MARKER + " " + name);
		user.setTxtEmail(MARKER.toLowerCase() + "-" + System.nanoTime() + "@example.test");
		user.setTxtRole(role);
		user.setBlnIsActive(true);
		user.setBlnIsDeleted(false);
		return repositoryUserMaster.saveAndFlush(user);
	}
}
