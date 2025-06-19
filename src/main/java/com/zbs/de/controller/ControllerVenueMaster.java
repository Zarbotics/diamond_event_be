package com.zbs.de.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.util.ResponseMessage;
import com.zbs.de.model.dto.DtoVenueMaster;
import com.zbs.de.service.ServiceVenueMaster;


@RestController
@RequestMapping("/venueMaster")
public class ControllerVenueMaster {
	@Autowired
	private ServiceVenueMaster serviceVenueMaster;

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoVenueMaster dto) {
		return serviceVenueMaster.saveOrUpdate(dto);
	}

	@PostMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAll() {
		return serviceVenueMaster.getAllVenues();
	}

	@PostMapping(value = "/getByCityId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByCityId(@RequestBody Integer cityId) {
		return serviceVenueMaster.getVenuesByCityId(cityId);
	}

	@PostMapping(value = "/getAllGroupedByCity", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllGroupedByCity() {
		return serviceVenueMaster.getAllVenuesGroupedByCity();
	}
}
