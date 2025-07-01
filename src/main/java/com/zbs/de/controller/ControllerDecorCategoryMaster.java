package com.zbs.de.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.model.dto.DtoDecorCategoryMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceDecorCategoryMaster;
import com.zbs.de.util.ResponseMessage;

@RestController
@RequestMapping("/decorCategory")
public class ControllerDecorCategoryMaster {

	@Autowired
	private ServiceDecorCategoryMaster serviceDecorCategoryMaster;

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoDecorCategoryMaster dto) {
		DtoResult result = serviceDecorCategoryMaster.saveOrUpdate(dto);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), result.getResult());
	}

	@PostMapping(value = "/getAll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAll(@RequestBody DtoDecorCategoryMaster dto) {
		DtoResult result = serviceDecorCategoryMaster.getAll();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), result.getResulList());
	}

	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch search) {
		DtoResult result = serviceDecorCategoryMaster.getById(search.getId());
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), result.getResult());
	}

	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch search) {
		DtoResult result = serviceDecorCategoryMaster.deleteById(search.getId());
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
	}
}