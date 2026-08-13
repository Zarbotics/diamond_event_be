package com.zbs.de.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventRunningOrder;

/**
 * Renders the customer document end to end — template, assembler and PDF
 * engine — with no database, servlet or Spring context.
 *
 * <p>
 * The JasperReports templates this replaces could not be exercised at all
 * without a live database, because their queries lived inside the report.
 */
class EventDocumentRenderTest {

	private final EventDocumentAssembler assembler = new EventDocumentAssembler();

	/**
	 * A SpringTemplateEngine, not a plain one.
	 *
	 * <p>
	 * Plain Thymeleaf evaluates expressions with OGNL while Spring Boot
	 * autoconfigures SpringEL. Testing against the plain engine would exercise a
	 * different expression language from the one that runs in production, so this
	 * builds the Spring engine over an empty context — which needs no database and
	 * no component scan.
	 */
	private TemplateEngine engine() {
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCharacterEncoding("UTF-8");

		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(resolver);
		engine.setEnableSpringELCompiler(false);
		return engine;
	}

	private String render(DtoEventMaster event) {
		Context context = new Context();
		context.setVariable("doc", assembler.assemble(event));
		return engine().process("documents/event-document", context);
	}

	private DtoEventMaster fullEvent() {
		DtoEventMaster event = new DtoEventMaster();
		event.setSerEventMasterId(42);
		event.setTxtEventMasterCode("EVT-014");
		event.setTxtEventTypeName("Walima");
		event.setDteEventDate("15-08-2026");
		event.setNumNumberOfGuests(400);
		event.setNumNumberOfTables(40);
		event.setTxtBrideFirstName("Aisha");
		event.setTxtBrideLastName("Khan");
		event.setTxtGroomFirstName("Bilal");
		event.setTxtGroomLastName("Ahmed");
		event.setTxtVenueName("Diamond Suite");
		event.setTxtContactPersonFirstName("Aisha");
		event.setTxtContactPersonPhoneNo("07700 900123");
		event.setTxtCateringRemarks("Two guests have a severe nut allergy.");

		DtoEventRunningOrder ro = new DtoEventRunningOrder();
		ro.setTxtGuestArrival("2026-08-15T17:00:00");
		ro.setTxtMeal("19:30");
		ro.setTxtSpeeches("21:00");
		ro.setTxtEndOfNight("00:30");
		event.setDtoEventRunningOrder(ro);

		return event;
	}

	@Test
	@DisplayName("renders the customer's details, reference and running order")
	void rendersContent() {
		String html = render(fullEvent());

		assertThat(html).contains("EVT-014");
		assertThat(html).contains("Aisha Khan &amp; Bilal Ahmed");
		assertThat(html).contains("Saturday 15 August 2026");
		assertThat(html).contains("Diamond Suite");
		assertThat(html).contains("400");
		assertThat(html).contains("Two guests have a severe nut allergy.");
	}

	@Test
	@DisplayName("running order is in the order the day happens, in 24-hour time")
	void runningOrderIsChronologicalAnd24Hour() {
		String html = render(fullEvent());

		assertThat(html).contains("17:00").contains("19:30").contains("21:00").contains("00:30");

		// Ordered by the ceremony sequence, not by field order on the DTO.
		assertThat(html.indexOf("Guest arrival")).isLessThan(html.indexOf("Meal"));
		assertThat(html.indexOf("Meal")).isLessThan(html.indexOf("Speeches"));
		assertThat(html.indexOf("Speeches")).isLessThan(html.indexOf("End of night"));
	}

	@Test
	@DisplayName("an empty enquiry still produces a complete, non-broken document")
	void emptyEventStillRenders() {
		String html = render(new DtoEventMaster());

		// No blank cover, no "null" leaking into the page.
		assertThat(html).contains("Your event");
		assertThat(html).doesNotContain(">null<");
		// Empty sections explain themselves rather than sitting blank.
		assertThat(html).contains("No timings agreed yet");
		assertThat(html).contains("No dishes chosen yet");
	}

	@Test
	@DisplayName("an unparseable date is omitted rather than guessed")
	void badDateIsOmittedNotGuessed() {
		DtoEventMaster event = fullEvent();
		event.setDteEventDate("not-a-date");

		String html = render(event);

		// The previous implementation substituted the current year here, turning a
		// data problem into a confidently wrong date on the customer's document.
		assertThat(html).doesNotContain(String.valueOf(java.time.Year.now().getValue()) + "</p>");
	}

	/** Renders to PDF and reads the text back out, as a reader would see it. */
	private byte[] toPdf(String html) throws Exception {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withHtmlContent(html, null);
			builder.toStream(out);
			builder.run();
			return out.toByteArray();
		}
	}

	@Test
	@DisplayName("produces a real multi-page PDF whose text reads back correctly")
	void producesPdf() throws Exception {
		byte[] pdf = toPdf(render(fullEvent()));

		assertThat(pdf).isNotEmpty();
		assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

		try (PDDocument document = Loader.loadPDF(pdf)) {
			// Cover, then content. A one-page document would mean the cover's
			// page-break-after had stopped working.
			assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(2);

			String text = new PDFTextStripper().getText(document);
			assertThat(text).contains("EVT-014");
			assertThat(text).contains("Aisha Khan & Bilal Ahmed");
			assertThat(text).contains("Saturday 15 August 2026");
			assertThat(text).contains("Guest arrival");

			// Accented characters and the pound sign must survive the PDF font
			// encoding — "Décor" arriving as "Dcor" on a customer's document would
			// be both wrong and embarrassing.
			assertThat(text).contains("Décor");
		}

		// Written out so the rendered document can be eyeballed after a test run.
		Path target = Path.of("target", "event-document-sample.pdf");
		Files.createDirectories(target.getParent());
		Files.write(target, pdf);
	}

	@Test
	@DisplayName("customer notes are escaped, not injected into the page")
	void escapesCustomerInput() {
		DtoEventMaster event = fullEvent();
		event.setTxtEventRemarks("<script>alert('x')</script>");

		String html = render(event);

		assertThat(html).doesNotContain("<script>alert");
		assertThat(html).contains("&lt;script&gt;");
	}

	@Test
	@DisplayName("menu and decor sections are omitted when nothing was chosen")
	void omitsEmptyOptionalSections() {
		DtoEventMaster event = fullEvent();
		event.setServicesSelections(List.of());
		event.setExtrasSelections(List.of());

		String html = render(event);

		assertThat(html).doesNotContain("Section five");
	}
}
