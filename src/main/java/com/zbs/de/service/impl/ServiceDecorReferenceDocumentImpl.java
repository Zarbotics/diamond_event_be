package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.zbs.de.mapper.MapperDecorReferenceDocument;
import com.zbs.de.model.DecorReferenceDocument;
import com.zbs.de.model.dto.DtoDecorReferenceDocument;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorReferenceDocument;
import com.zbs.de.service.ServiceDecorReferenceDocument;

public class ServiceDecorReferenceDocumentImpl implements ServiceDecorReferenceDocument {

	@Autowired
	private RepositoryDecorReferenceDocument repositoryDecorReferenceDocument;

	@Override
	public DtoResult saveOrUpdate(DtoDecorReferenceDocument dto) {
		DecorReferenceDocument entity = MapperDecorReferenceDocument.toEntity(dto);
		repositoryDecorReferenceDocument.save(entity);
		return new DtoResult("Saved Successfully", null, MapperDecorReferenceDocument.toDto(entity), null);
	}

	@Override
	public DtoResult getAll() {
		List<DtoDecorReferenceDocument> list = repositoryDecorReferenceDocument.findAll().stream()
				.map(MapperDecorReferenceDocument::toDto).collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getById(Integer id) {
		Optional<DecorReferenceDocument> optional = repositoryDecorReferenceDocument.findById(id);
		if (optional.isPresent()) {
			return new DtoResult("Found", null, MapperDecorReferenceDocument.toDto(optional.get()), null);
		}
		return new DtoResult("Not Found", null, null, null);
	}

	@Override
	public DtoResult deleteById(Integer id) {
		repositoryDecorReferenceDocument.deleteById(id);
		return new DtoResult("Deleted", null, null, null);
	}

}
