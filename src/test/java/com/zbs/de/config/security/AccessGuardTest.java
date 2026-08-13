package com.zbs.de.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.EventMaster;
import com.zbs.de.repository.RepositoryEventMaster;

/**
 * Ownership rules.
 *
 * <p>
 * These cover the finding that a customer could read and edit any other
 * customer's booking by incrementing an id in the request body. Each test names
 * the attack it prevents.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccessGuardTest {

	private static final int ALICE_CUSTOMER_ID = 10;
	private static final int BOB_CUSTOMER_ID = 20;
	private static final int ALICE_EVENT_ID = 100;
	private static final int BOB_EVENT_ID = 200;

	@Mock
	private CurrentUser currentUser;

	@Mock
	private RepositoryEventMaster repositoryEventMaster;

	private AccessGuard guard;

	@BeforeEach
	void setUp() {
		guard = new AccessGuard(currentUser, repositoryEventMaster);

		// Signed in as Alice, a customer.
		when(currentUser.isAdmin()).thenReturn(false);
		when(currentUser.customerIds()).thenReturn(List.of(ALICE_CUSTOMER_ID));

		when(repositoryEventMaster.findByIdAndBlnIsDeletedFalse(ALICE_EVENT_ID))
				.thenReturn(Optional.of(eventOwnedBy(ALICE_CUSTOMER_ID)));
		when(repositoryEventMaster.findByIdAndBlnIsDeletedFalse(BOB_EVENT_ID))
				.thenReturn(Optional.of(eventOwnedBy(BOB_CUSTOMER_ID)));
		when(repositoryEventMaster.findByIdAndBlnIsDeletedFalse(999)).thenReturn(Optional.empty());
	}

	@Test
	@DisplayName("a customer may read their own customer record")
	void allowsOwnCustomerRecord() {
		assertThatCode(() -> guard.assertCanAccessCustomer(ALICE_CUSTOMER_ID)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("a customer may not read another customer's record")
	void deniesOtherCustomerRecord() {
		assertThatThrownBy(() -> guard.assertCanAccessCustomer(BOB_CUSTOMER_ID))
				.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@DisplayName("a missing customer id is denied rather than treated as 'any'")
	void deniesNullCustomerId() {
		assertThatThrownBy(() -> guard.assertCanAccessCustomer(null)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@DisplayName("a customer may read their own event")
	void allowsOwnEvent() {
		assertThatCode(() -> guard.assertCanAccessEvent(ALICE_EVENT_ID)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("a customer may not read another customer's event")
	void deniesOtherCustomersEvent() {
		assertThatThrownBy(() -> guard.assertCanAccessEvent(BOB_EVENT_ID)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@DisplayName("an unknown event id is denied with the same message as a forbidden one, so ids cannot be probed")
	void deniesUnknownEventWithoutLeakingExistence() {
		String forbidden = catchMessage(() -> guard.assertCanAccessEvent(BOB_EVENT_ID));
		String missing = catchMessage(() -> guard.assertCanAccessEvent(999));
		assertThat(missing).isEqualTo(forbidden);
	}

	@Test
	@DisplayName("resolveCustomerId ignores a spoofed id and returns the caller's own")
	void resolveIgnoresSpoofedId() {
		assertThat(guard.resolveCustomerId(null)).isEqualTo(ALICE_CUSTOMER_ID);
		assertThat(guard.resolveCustomerId(ALICE_CUSTOMER_ID)).isEqualTo(ALICE_CUSTOMER_ID);
		assertThatThrownBy(() -> guard.resolveCustomerId(BOB_CUSTOMER_ID)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@DisplayName("a signed-in user with no linked customer record cannot act as anybody")
	void deniesWhenNoCustomerRecordLinked() {
		when(currentUser.customerIds()).thenReturn(List.of());
		assertThatThrownBy(() -> guard.resolveCustomerId(null)).isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> guard.assertCanAccessCustomer(ALICE_CUSTOMER_ID))
				.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@DisplayName("an administrator bypasses ownership checks")
	void adminBypassesOwnership() {
		when(currentUser.isAdmin()).thenReturn(true);

		assertThatCode(() -> guard.assertCanAccessCustomer(BOB_CUSTOMER_ID)).doesNotThrowAnyException();
		assertThatCode(() -> guard.assertCanAccessEvent(BOB_EVENT_ID)).doesNotThrowAnyException();
		assertThat(guard.resolveCustomerId(BOB_CUSTOMER_ID)).isEqualTo(BOB_CUSTOMER_ID);
	}

	@Test
	@DisplayName("an event with no owner is never accessible to a customer")
	void deniesOrphanedEvent() {
		EventMaster orphan = new EventMaster();
		orphan.setCustomerMaster(null);
		when(repositoryEventMaster.findByIdAndBlnIsDeletedFalse(300)).thenReturn(Optional.of(orphan));

		assertThatThrownBy(() -> guard.assertCanAccessEvent(300)).isInstanceOf(AccessDeniedException.class);
	}

	private static EventMaster eventOwnedBy(int customerId) {
		CustomerMaster owner = new CustomerMaster();
		owner.setSerCustId(customerId);
		EventMaster event = new EventMaster();
		event.setCustomerMaster(owner);
		return event;
	}

	private static String catchMessage(Runnable action) {
		try {
			action.run();
			throw new AssertionError("expected AccessDeniedException");
		} catch (AccessDeniedException expected) {
			return expected.getMessage();
		}
	}
}
