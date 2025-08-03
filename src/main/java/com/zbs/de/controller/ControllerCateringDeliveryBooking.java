package com.zbs.de.controller;

import com.zbs.de.model.dto.DtoCateringDeliveryBooking;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceCateringDeliveryBooking;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cateringDelivery")
@CrossOrigin(origins = "*")
public class ControllerCateringDeliveryBooking {

	@Autowired
	private ServiceCateringDeliveryBooking service;

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseMessage> saveOrUpdate(@RequestBody DtoCateringDeliveryBooking dto,
			HttpServletRequest request) {
		return ResponseEntity.ok(new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved successfully",
				service.saveOrUpdate(dto).getResult()));
	}

	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseMessage> getById(@RequestBody DtoCateringDeliveryBooking dto,
			HttpServletRequest request) {
		Integer id = dto.getSerDeliveryBookingId();
		return ResponseEntity.ok(new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
				service.getByPK(id).getResult()));
	}

	@PostMapping(value = "/getAll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseMessage> getAll(@RequestBody DtoCateringDeliveryBooking dto,
			HttpServletRequest request) {
		return ResponseEntity.ok(new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched all successfully",
				service.getAll().getResult()));
	}

	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseMessage> deleteById(@RequestBody DtoSearch dtoSearch) {
		Integer id = dtoSearch.getId();
		return ResponseEntity.ok(new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Deleted successfully",
				service.softDelete(id).getResult()));
	}

	@PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseMessage> search(@RequestBody DtoSearch dtoSearch) {
		String keyword = dtoSearch.getSearchKeyword();
		return ResponseEntity.ok(new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Search results",
				service.search(keyword).getResult()));
	}

	@PostMapping(value = "/generateCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseMessage> generateCode(HttpServletRequest request) {
		return ResponseEntity.ok(new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Code generated",
				service.generateAutoCode()));
	}
}