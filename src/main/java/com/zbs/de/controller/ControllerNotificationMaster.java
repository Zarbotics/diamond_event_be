package com.zbs.de.controller;

import com.zbs.de.model.dto.DtoNotificationMaster;
import com.zbs.de.service.ServiceNotificationMaster;
import com.zbs.de.service.impl.ServiceCurrentUser;
import com.zbs.de.util.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "")
public class ControllerNotificationMaster {

	@Autowired
	private ServiceNotificationMaster notificationService;

	@PostMapping("/create")
	public ResponseMessage createNotification(@RequestBody DtoNotificationMaster dto) {
		try {
			DtoNotificationMaster saved = notificationService.createNotification(dto.getSerUserId().longValue(),
					dto.getTxtTitle(), dto.getTxtMessage(), dto.getTxtTargetUrl(), dto.getTxtType());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Notification created", saved);
		} catch (Exception e) {
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to create notification: " + e.getMessage(), null);
		}
	}

	// Get Paginated Notifications for Logged-in User
	@PostMapping("/getAll")
	public ResponseMessage getNotifications(Principal principal, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		try {
			Long userId = ServiceCurrentUser.getCurrentUserId().longValue();
			var notifications = notificationService.getUserNotifications(userId, PageRequest.of(page, size));
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched notifications", notifications);
		} catch (Exception e) {
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch notifications: " + e.getMessage(), null);
		}
	}

	// Get Unread Notifications
	@PostMapping("/unread")
	public ResponseMessage getUnreadNotifications(Principal principal) {
		try {
			Long userId = ServiceCurrentUser.getCurrentUserId().longValue();
			List<DtoNotificationMaster> unreadList = notificationService.getUnreadNotifications(userId);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched unread notifications",
					unreadList);
		} catch (Exception e) {
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch unread notifications: " + e.getMessage(), null);
		}
	}

	// ✅ Mark Notification as Read (Request Body instead of Request Param for
	// consistency)
	@PostMapping("/markAsRead")
	public ResponseMessage markAsRead(@RequestBody Map<String, Long> body) {
		try {
			Long notificationId = body.get("notificationId");
			Long userId = ServiceCurrentUser.getCurrentUserId().longValue();
			notificationService.markAsRead(notificationId, userId);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Notification marked as read", null);
		} catch (SecurityException se) {
			return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN, se.getMessage(), null);
		} catch (Exception e) {
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to mark notification as read: " + e.getMessage(), null);
		}
	}

	// ✅ Get Unread Count
	@GetMapping("/unreadCount")
	public ResponseMessage getUnreadCount() {
		try {
			Long userId = ServiceCurrentUser.getCurrentUserId().longValue();
			long count = notificationService.getUnreadCount(userId);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched unread count", count);
		} catch (Exception e) {
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch unread count: " + e.getMessage(), null);
		}
	}

	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter streamNotifications() {
		Long userId = ServiceCurrentUser.getCurrentUserId().longValue();
		return notificationService.registerClient(userId);
	}
}
