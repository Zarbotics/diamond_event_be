package com.zbs.de.service;

import java.util.Date;

import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.price.DtoPriceVersion;
import com.zbs.de.model.PriceVersion;

public interface ServicePriceVersion {

	// CRUD Operations
	DtoResult create(DtoPriceVersion dto);

	DtoResult update(DtoPriceVersion dto);

	DtoResult delete(Long id);

	DtoResult getById(Long id);

	DtoResult getAll();

	DtoResult getAllActive();

	DtoResult getDefault();

	// Special Operations
	DtoResult activate(Long id);

	DtoResult deactivate(Long id);

	DtoResult setAsDefault(Long id);

	DtoResult duplicate(Long sourceId, String newName);

	// Query Operations
	DtoResult getActiveForDate(Date date);

	DtoResult getByCode(String versionCode);

	// Helper Methods
	PriceVersion getPriceVersionEntityById(Long id);

	String generateVersionCode();

	PriceVersion getActivePriceVersionForDate(Date date);
	
	String generatePriceVersionCode();
	
	DtoResult getAllPriceVersionStatusValues();
}