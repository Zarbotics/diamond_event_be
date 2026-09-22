package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.model.Notification;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryNotification;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.enums.EnmNotificationCategory;
import com.zbs.de.util.enums.EnmNotificationPriority;

/**
 * Telling the office something happened.
 *
 * <h2>The rule this is built on</h2>
 *
 * A notification is addressed to <em>an audience</em>, never to the person who
 * caused it. Telling somebody what they themselves have this second done is a
 * receipt; the screen they are looking at has already told them.
 *
 * <p>
 * The predecessor did exactly that — every notification went to
 * {@code getCurrentUserId()} — which is why {@code notification_master} holds
 * no rows worth having. When a booking arrived through the customer journey
 * the current user was the customer, so the notification went to the customer
 * about their own booking and the office was told nothing at all.
 *
 * <h2>Who counts as the office</h2>
 *
 * Everybody with an administrator role, minus whoever caused the thing. An
 * administrator who edits a booking does not need telling that they edited a
 * booking; their colleague does.
 *
 * <h2>Why raising one can never fail a save</h2>
 *
 * Because the thing that happened has already happened. A booking that was
 * taken, a payment that landed — those are facts before this is called, and
 * losing one because the bell could not be rung would be the wrong trade by a
 * very large margin. Every failure here is logged and swallowed.
 *
 * <p>
 * The predecessor did not do this either: it called {@code .longValue()} on a
 * possibly-null id inside the save's try block, so a null current user turned
 * a successful save into a reported failure.
 *
 * <p>
 * Catching the exception is not enough on its own, and the first version of
 * this class made exactly that mistake. A failure inside the caller's
 * transaction marks it rollback-only, so the save dies at commit however
 * politely the exception was swallowed — the booking is lost and the log says
 * only that a notification could not be raised. {@code REQUIRES_NEW} is what
 * makes the guarantee real: the bell gets its own transaction, and it can fail
 * in it alone.
 */
