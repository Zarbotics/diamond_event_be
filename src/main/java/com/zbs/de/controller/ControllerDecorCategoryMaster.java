package com.zbs.de.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.dto.DtoDecorCategoryMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceDecorCategoryMaster;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/decorCategoryMaster")
@CrossOrigin("")
public class ControllerDecorCategoryMaster {

	@Autowired
	ServiceDecorCategoryMaster serviceDecorCategoryMaster;

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoDecorCategoryMaster dto) {
		DtoResult result = serviceDecorCategoryMaster.saveOrUpdate(dto);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), result.getResult());
	}

	@PostMapping(value = "/getAllData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllData() {
		DtoResult result = serviceDecorCategoryMaster.getAll();
		if (result.getResulList() != null && !result.getResulList().isEmpty()) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					result.getResulList());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "No data found", null);
	}

	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch) {
		DtoResult result = serviceDecorCategoryMaster.getById(dtoSearch.getId());
		if (result.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, result.getTxtMessage(), null);
	}

	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch dtoSearch) {
		DtoResult result = serviceDecorCategoryMaster.deleteById(dtoSearch.getId());
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
	}

	@PostMapping(value = "/saveOrUpdateWithFiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveWithFiles(@RequestPart("data") String jsonData,
			@RequestPart(value = "documents", required = false) MultipartFile[] documents, HttpServletRequest request) {
		try {
			// Parse JSON string into DTO
			ObjectMapper mapper = new ObjectMapper();
			DtoDecorCategoryMaster dto = mapper.readValue(jsonData, DtoDecorCategoryMaster.class);

			DtoResult result = serviceDecorCategoryMaster.saveWithDocuments(dto, documents);

			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved successfully",
						result.getResult());
			} else {
				return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
						result.getTxtMessage(), null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to parse or save data", null);
		}
	}
	
	@PostMapping(value = "/getAllDecorMasterData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllDecorMasterData() {
//		DtoResult result = serviceDecorCategoryMaster.getAll();
//		if (result.getResulList() != null && !result.getResulList().isEmpty()) {
//			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
//					result.getResulList());
//		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "No data found", null);
	}
}