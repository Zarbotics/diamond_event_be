package com.zbs.de.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.VenueMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoVenueMaster;
import com.zbs.de.util.ResponseMessage;

public interface ServiceVenueMaster {
	ResponseMessage saveOrUpdate(DtoVenueMaster dto);

	ResponseMessage getAllVenues();

	ResponseMessage getAllVenuesGroupedByCity();

	ResponseMessage getVenuesByCityId(Integer cityId);

	DtoResult saveVenueWithDetails(DtoVenueMaster dto, List<MultipartFile> files) throws IOException;

	VenueMaster getByPK(Integer serVenueMasterId);

	DtoResult getVenueByVenueMasterDetailId(Integer venueMasterDetailId);
	
	DtoResult getVenueDetailByVenueMasterDetailId(Integer venueMasterDetailId);
}
