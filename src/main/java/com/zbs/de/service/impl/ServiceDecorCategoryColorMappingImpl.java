package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.mapper.MapperDecoreCategoryColorMapping;
import com.zbs.de.model.DecorCategoryColorMapping;
import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.DecorColorMaster;
import com.zbs.de.model.dto.DtoDecorCategoryColorMapping;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorCategoryColorMapping;
import com.zbs.de.service.ServiceDecorCategoryColorMapping;
import com.zbs.de.service.ServiceDecorCategoryMaster;
import com.zbs.de.service.ServiceDecorColorMaster;
import com.zbs.de.util.UtilRandomKey;

@Service("serviceDecorCategoryColorMappingImpl")
public class ServiceDecorCategoryColorMappingImpl implements ServiceDecorCategoryColorMapping {

	@Autowired
	private RepositoryDecorCategoryColorMapping repositoryDecorCategoryColorMapping;

	@Autowired
	private ServiceDecorColorMaster serviceDecorColorMaster;

	@Autowired
	ServiceDecorCategoryMaster serviceDecorCategoryMaster;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	@Override
	public DtoResult saveOrUpdate(DtoDecorCategoryColorMapping dto) {
		DtoResult result = new DtoResult();
		try {
			DecorCategoryColorMapping entity = new DecorCategoryColorMapping();

			if (dto.getSerMappingId() != null)
				entity.setSerMappingId(dto.getSerMappingId());

			// Set Decore Category
			// *******************
			DecorColorMaster decorColorMaster = serviceDecorColorMaster.getByPK(dto.getSerDecorColorId());
			if (UtilRandomKey.isNotNull(decorColorMaster)) {
				entity.setDecorColorMaster(decorColorMaster);
			} else {
				result.setTxtMessage("No Color Exists Against Color Id: " + dto.getSerDecorColorId());
				return result;
			}

			DecorCategoryMaster decorCategoryMaster = serviceDecorCategoryMaster.getByPK(dto.getSerDecorCategoryId());
			if (UtilRandomKey.isNotNull(decorCategoryMaster)) {
				entity.setDecorCategory(decorCategoryMaster);
			} else {
				result.setTxtMessage("No Category Exists Against Category Id: " + dto.getSerDecorColorId());
				return result;
			}

			repositoryDecorCategoryColorMapping.save(entity);

			result.setTxtMessage("Saved successfully");
			result.setResult(entity);
		} catch (Exception e) {
			result.setTxtMessage("Error: " + e.getMessage());
		}
		return result;
	}

	@Override
	public DtoResult getAll() {
		DtoResult result = new DtoResult();

		try {
			List<DecorCategoryColorMapping> list = repositoryDecorCategoryColorMapping.findAll();

			if (UtilRandomKey.isNotNull(list)) {
				List<DtoDecorCategoryColorMapping> dtoDecorCategoryColorMappings = new ArrayList<>();
				for (DecorCategoryColorMapping entity : list) {
					DtoDecorCategoryColorMapping dto = MapperDecoreCategoryColorMapping.toDto(entity);
					dtoDecorCategoryColorMappings.add(dto);
				}
				result.setResulList(new ArrayList<>(dtoDecorCategoryColorMappings));
				result.setTxtMessage("Fetched successfully");
			} else {
				result.setTxtMessage("No Data Found.");
			}
			return result;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			result.setTxtMessage(e.getMessage());
			return result;
		}

	}

	@Override
	public DtoResult getById(Integer id) {
		DtoResult result = new DtoResult();
		DecorCategoryColorMapping entity = repositoryDecorCategoryColorMapping.findById(id).orElse(null);
		if (entity != null) {
			result.setResult(entity);
			result.setTxtMessage("Found");
		} else {
			result.setTxtMessage("Not found");
		}
		return result;
	}

	@Override
	public DtoResult deleteById(Integer id) {
		DtoResult result = new DtoResult();
		try {
			repositoryDecorCategoryColorMapping.deleteById(id);
			result.setTxtMessage("Deleted successfully");
		} catch (Exception e) {
			result.setTxtMessage("Error deleting: " + e.getMessage());
		}
		return result;
	}

	@Override
	public DtoResult getByCategoryId(Integer id) {
		DtoResult dtoResult = new DtoResult();

		try {
			if (id == null) {
				dtoResult.setTxtMessage("Category ID must not be null");
				return dtoResult;
			}

			// Fetch mappings by category ID
			List<DecorCategoryColorMapping> mappings = repositoryDecorCategoryColorMapping
					.findByDecorCategory_SerDecorCategoryIdAndBlnIsDeletedFalse(id);

			if (mappings == null || mappings.isEmpty()) {
				dtoResult.setTxtMessage("No color mappings found for the given category");
				return dtoResult;
			}

			// Convert to DTOs
			List<DtoDecorCategoryColorMapping> dtos = mappings.stream().map(MapperDecoreCategoryColorMapping::toDto)
					.collect(Collectors.toList());

			dtoResult.setTxtMessage("Fetched Successfully");
			dtoResult.setResulList(dtos.stream().map(d -> (Object) d).collect(Collectors.toList()));
		} catch (Exception e) {
			LOGGER.error("Error fetching mappings for category ID: {}", id, e);
			dtoResult.setTxtMessage("Error occurred while fetching color mappings: " + e.getMessage());
		}

		return dtoResult;
	}

}
