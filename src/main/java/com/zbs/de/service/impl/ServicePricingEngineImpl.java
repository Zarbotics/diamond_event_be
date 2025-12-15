package com.zbs.de.service.impl;

import com.zbs.de.model.PriceEntry;
import com.zbs.de.model.PricingRule;
import com.zbs.de.model.dto.DtoPreviewSelection;
import com.zbs.de.model.dto.DtoPricePreviewLine;
import com.zbs.de.model.dto.DtoPricePreviewRequest;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.MenuComponent;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.repository.RepositoryPriceEntry;
import com.zbs.de.repository.RepositoryPricingRule;
import com.zbs.de.service.ServicePricingEngine;
import com.zbs.de.repository.RepositoryMenuComponent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicePricingEngineImpl implements ServicePricingEngine {

	@Autowired
	private RepositoryPriceEntry entryRepo;
	@Autowired
	private RepositoryPricingRule ruleRepo;
	@Autowired
	private RepositoryMenuItem menuItemRepo;
	@Autowired
	private RepositoryMenuComponent menuComponentRepo;

	@Override
	@Transactional(readOnly = true)
	public DtoPricePreviewRequest preview(DtoPricePreviewRequest request) {
		if (request == null)
			throw new IllegalArgumentException("request required");
		Long versionId = request.getPriceVersionId();
		List<PriceEntry> entries = entryRepo.findByPriceVersionSerPriceVersionId(versionId);

		Map<Long, List<PriceEntry>> itemEntries = entries.stream()
				.filter(e -> "ITEM".equalsIgnoreCase(e.getApplyScope().name()) && e.getNumTargetId() != null)
				.collect(Collectors.groupingBy(PriceEntry::getNumTargetId));

		// grouped by scope for in-memory filters
		List<PriceEntry> roleEntries = entries.stream().filter(e -> "ROLE".equalsIgnoreCase(e.getApplyScope().name()))
				.collect(Collectors.toList());

		List<PriceEntry> typeEntries = entries.stream().filter(e -> "TYPE".equalsIgnoreCase(e.getApplyScope().name()))
				.collect(Collectors.toList());

		List<PriceEntry> bundleEntries = entries.stream()
				.filter(e -> "BUNDLE".equalsIgnoreCase(e.getApplyScope().name())).collect(Collectors.toList());

		List<PriceEntry> stationEntries = entries.stream()
				.filter(e -> "STATION".equalsIgnoreCase(e.getApplyScope().name())).collect(Collectors.toList());

		List<PricingRule> rules = ruleRepo.findByPriceVersion_SerPriceVersionIdOrderByNumPriorityAsc(versionId);

		List<DtoPricePreviewLine> lines = new ArrayList<>();
		double total = 0.0;
		if (request.getSelections() == null)
			request.setSelections(Collections.emptyList());

		for (DtoPreviewSelection sel : request.getSelections()) {
			DtoPricePreviewLine line = new DtoPricePreviewLine();
			line.setMenuItemId(sel.getMenuItemId());
			double qty = sel.getQty() == null ? 1.0 : sel.getQty();
			line.setQty(qty);

			// 1) override
			if (sel.getUnitPriceOverride() != null) {
				line.setUnitPrice(sel.getUnitPriceOverride());
				line.setLineTotal(qty * line.getUnitPrice());
				line.setAppliedPriceEntryId(null);
				lines.add(line);
				total += line.getLineTotal();
				continue;
			}

			// 2) load menu item (if available)
			MenuItem item = menuItemRepo.findById(sel.getMenuItemId()).orElse(null);

			// 3) ITEM-level
			PriceEntry chosen = null;
			List<PriceEntry> candidates = itemEntries.getOrDefault(sel.getMenuItemId(), Collections.emptyList());
			if (!candidates.isEmpty())
				chosen = candidates.get(0);

			// 4) ROLE-level match (metadata.role == item.txtRole)
			if (chosen == null && item != null) {
				String role = item.getTxtRole();
				if (role != null) {
					chosen = roleEntries.stream().filter(e -> {
						Map<String, Object> md = e.getMetadata();
						return md != null && role.equals(String.valueOf(md.get("role")));
					}).findFirst().orElse(null);
				}
			}

            // 5) TYPE-level match (metadata.type == item.txtType)
            if (chosen == null && item != null) {
                String type = item.getTxtType();
                if (type != null) {
                    chosen = typeEntries.stream()
                            .filter(e -> {
                                Map<String,Object> md = e.getMetadata();
                                return md != null && type.equals(String.valueOf(md.get("type")));
                            }).findFirst().orElse(null);
                }
            }

			// 6) BUNDLE-level: find parent bundles of this item via MenuComponent
			if (chosen == null) {
				List<MenuComponent> parents = menuComponentRepo.findByChildMenuItem_SerMenuItemId(sel.getMenuItemId());
				if (!parents.isEmpty()) {
					Set<Long> parentIds = parents.stream().map(c -> c.getParentMenuItem().getSerMenuItemId())
							.collect(Collectors.toSet());
					chosen = bundleEntries.stream()
							.filter(e -> e.getNumTargetId() != null && parentIds.contains(e.getNumTargetId()))
							.findFirst().orElse(null);
				}
			}

			// 7) STATION-level matching via metadata (if used)
			if (chosen == null && item != null) {
				String station = (item.getMetadata() != null) ? (String) item.getMetadata().get("station") : null;
				if (station != null) {
					chosen = stationEntries.stream().filter(e -> {
						Map<String, Object> md = e.getMetadata();
						return md != null && station.equals(String.valueOf(md.get("station")));
					}).findFirst().orElse(null);
				}
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
