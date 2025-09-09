package com.zbs.de.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventMasterAdminPortal;
import com.zbs.de.model.dto.DtoEventMasterStats;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;

public interface ServiceEventMaster {

	DtoResult saveAndUpdate(DtoEventMaster dtoEventMaster);

	DtoResult getByEventTypeIdAndCustId(DtoSearch dtoSearch);

	DtoResult getByCustId(DtoSearch dtoSearch);

	String generateNextEventMasterCode();

	DtoResult getAllEvents();

	List<DtoEventMasterStats> getEventTypeStats();

	DtoResult saveAndUpdateWithDocs(DtoEventMaster dtoEventMaster, List<MultipartFile> files) throws IOException;

	DtoResult deleteById(Integer id);

	DtoResult getAllEventsTableView();

	DtoResult saveAndUpdateWithDocsAdminPortal(DtoEventMasterAdminPortal dtoEventMasterAdminPortal,
			List<MultipartFile> files) throws IOException;

	DtoResult getAllEventsAdminPortal();
}
