package com.zbs.de.service;

import com.zbs.de.model.dto.DtoNotificationMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface ServiceNotificationMaster {
	DtoNotificationMaster createNotification(Long userId, String title, String message, String targetUrl, String type);
	
	void sendNotification(Long userId, DtoNotificationMaster notification);

	Page<DtoNotificationMaster> getUserNotifications(Long userId, Pageable pageable);

	List<DtoNotificationMaster> getUnreadNotifications(Long userId);

	void markAsRead(Long notificationId, Long userId);

	long getUnreadCount(Long userId);

	SseEmitter registerClient(Long userId);
}
