package com.zbs.de.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.zbs.de.model.DecorExtrasMaster;
import com.zbs.de.model.DecorExtrasOption;
import com.zbs.de.model.DecorExtrasOptionDocument;
import com.zbs.de.model.dto.DtoDecorExtrasMaster;
import com.zbs.de.model.dto.DtoDecorExtrasOption;
import com.zbs.de.model.dto.DtoDecorExtrasOptionDocument;

public class MapperDecorExtrasMaster {

	// ------------------------
	// Master Mapping
	// ------------------------

	public static DtoDecorExtrasMaster toDto(DecorExtrasMaster entity) {
		if (entity == null)
			return null;

		DtoDecorExtrasMaster dto = new DtoDecorExtrasMaster();
		dto.setSerExtrasId(entity.getSerExtrasId());
		dto.setTxtExtrasCode(entity.getTxtExtrasCode());
		dto.setTxtExtrasName(entity.getTxtExtrasName());
		dto.setBlnIsActive(entity.getBlnIsActive());
		dto.setNumPrice(entity.getNumPrice());

		if (entity.getDecorExtrasOptions() != null) {
			List<DtoDecorExtrasOption> options = entity.getDecorExtrasOptions().stream()
					.map(MapperDecorExtrasMaster::toDto).collect(Collectors.toList());
			dto.setDecorExtrasOptions(options);
		} else {
			dto.setDecorExtrasOptions(new ArrayList<>());
		}

		return dto;
	}

	public static DecorExtrasMaster toEntity(DtoDecorExtrasMaster dto) {
		if (dto == null)
			return null;

		DecorExtrasMaster entity = new DecorExtrasMaster();
		entity.setSerExtrasId(dto.getSerExtrasId());
		entity.setTxtExtrasCode(dto.getTxtExtrasCode());
		entity.setTxtExtrasName(dto.getTxtExtrasName());
		entity.setBlnIsActive(dto.getBlnIsActive());
		entity.setNumPrice(dto.getNumPrice());


		return entity;
	}

	// ------------------------
	// Option Mapping
	// ------------------------

	public static DtoDecorExtrasOption toDto(DecorExtrasOption entity) {
		if (entity == null)
			return null;

		DtoDecorExtrasOption dto = new DtoDecorExtrasOption();
		dto.setSerExtraOptionId(entity.getSerExtraOptionId());
		dto.setTxtOptionCode(entity.getTxtOptionCode());
		dto.setTxtOptionName(entity.getTxtOptionName());
		dto.setBlnIsDocument(entity.getBlnIsDocument());
		dto.setBlnIsActive(entity.getBlnIsActive());

		if (entity.getDocument() != null) {
			dto.setDocument(toDto(entity.getDocument()));
		}

		return dto;
	}

	public static DecorExtrasOption toEntity(DtoDecorExtrasOption dto) {
		if (dto == null)
			return null;

		DecorExtrasOption entity = new DecorExtrasOption();
		entity.setSerExtraOptionId(dto.getSerExtraOptionId());
		entity.setTxtOptionCode(dto.getTxtOptionCode());
		entity.setTxtOptionName(dto.getTxtOptionName());
		entity.setBlnIsDocument(dto.getBlnIsDocument());
		entity.setBlnIsActive(dto.getBlnIsActive());

		return entity;
	}

	// ------------------------
	// Document Mapping
	// ------------------------

	public static DtoDecorExtrasOptionDocument toDto(DecorExtrasOptionDocument entity) {
		if (entity == null)
			return null;

		DtoDecorExtrasOptionDocument dto = new DtoDecorExtrasOptionDocument();
		dto.setDocumentId(entity.getDocumentId());
		dto.setDocumentName(entity.getDocumentName());
		dto.setDocumentType(entity.getDocumentType());
		dto.setOriginalName(entity.getOriginalName());
		dto.setSize(entity.getSize());
		dto.setTxtDocumentUrl(entity.getFilePath());

		return dto;
	}

	public static DecorExtrasOptionDocument toEntity(DtoDecorExtrasOptionDocument dto) {
		if (dto == null)
			return null;

		DecorExtrasOptionDocument entity = new DecorExtrasOptionDocument();
		entity.setDocumentId(dto.getDocumentId());
		entity.setDocumentName(dto.getDocumentName());
		entity.setDocumentType(dto.getDocumentType());
		entity.setOriginalName(dto.getOriginalName());
		entity.setSize(dto.getSize());
		entity.setFilePath(dto.getTxtDocumentUrl());

		return entity;
	}

}
