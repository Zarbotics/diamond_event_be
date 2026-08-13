package com.zbs.de.documents;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.service.ServiceEventMaster;
import com.zbs.de.util.exception.NotFoundException;

/**
 * Builds the customer's event document, as HTML or PDF.
 *
 * <p>
 * Reads through {@link ServiceEventMaster} rather than issuing its own SQL.
 * That matters beyond tidiness: the JasperReports pipeline this replaces
 * embedded queries in its templates and filled them from a raw JDBC
 * {@code Connection}, so it saw the database directly and no service-layer or
 * authorisation rule applied to what it returned.
 */
@Service
public class EventDocumentService {

	private static final String TEMPLATE = "documents/event-document";

	private final ServiceEventMaster serviceEventMaster;
	private final EventDocumentAssembler assembler;
	private final TemplateEngine templateEngine;

	public EventDocumentService(ServiceEventMaster serviceEventMaster, EventDocumentAssembler assembler,
			TemplateEngine templateEngine) {
		this.serviceEventMaster = serviceEventMaster;
		this.assembler = assembler;
		this.templateEngine = templateEngine;
	}

	/** The document as HTML — what the browser shows, and what the PDF is made from. */
	public String renderHtml(Integer eventId) {
		DtoEventMaster event = serviceEventMaster.getEventById(eventId);
		if (event == null) {
			throw new NotFoundException("We could not find that booking.");
		}

		Context context = new Context();
		context.setVariable("doc", assembler.assemble(event));
		return templateEngine.process(TEMPLATE, context);
	}

	/** The same document, rendered to PDF. */
	public byte[] renderPdf(Integer eventId) {
		String html = renderHtml(eventId);

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withHtmlContent(html, null);
			builder.toStream(out);
			builder.run();
			return out.toByteArray();
		} catch (Exception e) {
			throw new IllegalStateException("Could not produce the document PDF", e);
		}
	}

	/** A filename a customer will recognise in their downloads folder. */
	public String filenameFor(Integer eventId) {
		DtoEventMaster event = serviceEventMaster.getEventById(eventId);
		String reference = event == null || event.getTxtEventMasterCode() == null ? String.valueOf(eventId)
				: event.getTxtEventMasterCode();
		return "diamond-events-" + reference.toLowerCase().replaceAll("[^a-z0-9-]", "-") + ".pdf";
	}

	byte[] utf8(String value) {
		return value.getBytes(StandardCharsets.UTF_8);
	}
}
