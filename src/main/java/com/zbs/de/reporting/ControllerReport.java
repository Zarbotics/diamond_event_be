package com.zbs.de.reporting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("report")
@CrossOrigin("")
public class ControllerReport {

	@Autowired
	private ServiceReport reportService;

	@GetMapping("/event/{eventId}")
	public ResponseEntity<byte[]> getEventReport(@PathVariable Integer eventId) throws Exception {
		byte[] pdfBytes = reportService.generateEventReport(eventId);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=event_report.pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdfBytes);
	}
	
	@GetMapping("/eventClientSide/{eventId}")
	public ResponseEntity<byte[]> getEventReportClientSide(@PathVariable Integer eventId) throws Exception {
		byte[] pdfBytes = reportService.generateEventReportClientSide(eventId);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=event_summary_client_side.pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdfBytes);
	}

}
