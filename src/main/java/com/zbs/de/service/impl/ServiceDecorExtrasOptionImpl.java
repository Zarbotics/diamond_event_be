package com.zbs.de.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.mapper.MapperDecorExtrasMaster;
import com.zbs.de.model.DecorExtrasMaster;
import com.zbs.de.model.DecorExtrasOption;
import com.zbs.de.model.DecorExtrasOptionDocument;
import com.zbs.de.model.dto.DtoDecorExtrasOption;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorExtrasOption;
import com.zbs.de.service.ServiceDecorExtrasMaster;
import com.zbs.de.service.ServiceDecorExtrasOption;
import com.zbs.de.service.ServiceDecorExtrasOptionDocument;
import com.zbs.de.util.UtilFileStorage;
import com.zbs.de.util.UtilRandomKey;

@Service("serviceDecorExtrasOption")
public class ServiceDecorExtrasOptionImpl implements ServiceDecorExtrasOption {

	@Autowired
	private RepositoryDecorExtrasOption repositoryDecorExtrasOption;

	@Autowired
	private ServiceDecorExtrasMaster serviceDecorExtrasMaster;

	@Autowired
	private ServiceDecorExtrasOptionDocument serviceDecorExtrasOptionDocument;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	@Override
	public DecorExtrasOption getByIdAndNotDeleted(Integer id) {
		try {
			return repositoryDecorExtrasOption.findByIdAndNotDeleted(id).orElse(null);
		} catch (Exception e) {
			LOGGER.debug("Failed to get DecorExtrasOption by ID {}: {}", id, e.getMessage());
			return null;
		}
	}

	@Override
	public DtoResult saveExtrasOptionWithDoc(DtoDecorExtrasOption dto, List<MultipartFile> files) {
		DtoResult dtoResult = new DtoResult();
		try {
			DecorExtrasMaster decorExtrasMaster = null;
			if (UtilRandomKey.isNotNull(dto) && UtilRandomKey.isNotNull(dto.getSerExtrasId())) {
				decorExtrasMaster = serviceDecorExtrasMaster.getByPk(dto.getSerExtrasId());
				if (UtilRandomKey.isNull(decorExtrasMaster)) {
					dtoResult.setTxtMessage("Invalid Extras ID: " + dto.getSerExtrasId());
					return dtoResult;
				}
			}

			Map<String, MultipartFile> fileMap = files.stream()
					.collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f));

			Optional<DecorExtrasOption> option = null;
			DecorExtrasOption decorExtrasOption = null;
			if (dto.getSerExtraOptionId() != null) {
				option = repositoryDecorExtrasOption.findByIdAndNotDeleted(dto.getSerExtraOptionId());
				if (option == null || option.isEmpty()) {
					dtoResult.setTxtMessage("Invalid Extras Option ID: " + dto.getSerExtraOptionId());
					return dtoResult;
				}
			}

			if (option == null || option.isEmpty()) {
				decorExtrasOption = new DecorExtrasOption();
				decorExtrasOption.setSerExtraOptionId(dto.getSerExtraOptionId());
				decorExtrasOption.setTxtOptionCode(dto.getTxtOptionCode());
				decorExtrasOption.setTxtOptionName(dto.getTxtOptionName());
				decorExtrasOption.setBlnIsActive(Boolean.valueOf(true));
				decorExtrasOption.setDecorExtrasMaster(decorExtrasMaster);
				decorExtrasOption.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
				decorExtrasOption.setBlnIsApproved(true);
				decorExtrasMaster.setBlnIsDeleted(false);

			} else {
				decorExtrasOption = option.get();
				decorExtrasOption.setTxtOptionCode(dto.getTxtOptionCode());
				decorExtrasOption.setTxtOptionName(dto.getTxtOptionName());
				decorExtrasOption.setBlnIsActive(dto.getBlnIsActive());
				decorExtrasOption.setDecorExtrasMaster(decorExtrasMaster);
				decorExtrasOption.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
			}

			// *******Saving Document********
			if (dto.getDocument() != null && dto.getDocument().getOriginalName() != null) {
				MultipartFile file = fileMap.get(dto.getDocument().getOriginalName());
				if (file != null) {
					String uploadPath = UtilFileStorage.saveFile(file, "extrasOption");
					DecorExtrasOptionDocument doc = new DecorExtrasOptionDocument();
					doc.setDocumentName(file.getName());
					doc.setOriginalName(file.getOriginalFilename());
					doc.setDocumentType(file.getContentType());
					doc.setSize(String.valueOf(file.getSize()));
					doc.setFilePath(uploadPath);
					doc = serviceDecorExtrasOptionDocument.save(doc);
					decorExtrasOption.setDocument(doc);
					decorExtrasOption.setBlnIsDocument(Boolean.TRUE);
				}
			}

			repositoryDecorExtrasOption.save(decorExtrasOption);

			dtoResult.setTxtMessage("Success");

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
		}
		return dtoResult;

	}

	@Override
	public DtoResult getAll() {
		DtoResult dtoResult = new DtoResult();
		try {
			List<DecorExtrasOption> extras = repositoryDecorExtrasOption.findAllNotDeleted();
			List<DtoDecorExtrasOption> dtoList = extras.stream().map(MapperDecorExtrasMaster::toDto)
					.collect(Collectors.toList());
			dtoResult.setTxtMessage("Success");
			dtoResult.setResult(dtoList);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Failed to retrieve extras options");
		}
		return dtoResult;
	}

}
