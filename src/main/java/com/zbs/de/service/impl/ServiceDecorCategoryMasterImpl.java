package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.controller.ControllerEventType;
import com.zbs.de.mapper.MapperDecorCategoryMaster;
import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.DecorCategoryReferenceDocument;
import com.zbs.de.model.dto.DtoDecorCategoryMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorCategoryMaster;
import com.zbs.de.service.ServiceDecorCategoryMaster;
import com.zbs.de.service.ServiceDecorCategoryPropertyMaster;
import com.zbs.de.service.ServiceDecorCategoryReferenceDocument;
import com.zbs.de.util.UtilFileStorage;

@Service("serviceDecorCategoryMasterImpl")
public class ServiceDecorCategoryMasterImpl implements ServiceDecorCategoryMaster {

	@Autowired
	private RepositoryDecorCategoryMaster repositoryDecorCategoryMaster;

	@Autowired
	private ServiceDecorCategoryPropertyMaster serviceDecorCategoryPropertyMaster;

	@Autowired
	private ServiceDecorCategoryReferenceDocument serviceDecorCategoryReferenceDocument;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	@Override
	public DtoResult saveOrUpdate(DtoDecorCategoryMaster dto) {
		DecorCategoryMaster entity = MapperDecorCategoryMaster.toEntity(dto);
		entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
		repositoryDecorCategoryMaster.save(entity);
		return new DtoResult("Saved Successfully", null, MapperDecorCategoryMaster.toDto(entity), null);
	}

	@Override
	public DtoResult getAll() {
		List<DtoDecorCategoryMaster> list = repositoryDecorCategoryMaster.findByBlnIsDeletedFalse().stream()
				.map(MapperDecorCategoryMaster::toMasterDto).collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getAllMasterData() {
		List<DtoDecorCategoryMaster> list = repositoryDecorCategoryMaster.findByBlnIsDeletedFalse().stream()
				.map(MapperDecorCategoryMaster::toDto).collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getById(Integer id) {
		Optional<DecorCategoryMaster> optional = repositoryDecorCategoryMaster.findById(id);
		if (optional.isPresent()) {
			return new DtoResult("Found", null, MapperDecorCategoryMaster.toDto(optional.get()), null);
		}
		return new DtoResult("Not Found", null, null, null);
	}

	@Override
	public DtoResult deleteById(Integer id) {
		DtoResult result = new DtoResult();
		Optional<DecorCategoryMaster> optional = repositoryDecorCategoryMaster.findById(id);
		if (optional.isPresent()) {
			DecorCategoryMaster e = optional.get();
			e.setBlnIsDeleted(true);
			serviceDecorCategoryPropertyMaster.deleteByCategoryId(id);
			serviceDecorCategoryReferenceDocument.deleteByCategoryId(id);
			repositoryDecorCategoryMaster.save(e);
			result.setTxtMessage("Deleted (soft) successfully");
		} else {
			result.setTxtMessage("No record found to delete");
		}
		return result;
	}

	@Override
	public DecorCategoryMaster getByPK(Integer id) {
		try {
			Optional<DecorCategoryMaster> optional = repositoryDecorCategoryMaster.findById(id);
			if (optional.isPresent()) {
				return optional.get();
			} else {
				return null;
			}

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}

	}

	@Override
	public DtoResult saveWithDocuments(DtoDecorCategoryMaster dto, MultipartFile[] documents) {
		try {
//			DecorCategoryMaster entity = MapperDecorCategoryMaster.toEntity(dto);
			Optional<DecorCategoryMaster> decOptional = null;
			DecorCategoryMaster entity = null;
			if (dto.getSerDecorCategoryId() != null) {
				decOptional = this.repositoryDecorCategoryMaster
						.findBySerDecorCategoryIdAndBlnIsDeletedFalse(dto.getSerDecorCategoryId());
				if (decOptional == null || decOptional.isEmpty()) {
					return new DtoResult("No Category Found For the Given Category Id In DB", null, null, null);
				}
			}

			if (decOptional == null || decOptional.isEmpty()) {
				entity = MapperDecorCategoryMaster.toEntity(dto);
				entity.setBlnIsActive(true);
				entity.setBlnIsApproved(true);
				entity.setBlnIsDeleted(false);

			} else {
				entity = decOptional.get();
				entity.setTxtDecorCategoryCode(dto.getTxtDecorCategoryCode());
				entity.setTxtDecorCategoryName(dto.getTxtDecorCategoryName());
				entity.setBlnIsActive(dto.getBlnIsActive());
			}
			List<DecorCategoryReferenceDocument> docEntities = new ArrayList<>();

			if (documents != null && documents.length > 0) {
				for (MultipartFile file : documents) {
					String uploadPath = UtilFileStorage.saveFile(file, "decorCategory");

					DecorCategoryReferenceDocument document = new DecorCategoryReferenceDocument();
					document.setDocumentName(file.getOriginalFilename());
					document.setDocumentType(file.getContentType());
					document.setOriginalName(file.getOriginalFilename());
					document.setSize(String.valueOf(file.getSize()));
					document.setFilePath(uploadPath);
					document.setDecorCategoryMaster(entity);

					docEntities.add(document);
				}
			}

			entity.setReferenceDocuments(docEntities);
			repositoryDecorCategoryMaster.save(entity);

			return new DtoResult("Saved successfully", null, MapperDecorCategoryMaster.toDto(entity), null);

		} catch (Exception e) {
			e.printStackTrace();
			return new DtoResult("Error while saving decor category with documents", null, null, null);
		}
	}

}
