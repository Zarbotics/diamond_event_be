package com.zbs.de.service.impl;

import org.springframework.stereotype.Service;

import com.zbs.de.service.ServiceMenuFoodMaster;
import com.zbs.de.mapper.MapperMenuFoodMaster;
import com.zbs.de.model.MenuFoodMaster;
import com.zbs.de.model.dto.DtoMenuFoodMaster;
import com.zbs.de.repository.RepositoryMenuFoodMaster;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.UtilRandomKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Service("serviceMenuFoodMaster")
public class ServiceMenuFoodMasterImpl implements ServiceMenuFoodMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMenuFoodMasterImpl.class);

	@Autowired
	RepositoryMenuFoodMaster repositoryMenuFoodMaster;

	@Override
	public List<DtoMenuFoodMaster> getAllData() {
		List<MenuFoodMaster> list = repositoryMenuFoodMaster.findByBlnIsDeleted(false);
		List<DtoMenuFoodMaster> dtos = new ArrayList<>();
		for (MenuFoodMaster item : list) {
			dtos.add(MapperMenuFoodMaster.toDto(item));
		}
		return dtos;
	}

	@Override
	public ResponseMessage saveAndUpdate(DtoMenuFoodMaster dto) {
		ResponseMessage res = new ResponseMessage();
		try {
			MenuFoodMaster entity = MapperMenuFoodMaster.toEntity(dto);
			entity.setBlnIsActive(true);
			entity.setBlnIsDeleted(false);
			entity.setBlnIsApproved(true);
			entity = repositoryMenuFoodMaster.saveAndFlush(entity);
			res.setMessage("Saved successfully");
			res.setResult(entity);
		} catch (Exception e) {
			LOGGER.error("Error saving MenuFoodMaster", e);
			res.setMessage("Unexpected error occurred while saving");
		}
		return res;
	}

	@Override
	public ResponseMessage getById(Integer id) {
		ResponseMessage res = new ResponseMessage();
		try {
			Optional<MenuFoodMaster> optional = repositoryMenuFoodMaster.findById(id);
			if (optional.isPresent()) {
				res.setMessage("Record fetched successfully");
				res.setResult(optional.get());
			} else {
				res.setMessage("MenuFoodMaster not found");
			}
		} catch (Exception e) {
			LOGGER.error("Error fetching by ID", e);
			res.setMessage("Unexpected error occurred");
		}
		return res;
	}

	@Override
	public ResponseMessage getByType(String type) {
		ResponseMessage res = new ResponseMessage();
		try {
			List<MenuFoodMaster> menuFoodMasterLSt = repositoryMenuFoodMaster.findByFoodType(type);
			if (UtilRandomKey.isNotNull(menuFoodMasterLSt)) {
				res.setMessage("Record fetched successfully");
				res.setResult(menuFoodMasterLSt);
			} else {
				res.setMessage("MenuFoodMaster not found");
			}
		} catch (Exception e) {
			LOGGER.error("Error fetching by Type", e);
			res.setMessage("Unexpected error occurred");
		}
		return res;
	}

	@Override
	public MenuFoodMaster getByPK(Integer id) {
		try {
			Optional<MenuFoodMaster> optional = repositoryMenuFoodMaster.findById(id);
			if (optional.isPresent()) {
				return optional.get();
			} else {
				return null;
			}
		} catch (Exception e) {
			LOGGER.error("Error fetching by ID", e);
			return null;
		}
	}

	@Override
	public Map<String, List<DtoMenuFoodMaster>> getAllFoodGroupedByType() {
		Map<String, List<DtoMenuFoodMaster>> grouped = new HashMap<>();
		try {
			List<MenuFoodMaster> allItems = repositoryMenuFoodMaster.findByBlnIsDeleted(false);
			for (MenuFoodMaster item : allItems) {
				if (Boolean.TRUE.equals(item.getBlnIsDessert())) {
					grouped.computeIfAbsent("Dessert", k -> new ArrayList<>()).add(MapperMenuFoodMaster.toDto(item));
				}
				if (Boolean.TRUE.equals(item.getBlnIsDrink())) {
					grouped.computeIfAbsent("Drink", k -> new ArrayList<>()).add(MapperMenuFoodMaster.toDto(item));
				}
				if (Boolean.TRUE.equals(item.getBlnIsStarter())) {
					grouped.computeIfAbsent("Starter", k -> new ArrayList<>()).add(MapperMenuFoodMaster.toDto(item));
				}
				if (Boolean.TRUE.equals(item.getBlnIsAppetiser())) {
					grouped.computeIfAbsent("Appetiser", k -> new ArrayList<>()).add(MapperMenuFoodMaster.toDto(item));
				}
				if (Boolean.TRUE.equals(item.getBlnIsSaladAndCondiment())) {
					grouped.computeIfAbsent("SaladAndCondiment", k -> new ArrayList<>())
							.add(MapperMenuFoodMaster.toDto(item));
				}
				if (Boolean.TRUE.equals(item.getBlnIsMainCourse())) {
					grouped.computeIfAbsent("MainCourse", k -> new ArrayList<>()).add(MapperMenuFoodMaster.toDto(item));
				}
			}

			return grouped;
		} catch (Exception e) {
			LOGGER.error("Error fetching grouped food items", e);
			return null;
		}
	}

}