package com.zbs.de.config.security;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.zbs.de.model.EventMaster;
import com.zbs.de.repository.RepositoryEventMaster;

/**
 * Row-level ownership checks for customer-facing operations.
 *
 * <p>
 * {@link PortalEndpoints} decides which <em>operations</em> a customer may
 * invoke. This decides which <em>rows</em> they may touch. Both are required:
 * without this, a customer could call a legitimately-permitted endpoint such as
 * {@code /eventMaster/getEventById} and simply increment the id in the request
 * body to walk the entire customer base.
 *
 * <p>
 * Administrators bypass every check here — the admin surface is authorised by
 * role in the security chain instead.
 */
@Component
public class AccessGuard {

	private final CurrentUser currentUser;
	private final RepositoryEventMaster repositoryEventMaster;

	public AccessGuard(CurrentUser currentUser, RepositoryEventMaster repositoryEventMaster) {
		this.currentUser = currentUser;
		this.repositoryEventMaster = repositoryEventMaster;
	}

	/**
	 * Asserts the caller may act on the given customer record.
	 *
	 * @throws AccessDeniedException if the record belongs to somebody else
	 */
	public void assertCanAccessCustomer(Integer customerId) {
		if (currentUser.isAdmin()) {
			return;
		}
		if (customerId == null) {
			throw new AccessDeniedException("A customer must be identified for this operation.");
		}
		List<Integer> owned = currentUser.customerIds();
		if (!owned.contains(customerId)) {
			throw new AccessDeniedException("You do not have access to this customer record.");
		}
	}

	/**
	 * Asserts the caller may act on the given event.
	 *
	 * @throws AccessDeniedException if the event belongs to somebody else
	 */
	public void assertCanAccessEvent(Integer eventId) {
		if (currentUser.isAdmin()) {
			return;
		}
		if (eventId == null) {
			throw new AccessDeniedException("An event must be identified for this operation.");
		}
		EventMaster event = repositoryEventMaster.findByIdAndBlnIsDeletedFalse(eventId)
				.orElseThrow(() -> new AccessDeniedException("You do not have access to this event."));

		Integer ownerId = event.getCustomerMaster() == null ? null : event.getCustomerMaster().getSerCustId();
		if (ownerId == null || !currentUser.customerIds().contains(ownerId)) {
			// Deliberately the same message as the not-found case above, so that the
			// response cannot be used to probe which event ids exist.
			throw new AccessDeniedException("You do not have access to this event.");
		}
	}

	/**
	 * Returns the customer id the caller is allowed to operate as.
	 *
	 * <p>
	 * For a customer this is resolved from their authenticated identity and the
	 * requested value is only honoured if it matches — client-supplied owner ids are
	 * never trusted. For an administrator the requested value is returned as-is.
	 */
	public Integer resolveCustomerId(Integer requestedCustomerId) {
		if (currentUser.isAdmin()) {
			return requestedCustomerId;
		}
		List<Integer> owned = currentUser.customerIds();
		if (owned.isEmpty()) {
			throw new AccessDeniedException("No customer record is linked to your account.");
		}
		if (requestedCustomerId != null && owned.contains(requestedCustomerId)) {
			return requestedCustomerId;
		}
		if (requestedCustomerId != null) {
			throw new AccessDeniedException("You do not have access to this customer record.");
		}
		return owned.get(0);
	}
}
