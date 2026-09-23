package com.zbs.de.documents;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.config.security.AccessGuard;

/**
 * The customer's event document.
 *
 * <p>
 * Two representations of one thing:
 *
 * <ul>
 * <li>{@code /documents/event/{id}} — HTML. Readable on a phone, which a
 * multi-page A4 PDF is not, and the natural thing to link from a confirmation
 * email.</li>
 * <li>{@code /documents/event/{id}/pdf} — the same document as a PDF, for
 * saving, printing and attaching.</li>
 * </ul>
 *
 * <p>
 * Both assert ownership. The reports these replace were reachable at
 * {@code /report/**}, which sat in the security chain's permit-all list, so any
 * customer's full event summary could be downloaded by anyone who guessed an
 * event id.
 */
@RestController
@RequestMapping("/documents")
public class ControllerEventDocument {

	private final EventDocumentService documentService;
	private final AccessGuard accessGuard;

	public ControllerEventDocument(EventDocumentService documentService, AccessGuard accessGuard) {
		this.documentService = documentService;
		this.accessGuard = accessGuard;
	}

	@GetMapping(value = "/event/{eventId}", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> html(@PathVariable Integer eventId) {
		accessGuard.assertCanAccessEvent(eventId);

		return ResponseEntity.ok()
				.contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
				// A customer's booking must never be cached by a shared proxy.
				.header(HttpHeaders.CACHE_CONTROL, "private, no-store")
				.body(documentService.renderHtml(eventId));
	}

	@GetMapping(value = "/event/{eventId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> pdf(@PathVariable Integer eventId) {
		accessGuard.assertCanAccessEvent(eventId);

		byte[] pdf = documentService.renderPdf(eventId);

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CACHE_CONTROL, "private, no-store")
				// `attachment` rather than `inline`: this is served from the API origin,
				// and anything rendered inline there is a stored-XSS surface.
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(documentService.filenameFor(eventId), StandardCharsets.UTF_8)
						.build().toString())
				.body(pdf);
	}
}
