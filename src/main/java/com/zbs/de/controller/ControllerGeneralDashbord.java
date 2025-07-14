package com.zbs.de.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.model.dto.DtoGenralDashboard;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceGeneralDashbord;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.UtilRandomKey;


@RestController
@RequestMapping("/dashboardStats")
@CrossOrigin(origins = "")
public class ControllerGeneralDashbord {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	@Autowired
	private ServiceGeneralDashbord serviceGeneralDashbord;

	@PostMapping(value = "/getCustomerAndEventYearReport", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Saving EventType: {}", dtoSearch);
		List<DtoGenralDashboard> dtoGetnDashboardLst = serviceGeneralDashbord
				.getYearlyCustomerEventReport(dtoSearch.getId());
		if (UtilRandomKey.isNotNull(dtoGetnDashboardLst)) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", dtoGetnDashboardLst);
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save", dtoSearch);
	}

}
