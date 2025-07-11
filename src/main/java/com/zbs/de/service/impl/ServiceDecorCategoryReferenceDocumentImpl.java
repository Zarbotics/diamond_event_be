package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.mapper.MapperDecorCategoryReferenceDocument;
import com.zbs.de.model.DecorCategoryReferenceDocument;
import com.zbs.de.model.dto.DtoDecorCategoryReferenceDocument;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorCategoryReferenceDocument;
import com.zbs.de.service.ServiceDecorCategoryReferenceDocument;
import com.zbs.de.util.UtilRandomKey;

@Service("serviceDecorCategoryReferenceDocument")
public class ServiceDecorCategoryReferenceDocumentImpl implements ServiceDecorCategoryReferenceDocument {

	@Autowired
	RepositoryDecorCategoryReferenceDocument repositoryDecorCategoryReferenceDocument;

	@Override
	public DtoResult saveOrUpdate(DtoDecorCategoryReferenceDocument dto) {
		DecorCategoryReferenceDocument entity = MapperDecorCategoryReferenceDocument.toEntity(dto);
		repositoryDecorCategoryReferenceDocument.save(entity);
		return new DtoResult("Saved Successfully", null, MapperDecorCategoryReferenceDocument.toDto(entity), null);
	}

	@Override
	public DtoResult getAll() {
		List<DtoDecorCategoryReferenceDocument> list = repositoryDecorCategoryReferenceDocument
				.findAllByBlnIsDeletedFalse().stream().map(MapperDecorCategoryReferenceDocument::toDto)
				.collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getByCategoryId(Integer categoryId) {
		List<DtoDecorCategoryReferenceDocument> list = repositoryDecorCategoryReferenceDocument
				.findByDecorCategoryMasterSerDecorCategoryIdAndBlnIsDeletedFalse(categoryId).stream()
				.map(MapperDecorCategoryReferenceDocument::toDto).collect(Collectors.toList());
		return new DtoResult("Fetched by Category ID", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getById(Integer id) {
		Optional<DecorCategoryReferenceDocument> optional = repositoryDecorCategoryReferenceDocument
				.findByIdAndBlnIsDeletedFalse(id);
		return optional
				.map(entity -> new DtoResult("Found", null, MapperDecorCategoryReferenceDocument.toDto(entity), null))
				.orElseGet(() -> new DtoResult("Not Found", null, null, null));
	}

	@Override
	public DtoResult deleteById(Integer id) {
		Optional<DecorCategoryReferenceDocument> optional = repositoryDecorCategoryReferenceDocument
				.findByIdAndBlnIsDeletedFalse(id);
		if (optional.isPresent()) {
			DecorCategoryReferenceDocument entity = optional.get();
			entity.setBlnIsDeleted(true);
			repositoryDecorCategoryReferenceDocument.save(entity);
			return new DtoResult("Deleted (soft) successfully", null, null, null);
		}
		return new DtoResult("No record found to delete", null, null, null);
	}

	@Override
	public DtoResult saveAll(List<DecorCategoryReferenceDocument> decorCategoryReferenceDocuments) {
		if (UtilRandomKey.isNotNull(decorCategoryReferenceDocuments)) {
			for (DecorCategoryReferenceDocument doc : decorCategoryReferenceDocuments) {
				if (UtilRandomKey.isNotNull(doc.getDecorCategoryMaster())
						&& UtilRandomKey.isNotNull(doc.getDecorCategoryMaster().getSerDecorCategoryId())) {
					repositoryDecorCategoryReferenceDocument.save(doc);
				} else {
					return new DtoResult(
							"Document: " + doc.getOriginalName() + " Don't have The Refrence Of Decor Categor.", null,
							null, null);
				}
			}

			return new DtoResult("Success", null, null, null);
		}
		return new DtoResult("No Data Found", null, null, null);
	}

}
