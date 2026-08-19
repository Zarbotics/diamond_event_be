package com.zbs.de.service;

import com.zbs.de.model.ConsultationBooking;

/**
 * Telling people what happened to a consultation.
 *
 * <p>
 * A separate interface from {@link ServiceConsultation} rather than a few more
 * methods on it, for one reason: <strong>an email must never be able to undo a
 * booking</strong>. The mail server is the least reliable thing in this
 * system — it is somebody else's, it needs credentials that are absent in
 * development, and it is reached over the network — and a customer losing their
 * slot because Gmail was slow is far worse than a customer not receiving a
 * confirmation.
 *
 * <p>
 * So every method here is defined to swallow its own failures and log them. The
 * booking is already committed by the time any of them is called, and none of
 * them throws. That is a promise this interface makes to its callers, not an
 * accident of the implementation, which is why it is written down here.
 */
public interface ServiceConsultationNotifier {

	/**
	 * A slot was taken and needs no agreeing — tells the customer it is booked.
	 */
	void bookingConfirmed(ConsultationBooking booking);

	/**
	 * A time was requested and is being held — tells the customer it is not a
	 * booking yet, and tells the host somebody is waiting on them.
	 */
	void bookingRequested(ConsultationBooking booking);

	/** The team agreed to a request. Carries the joining link, if there is one. */
	void requestApproved(ConsultationBooking booking);

	/** The team said no. Carries the reason, which is the useful part. */
	void requestDeclined(ConsultationBooking booking, String reason);

	/** Called off, by either side. */
	void bookingCancelled(ConsultationBooking booking, String reason, boolean byCustomer);
}
