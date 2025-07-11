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
import com.zbs.de.model.dto.DtoDecorCategoryReferenceDocument;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceDecorCategoryReferenceDocument;
import com.zbs.de.util.ResponseMessage;

@RequestMapping("/decorCategoryReferenceDocument")
@CrossOrigin(origins = "")
@RestController
public class ControllerDecorCategoryReferenceDocument {

	@Autowired
	ServiceDecorCategoryReferenceDocument serviceDecorCategoryReferenceDocument;

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveOrUpdate(@RequestPart("decorDoc") String jsonData,
			@RequestPart("file") MultipartFile file) {
		try {
			DtoDecorCategoryReferenceDocument dto = new ObjectMapper().readValue(jsonData,
					DtoDecorCategoryReferenceDocument.class);
			dto.setDocumentFile(file.getBytes());

			DtoResult result = serviceDecorCategoryReferenceDocument.saveOrUpdate(dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to parse or save document", null);
		}
	}

	@PostMapping(value = "/getByCategoryId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByCategoryId(@RequestBody DtoSearch dtoSearch) {
		DtoResult result = serviceDecorCategoryReferenceDocument.getByCategoryId(dtoSearch.getId());
		if (result.getResulList() != null && !result.getResulList().isEmpty()) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					result.getResulList());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "No documents found", null);
	}

	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch dtoSearch) {
		DtoResult result = serviceDecorCategoryReferenceDocument.deleteById(dtoSearch.getId());
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
	}
}