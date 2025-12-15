package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoPriceVersion;

public interface ServicePriceVersion {
	DtoPriceVersion create(DtoPriceVersion dto);

	DtoPriceVersion update(Long id, DtoPriceVersion dto);

	void delete(Long id);

	DtoPriceVersion getById(Long id);

	List<DtoPriceVersion> listAll();

	DtoPriceVersion publish(Long id);
}