@Service
public class ServiceNotifications {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceNotifications.class);

	/** How many the bell loads at once. Beyond this is a screen, not a dropdown. */
	private static final int INBOX_PAGE = 30;

	@Autowired
	private RepositoryNotification repository;

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	/**
	 * A thing worth telling the office about.
	 *
	 * <p>
	 * Built rather than passed as nine arguments, because a call with nine
	 * positional arguments is one where two of them end up the wrong way round
	 * and nothing complains.
	 */
	public static final class Event {

		private final EnmNotificationCategory category;
		private final String title;
		private String body;
		private EnmNotificationPriority priority = EnmNotificationPriority.NORMAL;
		private String entityType;
		private Long entityId;
		private String route;
		private String groupKey;
		private Long actorUserId;

		public Event(EnmNotificationCategory category, String title) {
			this.category = category;
			this.title = title;
		}

		public Event body(String value) {
			this.body = value;
			return this;
		}

		public Event priority(EnmNotificationPriority value) {
			this.priority = value;
			return this;
		}

		/** What it is about, and where in the portal to find it. */
		public Event about(String type, Long id, String portalRoute) {
			this.entityType = type;
			this.entityId = id;
			this.route = portalRoute;
			return this;
		}

		/** Repeats sharing this key collapse onto one line. */
		public Event groupedBy(String value) {
			this.groupKey = value;
			return this;
		}

		/** Who did it, so they are not told about themselves. */
		public Event causedBy(Long userId) {
			this.actorUserId = userId;
			return this;
		}
	}

	/**
	 * Tells the office, and returns how many people were told.
	 *
	 * @return the number of recipients, which is zero when there is nobody to
	 *         tell rather than an error — a business with one administrator who
	 *         did the thing themselves is a normal state.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int raise(Event event) {
		try {
			List<UserMaster> audience = office(event.actorUserId);
			if (audience.isEmpty()) {
				return 0;
			}

			Date now = UtilDateAndTime.getCurrentDate();
			List<Notification> raised = new ArrayList<>();

			for (UserMaster recipient : audience) {
				/*
				 * A repeat of something already sitting unread in somebody's
				 * bell updates that one rather than adding another. Twelve
				 * payments in an afternoon is one line, not twelve.
				 */
				if (event.groupKey != null) {
					List<Notification> open = repository.openInGroup(recipient.getSerUserId(), event.groupKey);
					if (!open.isEmpty()) {
						Notification existing = open.get(0);
						existing.setTxtTitle(event.title);
						existing.setTxtBody(event.body);
						existing.setUpdatedDate(now);
						/* Back to unread: it has changed since it was read. */
						existing.setDteReadOn(null);
						repository.save(existing);
						continue;
					}
				}

				Notification notification = new Notification();
				notification.setSerUserId(recipient.getSerUserId());
				notification.setTxtCategory(event.category);
				notification.setTxtPriority(event.priority);
				notification.setTxtTitle(event.title);
				notification.setTxtBody(event.body);
				notification.setTxtEntityType(event.entityType);
				notification.setNumEntityId(event.entityId);
				notification.setTxtRoute(event.route);
				notification.setSerActorUserId(event.actorUserId);
				notification.setTxtGroupKey(event.groupKey);
				notification.setCreatedDate(now);
				notification.setBlnIsActive(true);
				notification.setBlnIsDeleted(false);
				raised.add(notification);
			}

			if (!raised.isEmpty()) {
				repository.saveAll(raised);
			}
			return audience.size();

		} catch (Exception e) {
			/*
			 * The thing this is about has already happened. Losing a booking
			 * because the bell could not be rung would be the wrong trade.
			 */
			LOGGER.error("Could not raise a {} notification: {}", event.category, e.getMessage(), e);
			return 0;
		}
	}

	/**
	 * Everybody who should be told, minus whoever did it.
	 *
	 * <p>
	 * Administrators only. A customer has a user account — that is how the
	 * journey signs them in — and notifying them about the office's work would
	 * be both useless and a disclosure.
	 */
	private List<UserMaster> office(Long actorUserId) {
		List<UserMaster> staff = new ArrayList<>();

		for (UserMaster user : repositoryUserMaster.findAll()) {
			if (Boolean.TRUE.equals(user.getBlnIsDeleted())) {
				continue;
			}
			if (!"ROLE_ADMIN".equalsIgnoreCase(user.getTxtRole())) {
				continue;
			}
			if (actorUserId != null && actorUserId.equals(user.getSerUserId())) {
				continue;
			}
			staff.add(user);
		}
		return staff;
	}

	// ── reading ──────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public List<Notification> inboxFor(Long userId) {
		if (userId == null) {
			return List.of();
		}
		return repository.inboxFor(userId, PageRequest.of(0, INBOX_PAGE));
	}

	@Transactional(readOnly = true)
	public long unreadCountFor(Long userId) {
		return userId == null ? 0 : repository.unreadCountFor(userId);
	}

	/**
	 * Marks one as read.
	 *
	 * <p>
	 * Scoped to the reader. Without that, an id from anywhere marks anybody's
	 * notification read — which is a small thing to get wrong and an
	 * unpleasant one to explain.
	 */
	@Transactional
	public boolean markRead(Long notificationId, Long userId) {
		return repository.findBySerNotificationIdAndSerUserId(notificationId, userId)
				.map(notification -> {
					if (notification.getDteReadOn() == null) {
						notification.setDteReadOn(UtilDateAndTime.getCurrentDate());
						repository.save(notification);
					}
					return true;
				})
				.orElse(false);
	}

	@Transactional
	public boolean dismiss(Long notificationId, Long userId) {
		return repository.findBySerNotificationIdAndSerUserId(notificationId, userId)
				.map(notification -> {
					Date now = UtilDateAndTime.getCurrentDate();
					notification.setDteDismissedOn(now);
					/* Dismissing implies having seen it. */
					if (notification.getDteReadOn() == null) {
						notification.setDteReadOn(now);
					}
					repository.save(notification);
					return true;
				})
				.orElse(false);
	}

	@Transactional
	public int markAllRead(Long userId) {
		if (userId == null) {
			return 0;
		}
		return repository.markAllReadFor(userId, UtilDateAndTime.getCurrentDate());
	}
}
