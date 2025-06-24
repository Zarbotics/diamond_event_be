package com.zbs.de.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.service.ServiceEventMaster;
import com.zbs.de.util.ResponseMessage;

@RestController
@RequestMapping("/eventMaster")
@CrossOrigin(origins = "")
public class ControllerEventMaster {

	@Autowired
	ServiceEventMaster serviceEventMaster;

//	@PostMapping(value = "/getAllData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseMessage getAllData() {
//		List<DtoEventType> list = serviceEventMaster.getAllData();
//		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Event Types", list);
//	}

	@PostMapping(value = "/generateEventCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateEventCode() {
		String txtCode = serviceEventMaster.generateNextEventMasterCode();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Event Types", txtCode);
	}

}
