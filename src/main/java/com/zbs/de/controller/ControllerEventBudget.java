package com.zbs.de.controller;

import com.zbs.de.model.dto.DtoEventAnalytics;
import com.zbs.de.model.dto.DtoEventBudget;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.service.ServiceEventBudget;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.UtilRandomKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eventBudget")
@CrossOrigin(origins = "")
public class ControllerEventBudget {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventBudget.class);

	@Autowired
	private ServiceEventBudget serviceEventBudget;

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoEventBudget dtoEventBudget) {
		LOGGER.info("Saving EventBudget: {}", dtoEventBudget);
		DtoResult result = serviceEventBudget.saveOrUpdate(dtoEventBudget);

		if (UtilRandomKey.isNotNull(result) && result.getTxtMessage().equalsIgnoreCase("Success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Event budget saved successfully",
					result.getResult());
		}

		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
				dtoEventBudget);
	}

	@PostMapping(value = "/getByEventId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByEvent(@RequestBody DtoEventBudget dtoEventBudget) {
		LOGGER.info("Fetching EventBudget for eventId: {}", dtoEventBudget.getSerEventMasterId());
		DtoEventBudget dto = serviceEventBudget.getByEventId(dtoEventBudget.getSerEventMasterId());

		if (dto != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched event budget", dto);
		}

		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "Event budget not found", null);
	}
	
	
	@PostMapping(value = "/getAllData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getgetAllDataByEvent() {
		LOGGER.info("Fetching All EventBudget.");
		DtoResult dtoResult = serviceEventBudget.getAllData();
		if (UtilRandomKey.isNotNull(dtoResult) && dtoResult.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched event budget", dtoResult.getResulList());
		}

		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "Event budget not found", null);
	}

	@PostMapping(value = "/monthlySales", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getMonthlySales() {
		LOGGER.info("Fetching Monthly Sales Data...");
		List<DtoEventAnalytics> result = serviceEventBudget.getMonthlySales();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched monthly sales", result);
	}

	@PostMapping(value = "/monthlyProfitByEventType", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getMonthlyProfitByEventType() {
		LOGGER.info("Fetching Monthly Profit by Event Type...");
		List<DtoEventAnalytics> result = serviceEventBudget.getMonthlyProfitByEventType();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched monthly profit by event type",
				result);
	}

	@PostMapping(value = "/summary", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getOverallSummary() {
		LOGGER.info("Fetching Sales Summary...");
		DtoEventAnalytics summary = serviceEventBudget.getOverallSummary();

		if (summary != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched overall summary", summary);
		}

		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "No data found", null);
	}
}