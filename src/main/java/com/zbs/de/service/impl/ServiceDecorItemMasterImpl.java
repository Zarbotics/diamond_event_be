package com.zbs.de.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.service.ServiceDecorCategoryMaster;
import com.zbs.de.service.ServiceDecorItemMaster;
import com.zbs.de.util.UtilFileStorage;
import com.zbs.de.mapper.MapperDecorItemMaster;
import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.DecorItemMaster;
import com.zbs.de.model.DecorReferenceDocument;
import com.zbs.de.model.dto.DtoDecorItemMaster;
import com.zbs.de.model.dto.DtoDecorReferenceDocument;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorItemMaster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Service("serviceDecorItemMaster")
public class ServiceDecorItemMasterImpl implements ServiceDecorItemMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDecorItemMasterImpl.class);

	@Autowired
	RepositoryDecorItemMaster repositoryDecorItemMaster;

	@Autowired
	ServiceDecorCategoryMaster serviceDecorCategoryMaster;

	@Override
	public DtoResult saveOrUpdate(DtoDecorItemMaster dto) {
		DecorItemMaster entity = MapperDecorItemMaster.toEntity(dto);
		repositoryDecorItemMaster.save(entity);
		return new DtoResult("Saved Successfully", null, MapperDecorItemMaster.toDto(entity), null);
	}

	@Override
	public DtoResult getAll() {
		List<DtoDecorItemMaster> list = repositoryDecorItemMaster.findAll().stream().map(MapperDecorItemMaster::toDto)
				.collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getById(Integer id) {
		Optional<DecorItemMaster> optional = repositoryDecorItemMaster.findById(id);
		if (optional.isPresent()) {
			return new DtoResult("Found", null, MapperDecorItemMaster.toDto(optional.get()), null);
		}
		return new DtoResult("Not Found", null, null, null);
	}

	@Override
	public DtoResult deleteById(Integer id) {
		repositoryDecorItemMaster.deleteById(id);
		return new DtoResult("Deleted", null, null, null);
	}

	@Override
	public DtoResult saveDecorItemWithDocuments(DtoDecorItemMaster dto, List<MultipartFile> files) throws IOException {
		DtoResult dtoResult = new DtoResult();
		try {
			DecorItemMaster entity = new DecorItemMaster();
			entity.setTxtDecorName(dto.getTxtDecorName());
			entity.setTxtDecorCode(dto.getTxtDecorCode());
			entity.setBlnIsColorRequired(dto.getBlnIsColorRequired());
			entity.setBlnIsCountRequired(dto.getBlnIsCountRequired());
			entity.setBlnIsImageRequired(dto.getBlnIsImageRequired());
			entity.setBlnIsActive(dto.getBlnIsActive());

			if (dto.getSerDecorCategoryId() != null) {
				DecorCategoryMaster category = serviceDecorCategoryMaster.getByPK(dto.getSerDecorCategoryId());
				entity.setDecorCategoryMaster(category);
			}

			Map<String, MultipartFile> fileMap = files.stream()
					.collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f));

			List<DecorReferenceDocument> docs = new ArrayList<>();

			if (dto.getDtoDecorReferenceDocumentLst() != null) {
				for (DtoDecorReferenceDocument docDto : dto.getDtoDecorReferenceDocumentLst()) {
					MultipartFile file = fileMap.get(docDto.getTxtOriginalName());
					if (file != null) {
						String uploadPath = UtilFileStorage.saveFile(file, "decor");
						DecorReferenceDocument doc = new DecorReferenceDocument();
						doc.setDocumentName(file.getName());
						doc.setOriginalName(file.getOriginalFilename());
						doc.setDocumentType(file.getContentType());
						doc.setSize(String.valueOf(file.getSize()));
						doc.setFilePath(uploadPath);
						doc.setDecorItemMaster(entity);
						docs.add(doc);
					}
				}
			}

			entity.setDecorReferenceDocuments(docs);
			repositoryDecorItemMaster.save(entity);

			dtoResult.setTxtMessage("Saved Successfully");
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());

		}
		return dtoResult;

	}

}