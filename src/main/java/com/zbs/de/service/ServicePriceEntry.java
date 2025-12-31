package com.zbs.de.service;

import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.price.*;


import java.util.List;

public interface ServicePriceEntry {

	// Tree & Filters
	DtoPriceMatrixTree getMenuTreeWithPrices(Long versionId, String roleFilter, String typeFilter);

	DtoPriceMatrixEntries getPriceEntriesByVersion(Long versionId, String scope);

	DtoPriceMatrixFilters getAvailableFilters();

	// Bulk Operations
	DtoBulkAssignResult bulkAssignPrices(Long versionId, DtoBulkAssignRequest request);

	DtoResult bulkUpdatePriceEntries(Long versionId, List<DtoPriceEntryUpdate> updates);

	DtoResult bulkDeletePriceEntries(Long versionId, List<Long> priceEntryIds);

	// Import/Export
	DtoResult exportPriceMatrixToCsv(Long versionId, String scope, Boolean includeMenuInfo);

//	DtoResult importPriceMatrixFromCsv(Long versionId, MultipartFile file, String importMode);

	// Validation & Preview
//	DtoResult validatePriceAssignments(Long versionId, DtoBulkAssignRequest request);

	DtoPricePreviewRequest quickPricePreview(DtoPricePreviewRequest request);
	
	List<DtoPriceEntryGrid> getQuantityBreaksForItem(Long versionId, Long itemId);
}
