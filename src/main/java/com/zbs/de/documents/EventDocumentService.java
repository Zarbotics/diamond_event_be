package com.zbs.de.documents;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
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

	/**
	 * The faces the PDF is set in, and their weights.
	 *
	 * <p>
	 * A PDF renderer has no font stack to fall back on the way a browser does:
	 * anything not registered here resolves to one of the fourteen faces every
	 * PDF reader is required to have, which in practice is Helvetica. The
	 * document asked for a font it was never given and got Helvetica silently.
	 *
	 * <p>
	 * Inter alone, rather than the journey's Inter-and-Fraunces pairing:
	 * Fraunces ships as a variable font and PDFBox cannot embed one, so a
	 * second face would mean vendoring a static cut of it into this repository.
	 * Inter at 600 carries the headings, and one embedded family keeps the file
	 * a few hundred kilobytes rather than a megabyte.
	 */
	private static final String[][] FONTS = {
			{ "/fonts/inter/Inter-Regular.ttf", "400" },
			{ "/fonts/inter/Inter-Medium.ttf", "500" },
			{ "/fonts/inter/Inter-SemiBold.ttf", "600" },
			{ "/fonts/inter/Inter-Bold.ttf", "700" },
	};

	/** The same document, rendered to PDF. */
	public byte[] renderPdf(Integer eventId) {
		return pdfFrom(renderHtml(eventId));
	}

	/**
	 * The one place HTML becomes a PDF.
	 *
	 * <p>
	 * Package-private and static so the render test drives <em>this</em> rather
	 * than assembling a second {@code PdfRendererBuilder} of its own. It used to
	 * do exactly that, which is why nothing caught the document rendering
	 * without fonts: the test's copy of the pipeline was configured differently
	 * from the one customers download from, and the test's copy was the one
	 * being asserted on.
	 */
	static byte[] pdfFrom(String html) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();

			for (String[] font : FONTS) {
				final String path = font[0];
				builder.useFont(() -> EventDocumentService.class.getResourceAsStream(path), "Inter",
						Integer.valueOf(font[1]), BaseRendererBuilder.FontStyle.NORMAL, true);
			}

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
