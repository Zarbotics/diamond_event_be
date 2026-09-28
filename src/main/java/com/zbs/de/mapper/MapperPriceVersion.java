package com.zbs.de.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zbs.de.model.PriceVersion;
import com.zbs.de.model.dto.price.DtoPriceVersion;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.enums.EnmPriceVersionStatus;

@Component
public class MapperPriceVersion {


	public DtoPriceVersion toDto(PriceVersion entity) {
		if (entity == null) {
			return null;
		}

		DtoPriceVersion dto = new DtoPriceVersion();
		dto.setSerPriceVersionId(entity.getSerPriceVersionId());
		dto.setTxtVersionCode(entity.getTxtVersionCode());
		dto.setTxtName(entity.getTxtName());
		dto.setTxtDescription(entity.getTxtDescription());
		dto.setDteEffectiveFrom(UtilDateAndTime.mmddyyyyDateToString(entity.getDteEffectiveFrom()));
		dto.setDteEffectiveTo(UtilDateAndTime.mmddyyyyDateToString(entity.getDteEffectiveTo()));
		dto.setBlnIsActive(entity.getBlnIsActive());
		dto.setBlnIsDefault(entity.getBlnIsDefault());
		dto.setNumPriority(entity.getNumPriority());
		dto.setMetadata(entity.getMetadata());
		if (entity.getPriceVersionStatus() != null) {
			String status = entity.getPriceVersionStatus().name();
			dto.setTxtPriceVersionStatus(status);
		}

		/*
		 * The two counts here were of menu_item_price rows — the old pricing
		 * engine's table, which holds none and which nothing reads. A version's
		 * prices live in menu_offering_price, which is a different table with a
		 * different shape, so these counts were not merely stale: they answered
		 * a question about a feature that was never used and reported it as
		 * this version's price count.
		 *
		 * Left unset rather than recounted against the live table, because
		 * nothing asks for them — see P4.
		 */

		return dto;
	}

	public PriceVersion toEntity(DtoPriceVersion dto) {
		if (dto == null) {
			return null;
		}

		PriceVersion entity = new PriceVersion();
		entity.setSerPriceVersionId(dto.getSerPriceVersionId());
		entity.setTxtVersionCode(dto.getTxtVersionCode());
		entity.setTxtName(dto.getTxtName());
		entity.setTxtDescription(dto.getTxtDescription());
		entity.setDteEffectiveFrom(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteEffectiveFrom()));
		entity.setDteEffectiveTo(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteEffectiveTo()));
		entity.setBlnIsActive(dto.getBlnIsActive() != null ? dto.getBlnIsActive() : true);
		entity.setBlnIsDefault(dto.getBlnIsDefault() != null ? dto.getBlnIsDefault() : false);
		entity.setNumPriority(dto.getNumPriority() != null ? dto.getNumPriority() : 1);
		entity.setMetadata(dto.getMetadata());

		return entity;
	}

	public void updateEntityFromDto(DtoPriceVersion dto, PriceVersion entity) {
		if (dto == null || entity == null) {
			return;
		}

		if (dto.getTxtName() != null) {
			entity.setTxtName(dto.getTxtName());
		}

		if (dto.getTxtDescription() != null) {
			entity.setTxtDescription(dto.getTxtDescription());
		}

		if (dto.getDteEffectiveFrom() != null) {
			entity.setDteEffectiveFrom(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteEffectiveFrom()));
		}

		if (dto.getDteEffectiveTo() != null) {
			entity.setDteEffectiveTo(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteEffectiveTo()));
		}

		if (dto.getBlnIsActive() != null) {
			entity.setBlnIsActive(dto.getBlnIsActive());
		}

		if (dto.getBlnIsDefault() != null) {
			entity.setBlnIsDefault(dto.getBlnIsDefault());
		}

		if (dto.getNumPriority() != null) {
			entity.setNumPriority(dto.getNumPriority());
		}

		if (dto.getMetadata() != null) {
			entity.setMetadata(dto.getMetadata());
		}
	}

	public List<DtoPriceVersion> toDtoList(List<PriceVersion> entities) {
		if (entities == null) {
			return new ArrayList<>();
		}

		return entities.stream().map(this::toDto).collect(Collectors.toList());
	}

	public List<PriceVersion> toEntityList(List<DtoPriceVersion> dtos) {
		if (dtos == null) {
			return new ArrayList<>();
		}

		return dtos.stream().map(this::toEntity).collect(Collectors.toList());
	}
}
