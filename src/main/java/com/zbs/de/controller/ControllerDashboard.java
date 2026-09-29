package com.zbs.de.controller;

import com.zbs.de.model.dto.DtoBusinessSummary;
import com.zbs.de.model.dto.DtoDashboard;
import com.zbs.de.service.impl.ServiceDashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class ControllerDashboard {

	@Autowired
	private ServiceDashboard service;

	@GetMapping("/summary")
	public ResponseEntity<DtoDashboard> getSummary() {
		DtoDashboard dto = service.getAnalyticsSummary();
		return ResponseEntity.ok(dto);
	}

	/**
	 * The pipeline, the year ahead, and what is waiting on somebody.
	 *
	 * <p>
	 * Separate from {@code /summary}, which answers how many customers and
	 * events were created this month against last. Those are creation counts;
	 * this is the business. See {@link DtoBusinessSummary}.
	 */
	@GetMapping("/business-summary")
	public ResponseEntity<DtoBusinessSummary> getBusinessSummary() {
		return ResponseEntity.ok(service.getBusinessSummary());
	}
}
