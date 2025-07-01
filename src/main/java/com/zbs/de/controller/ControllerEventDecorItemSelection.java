package com.zbs.de.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.model.dto.DtoEventDecorItemSelection;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceEventDecorItemSelection;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/eventDecorItemSelection")
@CrossOrigin("")
public class ControllerEventDecorItemSelection {
	@Autowired
	private ServiceEventDecorItemSelection serviceEventDecorItemSelection;

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(HttpServletRequest request, @RequestBody DtoEventDecorItemSelection dto) {
		DtoResult result = serviceEventDecorItemSelection.saveOrUpdate(dto);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), result.getResult());
	}

	@PostMapping(value = "/getAll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAll(HttpServletRequest request) {
		DtoResult result = serviceEventDecorItemSelection.getAll();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), result.getResulList());
	}

	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(HttpServletRequest request, @RequestBody DtoSearch search) {
		DtoResult result = serviceEventDecorItemSelection.getById(search.getId());
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), result.getResult());
	}

	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(HttpServletRequest request, @RequestBody DtoSearch search) {
		DtoResult result = serviceEventDecorItemSelection.deleteById(search.getId());
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
	}
}
