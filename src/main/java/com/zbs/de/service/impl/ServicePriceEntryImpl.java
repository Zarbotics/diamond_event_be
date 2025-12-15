package com.zbs.de.service.impl;

import com.zbs.de.model.PriceEntry;
import com.zbs.de.model.PriceVersion;
import com.zbs.de.model.dto.DtoPriceEntry;
import com.zbs.de.mapper.MapperPriceEntry;
import com.zbs.de.repository.RepositoryPriceEntry;
import com.zbs.de.repository.RepositoryPriceVersion;
import com.zbs.de.service.ServicePriceEntry;
import com.zbs.de.util.enums.EnmApplyScope;
import com.zbs.de.util.enums.EnmItineraryUnitType;
import com.zbs.de.util.enums.EnmPriceCalculationMethod;
import com.zbs.de.util.exception.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("servicePriceEntryImpl")
public class ServicePriceEntryImpl implements ServicePriceEntry {

	@Autowired
	private RepositoryPriceEntry repo;
	@Autowired
	private RepositoryPriceVersion versionRepo;

	@Override
	@Transactional
	public DtoPriceEntry create(DtoPriceEntry dto) {
		PriceEntry e = MapperPriceEntry.toEntity(dto);
		if (dto.getPriceVersionId() != null) {
			PriceVersion pv = versionRepo.findById(dto.getPriceVersionId())
					.orElseThrow(() -> new NotFoundException("PriceVersion not found"));
			e.setPriceVersion(pv);
		}
		repo.save(e);
		return MapperPriceEntry.toDto(e);
	}

	@Override
	@Transactional
	public DtoPriceEntry update(Long id, DtoPriceEntry dto) {
		PriceEntry exist = repo.findById(id).orElseThrow(() -> new NotFoundException("PriceEntry not found"));
		exist.setNumPrice(dto.getNumPrice());
		exist.setTxtCurrency(dto.getTxtCurrency());
		exist.setNumMinQuantity(dto.getNumMinQuantity());
		exist.setNumMaxQuantity(dto.getNumMaxQuantity());
		exist.setMetadata(dto.getMetadata());
		exist.setUnit(EnmItineraryUnitType.of(dto.getTxtUnit()));
		exist.setCalculationMethod(EnmPriceCalculationMethod.of(dto.getTxtCalculationMethod()));
		exist.setApplyScope(EnmApplyScope.of(dto.getTxtApplyScope()));
		exist.setNumTargetId(dto.getNumTargetId());
		repo.save(exist);
		return MapperPriceEntry.toDto(exist);
	}

	@Override
	public void delete(Long id) {
		repo.deleteById(id);
	}

	@Override
	public List<DtoPriceEntry> listByVersion(Long priceVersionId) {
		return repo.findByPriceVersionSerPriceVersionId(priceVersionId).stream().map(MapperPriceEntry::toDto)
				.collect(Collectors.toList());
	}
}
