package com.zbs.de.service.impl;

import com.zbs.de.model.PricingRule;
import com.zbs.de.model.dto.DtoPricingRule;
import com.zbs.de.model.PriceVersion;
import com.zbs.de.controller.ControllerDashboard;
import com.zbs.de.mapper.MapperPricingRule;
import com.zbs.de.repository.RepositoryPricingRule;
import com.zbs.de.repository.RepositoryPriceVersion;
import com.zbs.de.service.ServicePricingRule;
import com.zbs.de.util.exception.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("servicePricingRuleImpl")
public class ServicePricingRuleImpl implements ServicePricingRule {

	@Autowired
	private RepositoryPricingRule repo;

	@Autowired
	private RepositoryPriceVersion versionRepo;

	@Override
	@Transactional
	public DtoPricingRule create(DtoPricingRule dto) {
		PricingRule entity = MapperPricingRule.toEntity(dto);
		if (dto.getPriceVersionId() != null) {
			PriceVersion pv = versionRepo.findById(dto.getPriceVersionId())
					.orElseThrow(() -> new NotFoundException("PriceVersion not found"));
			entity.setPriceVersion(pv);
		}
		repo.save(entity);
		return MapperPricingRule.toDto(entity);
	}

	@Override
	@Transactional
	public DtoPricingRule update(Long id, DtoPricingRule dto) {
		PricingRule exist = repo.findById(id).orElseThrow(() -> new NotFoundException("PricingRule not found"));
		exist.setExpression(dto.getExpression());
		exist.setNumPriority(dto.getNumPriority());
		exist.setBlnStackable(dto.getBlnStackable());
		exist.setTxtLabel(dto.getTxtLabel());
		repo.save(exist);
		return MapperPricingRule.toDto(exist);
	}

	@Override
	public void delete(Long id) {
		repo.deleteById(id);
	}

	@Override
	public List<DtoPricingRule> listByVersion(Long priceVersionId) {
		return repo.findByPriceVersion_SerPriceVersionIdOrderByNumPriorityAsc(priceVersionId).stream()
				.map(MapperPricingRule::toDto).collect(Collectors.toList());
	}
}
