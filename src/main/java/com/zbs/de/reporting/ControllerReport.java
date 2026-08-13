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

import com.zbs.de.config.security.AccessGuard;

/**
 * Generated PDF reports.
 *
 * <p>
 * These endpoints were previously matched by a {@code /report/**} entry in the
 * security chain's permit-all list, which made every report — including the
 * kitchen itinerary and the full client summary — downloadable by anyone who
 * could guess an event id, with no authentication at all. They are now
 * authenticated, and the customer-facing summary additionally checks that the
 * caller owns the event.
 */
@RestController
@RequestMapping("report")
@CrossOrigin("")
public class ControllerReport {

	@Autowired
	private ServiceReport reportService;

	@Autowired
	private AccessGuard accessGuard;

//	@GetMapping("/event/{eventId}")
//	public ResponseEntity<byte[]> getEventReport(@PathVariable Integer eventId) throws Exception {
//		byte[] pdfBytes = reportService.generateEventReport(eventId);
//
//		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=event_report.pdf")
//				.contentType(MediaType.APPLICATION_PDF).body(pdfBytes);
//	}
	
	@GetMapping("/event/{eventId}")
	public ResponseEntity<byte[]> getEventReport(@PathVariable Integer eventId) throws Exception {
		byte[] pdfBytes = reportService.generateNewItineraryReport(eventId);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=event_report.pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdfBytes);
	}
	
//	@GetMapping("/eventClientSide/{eventId}")
//	public ResponseEntity<byte[]> getEventReportClientSide(@PathVariable Integer eventId) throws Exception {
//		byte[] pdfBytes = reportService.generateEventReportClientSide(eventId);
//
//		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=event_summary_client_side.pdf")
//				.contentType(MediaType.APPLICATION_PDF).body(pdfBytes);
//	}

	@GetMapping("/eventClientSide/{eventId}")
	public ResponseEntity<byte[]> getEventReportClientSide(@PathVariable Integer eventId) throws Exception {
		// The only report a customer may pull, and only for their own event.
		accessGuard.assertCanAccessEvent(eventId);
		byte[] pdfBytes = reportService.generateNewCustomeReport(eventId);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=event_summary_client_side.pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdfBytes);
	}
	
	
	@GetMapping("/kitchen_itinerary/{eventId}")
	public ResponseEntity<byte[]> getEventReportKitchenItinerary(@PathVariable Integer eventId) throws Exception {
		byte[] pdfBytes = reportService.generateKitchenItineraryReport(eventId);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=event_summary_client_side.pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdfBytes);
	}

}
