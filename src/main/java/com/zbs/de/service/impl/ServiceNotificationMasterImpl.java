package com.zbs.de.service.impl;

import com.zbs.de.mapper.MapperNotificationMaster;
import com.zbs.de.model.NotificationMaster;
import com.zbs.de.model.UserMaster;
import com.zbs.de.model.dto.DtoNotificationMaster;
import com.zbs.de.repository.RepositoryNotificationMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.service.ServiceNotificationMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service("serviceNotificationMaster")
public class ServiceNotificationMasterImpl implements ServiceNotificationMaster {

	private final Map<Long, SseEmitter> clients = new ConcurrentHashMap<>();

	@Autowired
	private RepositoryNotificationMaster notificationRepo;

	@Autowired
	private RepositoryUserMaster userRepo;

	/**
	 * Registers an SSE connection for a logged-in user.
	 */
	@Override
	public SseEmitter registerClient(Long userId) {
		SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
		clients.put(userId, emitter);

		emitter.onCompletion(() -> clients.remove(userId));
		emitter.onTimeout(() -> clients.remove(userId));

		return emitter;
	}

	/**
	 * Push a notification to a specific user via SSE.
	 */
	@Override
	public void sendNotification(Long userId, DtoNotificationMaster notification) {
		SseEmitter emitter = clients.get(userId);
		if (emitter != null) {
			try {
				emitter.send(notification);
			} catch (IOException e) {
				clients.remove(userId);
			}
		}
	}

	@Override
	public DtoNotificationMaster createNotification(Long userId, String title, String message, String targetUrl,
			String type) {
		NotificationMaster notification = null;
		List<UserMaster> userMasterLst = userRepo.getAdminUsersForEmail();
		if (userMasterLst != null && !userMasterLst.isEmpty()) {
			for (UserMaster user : userMasterLst) {
				notification = new NotificationMaster();
				notification.setTxtTitle(title);
				notification.setTxtMessage(message);
				notification.setTxtTargetUrl(targetUrl);
				notification.setTxtType(type);
				notification.setBlnIsRead(false);
				notification.setUserMaster(user);
				notification = notificationRepo.save(notification);
				sendNotification(user.getSerUserId(), MapperNotificationMaster.toDto(notification));
			}

		}

		return MapperNotificationMaster.toDto(notification);
	}

	@Override
	public Page<DtoNotificationMaster> getUserNotifications(Long userId, Pageable pageable) {
		UserMaster user = userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

		return notificationRepo.findByUserMasterOrderByCreatedAtDesc(user, pageable)
				.map(MapperNotificationMaster::toDto);
	}

	@Override
	public List<DtoNotificationMaster> getUnreadNotifications(Long userId) {
		UserMaster user = userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

		return notificationRepo.findByUserMasterAndBlnIsReadFalseOrderByCreatedAtDesc(user).stream()
				.map(MapperNotificationMaster::toDto).collect(Collectors.toList());
	}

	@Override
	public void markAsRead(Long notificationId, Long userId) {
		NotificationMaster notification = notificationRepo.findById(notificationId)
				.orElseThrow(() -> new EntityNotFoundException("Notification not found"));

		if (!notification.getUserMaster().getSerUserId().equals(userId)) {
			throw new SecurityException("Not allowed to mark other user's notification as read");
		}

		notification.setBlnIsRead(true);
		notificationRepo.save(notification);
	}

	@Override
	public long getUnreadCount(Long userId) {
		UserMaster user = userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

		return notificationRepo.countByUserMasterAndBlnIsReadFalse(user);
	}

	// Send heartbeat every 15 seconds to keep connection alive
	@Scheduled(fixedRate = 60000) // 15 seconds
	public void sendHeartbeats() {
		clients.forEach((userId, emitter) -> {
			try {
				emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
			} catch (IOException e) {
				clients.remove(userId);
			}
		});
	}
}
