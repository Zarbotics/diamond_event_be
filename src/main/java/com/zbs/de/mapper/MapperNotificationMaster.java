package com.zbs.de.mapper;

import com.zbs.de.model.NotificationMaster;
import com.zbs.de.model.dto.DtoNotificationMaster;
import com.zbs.de.util.UtilDateAndTime;

public class MapperNotificationMaster {

	public static DtoNotificationMaster toDto(NotificationMaster entity) {
		if (entity == null)
			return null;

		DtoNotificationMaster dto = new DtoNotificationMaster();
		dto.setId(entity.getId());

		if (entity.getUserMaster() != null) {
			dto.setSerUserId(entity.getUserMaster().getSerUserId().intValue());
			dto.setTxtUserName(entity.getUserMaster().getTxtName());
		}

		dto.setTxtTitle(entity.getTxtTitle());
		dto.setTxtMessage(entity.getTxtMessage());
		dto.setTxtTargetUrl(entity.getTxtTargetUrl());
		dto.setTxtType(entity.getTxtType());
		dto.setBlnIsRead(entity.getBlnIsRead());

		if (entity.getCreatedAt() != null) {
			dto.setCreatedAt(UtilDateAndTime.dateToStringddmmyyyy(entity.getCreatedAt()));
		}

		return dto;
	}

	public static NotificationMaster toEntity(DtoNotificationMaster dto) {
		if (dto == null)
			return null;

		NotificationMaster entity = new NotificationMaster();
		entity.setId(dto.getId());
		entity.setTxtTitle(dto.getTxtTitle());
		entity.setTxtMessage(dto.getTxtMessage());
		entity.setTxtTargetUrl(dto.getTxtTargetUrl());
		entity.setTxtType(dto.getTxtType());
		entity.setBlnIsRead(dto.getBlnIsRead());

		return entity;
	}
}
