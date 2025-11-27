package com.zbs.de.service.impl;

import com.zbs.de.model.PriceEntry;
import com.zbs.de.model.dto.DtoPreviewSelection;
import com.zbs.de.model.dto.DtoPricePreviewLine;
import com.zbs.de.model.dto.DtoPricePreviewRequest;
import com.zbs.de.service.ServicePricingEngine;
import com.zbs.de.repository.RepositoryPriceEntry;
import com.zbs.de.repository.RepositoryPricingRule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("servicePricingEngineImpl")
public class ServicePricingEngineImpl implements ServicePricingEngine {

	@Autowired
	private RepositoryPriceEntry entryRepo;

	@Autowired
	private RepositoryPricingRule ruleRepo;

	@Override
	@Transactional(readOnly = true)
	public DtoPricePreviewRequest preview(DtoPricePreviewRequest request) {
		if (request == null)
			throw new IllegalArgumentException("request required");
		Long versionId = request.getPriceVersionId();
		List<PriceEntry> entries = entryRepo.findByPriceVersionSerPriceVersionId(versionId);

		Map<String, List<PriceEntry>> entriesByScope = entries.stream()
				.collect(Collectors.groupingBy(e -> e.getApplyScope() == null ? "" : e.getApplyScope().name()));

		Map<Long, List<PriceEntry>> itemEntries = entries.stream()
				.filter(e -> "ITEM".equalsIgnoreCase(e.getApplyScope().name()) && e.getNumTargetId() != null)
				.collect(Collectors.groupingBy(PriceEntry::getNumTargetId));

		List<com.zbs.de.model.PricingRule> rules = ruleRepo
				.findByPriceVersion_SerPriceVersionIdOrderByNumPriorityAsc(versionId);

		List<DtoPricePreviewLine> lines = new ArrayList<>();
		double total = 0.0;

		if (request.getSelections() == null)
			request.setSelections(Collections.emptyList());

		for (DtoPreviewSelection sel : request.getSelections()) {
			DtoPricePreviewLine line = new DtoPricePreviewLine();
			line.setMenuItemId(sel.getMenuItemId());
			double qty = sel.getQty() == null ? 1.0 : sel.getQty();
			line.setQty(qty);
			if (sel.getUnitPriceOverride() != null) {
				line.setUnitPrice(sel.getUnitPriceOverride());
				line.setLineTotal(qty * line.getUnitPrice());
				line.setAppliedPriceEntryId(null);
			} else {
				PriceEntry chosen = null;
				List<PriceEntry> candidates = itemEntries.getOrDefault(sel.getMenuItemId(), Collections.emptyList());
				if (!candidates.isEmpty()) {
					chosen = candidates.get(0);
				}

				if (chosen != null) {
					line.setUnitPrice(chosen.getNumPrice());
					line.setAppliedPriceEntryId(chosen.getSerPriceEntryId());
					line.setLineTotal(qty * (line.getUnitPrice() == null ? 0.0 : line.getUnitPrice()));
				} else {
					line.setUnitPrice(0.0);
					line.setLineTotal(0.0);
					line.setAppliedPriceEntryId(null);
				}
			}
			lines.add(line);
			total += (line.getLineTotal() == null ? 0.0 : line.getLineTotal());
		}

		DtoPricePreviewRequest resp = new DtoPricePreviewRequest();
		resp.setPriceVersionId(versionId);
		resp.setGuestCount(request.getGuestCount());
		resp.setLines(lines);
		resp.setTotal(total);

		return resp;
	}
}
