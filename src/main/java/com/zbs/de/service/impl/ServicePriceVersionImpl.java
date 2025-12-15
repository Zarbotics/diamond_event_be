package com.zbs.de.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.mapper.MapperPriceVersion;
import com.zbs.de.model.PriceVersion;
import com.zbs.de.model.dto.DtoPriceVersion;
import com.zbs.de.repository.RepositoryPriceVersion;
import com.zbs.de.service.ServicePriceVersion;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.enums.EnmPriceVersionStatus;
import com.zbs.de.util.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicePriceVersionImpl implements ServicePriceVersion {

	@Autowired
	private RepositoryPriceVersion repo;

	@Override
	@Transactional
	public DtoPriceVersion create(DtoPriceVersion dto) {
		PriceVersion entity = MapperPriceVersion.toEntity(dto);
		if (entity.getPriceVersionStatus() == null)
			entity.setPriceVersionStatus(EnmPriceVersionStatus.DRAFT);
		repo.save(entity);
		return MapperPriceVersion.toDto(entity);
	}

	@Override
	@Transactional
	public DtoPriceVersion update(Long id, DtoPriceVersion dto) {
		PriceVersion exist = repo.findById(id).orElseThrow(() -> new NotFoundException("PriceVersion not found"));
		exist.setTxtName(dto.getTxtName());
		if (dto.getDteEffectiveFrom() != null) {
			exist.setDteEffectiveFrom(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteEffectiveFrom()));
		}
		if (dto.getDteEffectiveTo() != null) {
			exist.setDteEffectiveTo(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteEffectiveTo()));
		}
		exist.setPriceVersionStatus(EnmPriceVersionStatus.of(dto.getTxtStatus()));
		repo.save(exist);
		return MapperPriceVersion.toDto(exist);
	}

	@Override
	public void delete(Long id) {
		PriceVersion exist = repo.findById(id).orElseThrow(() -> new NotFoundException("PriceVersion not found"));
		if (EnmPriceVersionStatus.PUBLISHED.name().equalsIgnoreCase(exist.getPriceVersionStatus().name())) {
			throw new IllegalStateException("Cannot delete published version");
		}
		repo.deleteById(id);
	}

	@Override
	public DtoPriceVersion getById(Long id) {
		return repo.findById(id).map(MapperPriceVersion::toDto)
				.orElseThrow(() -> new NotFoundException("PriceVersion not found"));
	}

	@Override
	public List<DtoPriceVersion> listAll() {
		return repo.findAll().stream().map(MapperPriceVersion::toDto).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public DtoPriceVersion publish(Long id) {
		PriceVersion toPublish = repo.findById(id).orElseThrow(() -> new NotFoundException("PriceVersion not found"));
		repo.findAll().stream()
				.filter(v -> !v.getSerPriceVersionId().equals(id)
						&& EnmPriceVersionStatus.PUBLISHED.name().equalsIgnoreCase(v.getPriceVersionStatus().name()))
				.forEach(v -> {
					v.setPriceVersionStatus(EnmPriceVersionStatus.RETIRED);
					repo.save(v);
				});

		toPublish.setPriceVersionStatus(EnmPriceVersionStatus.PUBLISHED);
		repo.save(toPublish);
		return MapperPriceVersion.toDto(toPublish);
	}
}
