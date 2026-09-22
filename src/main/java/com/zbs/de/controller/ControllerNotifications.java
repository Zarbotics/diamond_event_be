package com.zbs.de.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.service.impl.ServiceCurrentUser;
import com.zbs.de.model.Notification;
import com.zbs.de.service.impl.ServiceNotifications;
import com.zbs.de.util.ResponseMessage;

/**
 * The bell.
 *
 * <h3>Why every route takes the reader from the session</h3>
 *
 * Never from the request. A user id in a path is an invitation to read
 * somebody else's notifications by changing a number, and the reader is always
 * the person holding the session — there is no case where one member of staff
 * legitimately marks another's notification read.
 *
 * <h3>Why the list comes back grouped</h3>
 *
 * Because that is how it is read. "Today", "Yesterday" and "Earlier" is the
 * shape a person scans; a flat list of thirty with timestamps is one they give
 * up on. Grouping it here rather than in the portal means the customer journey
 * — if it ever grows a bell — gets the same grouping without reimplementing
 * it.
 */
@RestController
@RequestMapping("/notification")
public class ControllerNotifications {

	@Autowired
	private ServiceNotifications serviceNotifications;

	/**
	 * This person's bell: what is in it, and how many they have not read.
	 *
	 * <p>
	 * One call rather than two. The portal needs both on every page load and
	 * splitting them doubles the requests to show one icon.
	 */
	@GetMapping("/inbox")
	public ResponseMessage inbox() {
		Long userId = readerId();
		if (userId == null) {
			return new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED,
					"Not signed in", null);
		}

		List<Notification> notifications = serviceNotifications.inboxFor(userId);

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("unread", serviceNotifications.unreadCountFor(userId));
		result.put("notifications", notifications.stream().map(ControllerNotifications::describe).toList());
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Success", result);
	}

	/**
	 * Just the count.
	 *
	 * <p>
	 * For polling, which is far cheaper than fetching thirty rows to render a
	 * number on a badge.
	 */
	@GetMapping("/unread-count")
	public ResponseMessage unreadCount() {
		Long userId = readerId();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Success",
				userId == null ? 0 : serviceNotifications.unreadCountFor(userId));
	}

	@PostMapping("/{id}/read")
	public ResponseMessage markRead(@PathVariable Long id) {
		Long userId = readerId();
		boolean done = userId != null && serviceNotifications.markRead(id, userId);
		return done
				? new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Success", null)
				: new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
						"That notification is not yours", null);
	}

	@PostMapping("/{id}/dismiss")
	public ResponseMessage dismiss(@PathVariable Long id) {
		Long userId = readerId();
		boolean done = userId != null && serviceNotifications.dismiss(id, userId);
		return done
				? new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Success", null)
				: new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
						"That notification is not yours", null);
	}

	@PostMapping("/read-all")
	public ResponseMessage markAllRead() {
		Long userId = readerId();
		int marked = userId == null ? 0 : serviceNotifications.markAllRead(userId);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Success", marked);
	}

	// ── how a notification reaches the portal ────────────────────────────

	private static Map<String, Object> describe(Notification n) {
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("serNotificationId", n.getSerNotificationId());
		dto.put("txtCategory", n.getTxtCategory() == null ? null : n.getTxtCategory().name());
		dto.put("txtPriority", n.getTxtPriority() == null ? null : n.getTxtPriority().name());
		dto.put("txtTitle", n.getTxtTitle());
		dto.put("txtBody", n.getTxtBody());
		dto.put("txtRoute", n.getTxtRoute());
		dto.put("txtEntityType", n.getTxtEntityType());
		dto.put("numEntityId", n.getNumEntityId());
		dto.put("dteCreatedOn", n.getCreatedDate());
		dto.put("dteReadOn", n.getDteReadOn());
		/*
		  Read as a boolean as well as a timestamp. The portal asks "is this
		  unread" on every row, and `dteReadOn === null` is the kind of check
		  that gets written as `!dteReadOn` and then quietly treats a valid
		  epoch-zero date as unread.
		*/
		dto.put("blnIsRead", n.getDteReadOn() != null);
		return dto;
	}

	/**
	 * The session's user, or null.
	 *
	 * <p>
	 * Never throws. A bell that 500s when nobody is signed in takes the whole
	 * page down with it, and the page is often the sign-in page.
	 */
	private static Long readerId() {
		try {
			Integer id = ServiceCurrentUser.getCurrentUserId();
			return id == null ? null : id.longValue();
		} catch (Exception e) {
			return null;
		}
	}
}
