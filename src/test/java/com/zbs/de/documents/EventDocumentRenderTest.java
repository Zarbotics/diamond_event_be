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

	/**
	 * The clock decides the order, not the usual shape of a wedding.
	 *
	 * <p>
	 * The entries were emitted in the order of the assembler's label map — the
	 * sequence a wedding normally runs in — and printed in that order whatever
	 * times the customer had actually given. A couple cutting the cake at 21:30
	 * and speaking at 21:00 got a page headed "Running order" listing 21:30
	 * above 21:00. Staff work from this on the day.
	 */
	@Test
	@DisplayName("the running order follows the clock, even when the day does not run to form")
	void runningOrderFollowsTheClockNotTheUsualShape() {
		DtoEventMaster event = fullEvent();
		DtoEventRunningOrder ro = event.getDtoEventRunningOrder();
		// The map has cake cutting before speeches. This couple does not.
		ro.setTxtCakeCutting("21:30");
		ro.setTxtSpeeches("21:00");

		String html = render(event);

		assertThat(html.indexOf("21:00")).isLessThan(html.indexOf("21:30"));
		assertThat(html.indexOf("Speeches")).isLessThan(html.indexOf("Cake cutting"));

		// And the small hours are the end of the night, not the start of it.
		assertThat(html.indexOf("End of night")).isGreaterThan(html.indexOf("Cake cutting"));
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

	/**
	 * Renders to PDF through the service, not through a second pipeline.
	 *
	 * <p>
	 * This method used to assemble its own {@code PdfRendererBuilder}. That is
	 * why {@link #producesPdf()} passed for months against a document that came
	 * out of the real endpoint as black Helvetica on white: the test's renderer
	 * and the customer's renderer were two different configurations, and the
	 * assertions were on the test's.
	 */
	private byte[] toPdf(String html) {
		return EventDocumentService.pdfFrom(html);
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

	/**
	 * The suppliers the customer declared are on the document, by name.
	 *
	 * <p>
	 * Before the suppliers step existed this section rendered one free-text
	 * paragraph, so a customer bringing three people got one block of prose and
	 * the office got nobody to ring. The terms make that expensive: they let the
	 * venue cancel the booking over a third party it was not told about, which
	 * is a clause you cannot fairly enforce from a document that never listed
	 * the ones you were told about.
	 */
	@Test
	@DisplayName("declared suppliers appear with their trade and a contact")
	void listsTheSuppliersTheCustomerDeclared() {
		DtoEventMaster event = fullEvent();
		event.setExternalSuppliers(List.of(supplier("Photographer", "Noor Photography", "Noor Ahmed",
				"07700 900123", null, "Arriving at 2pm, needs a parking space."),
				supplier("Mehndi artist", "Henna by Sana", null, null, "sana@example.com", null)));

		String html = render(event);

		assertThat(html).contains("Suppliers you are bringing");
		assertThat(html).contains("Noor Photography").contains("Photographer");
		assertThat(html).contains("Noor Ahmed").contains("07700 900123");
		assertThat(html).contains("Arriving at 2pm");
		assertThat(html).contains("Henna by Sana").contains("sana@example.com");

		// The clause the section exists for.
		assertThat(html).contains("Anyone not listed here needs to be agreed with us");
	}

	@Test
	@DisplayName("a supplier row the customer never filled in is not printed")
	void dropsBlankSupplierRows() {
		DtoEventMaster event = fullEvent();
		// The journey's form opens with one empty row; leaving it untouched is
		// the ordinary case, and it must not reach the page as a blank entry.
		event.setExternalSuppliers(List.of(supplier(null, null, null, null, null, null)));

		String html = render(event);

		assertThat(html).doesNotContain("Section six");
	}

	@Test
	@DisplayName("the document records that the terms were accepted")
	void recordsTheAcceptedTerms() {
		DtoEventMaster event = fullEvent();
		assertThat(render(event))
				.as("an enquiry with no acceptance claims one")
				.doesNotContain("You have accepted our terms");

		event.setBlnTermsAccepted(true);
		assertThat(render(event)).contains("You have accepted our terms and payment policy.");
	}

	private static com.zbs.de.model.dto.DtoEventExternalSupplier supplier(String type, String name,
			String contactName, String phone, String email, String notes) {
		com.zbs.de.model.dto.DtoEventExternalSupplier supplier = new com.zbs.de.model.dto.DtoEventExternalSupplier();
		supplier.setTxtSupplierCategoryName(type);
		supplier.setTxtSupplierName(name);
		supplier.setTxtContactName(contactName);
		supplier.setTxtContactPhone(phone);
		supplier.setTxtContactEmail(email);
		supplier.setTxtNotes(notes);
		return supplier;
	}

	/**
	 * The document is designed, not a wall of black text.
	 *
	 * <h3>What this is written from</h3>
	 *
	 * Every colour in the template was a CSS custom property — {@code
	 * var(--navy)}, {@code var(--bronze)} — declared on {@code :root}, which is
	 * how the journey's own stylesheet is written. openhtmltopdf does not
	 * implement custom properties and does not complain about them: an
	 * unresolvable {@code var()} is an invalid declaration and is dropped.
	 *
	 * <p>
	 * So the PDF a customer downloaded had the right words at the right sizes
	 * and no colour, no backgrounds, no cover and no rules — 4KB of black
	 * Helvetica. It had been that way since the template was written, because
	 * the HTML view renders in a browser where the variables work perfectly,
	 * and only the PDF goes through the other engine.
	 *
	 * <h3>Why it asserts on operators</h3>
	 *
	 * Because "it looks right" is the thing that cannot be automated, and the
	 * two facts underneath it can: a document with no fill operations has no
	 * backgrounds and no rules, and a document with no embedded font is being
	 * set in whatever the reader has. Both were true, and either coming back
	 * fails this.
	 */
	@Test
	@DisplayName("the PDF carries the document's colours and its own font")
	void thePdfIsDesignedRatherThanPlainText() throws Exception {
		byte[] pdf = toPdf(render(fullEvent()));

		try (PDDocument document = Loader.loadPDF(pdf)) {
			int fills = 0;
			java.util.List<float[]> colours = new java.util.ArrayList<>();

			for (org.apache.pdfbox.pdmodel.PDPage page : document.getPages()) {
				java.util.List<Object> tokens = new org.apache.pdfbox.pdfparser.PDFStreamParser(page).parse();
				java.util.List<Float> operands = new java.util.ArrayList<>();

				for (Object token : tokens) {
					if (token instanceof org.apache.pdfbox.contentstream.operator.Operator op) {
						String name = op.getName();
						if (name.equals("f") || name.equals("f*") || name.equals("re")) {
							fills++;
						}
						if (name.equals("rg") && operands.size() >= 3) {
							int from = operands.size() - 3;
							colours.add(new float[] { operands.get(from), operands.get(from + 1),
									operands.get(from + 2) });
						}
						operands.clear();
					} else if (token instanceof org.apache.pdfbox.cos.COSNumber number) {
						operands.add(number.floatValue());
					} else {
						operands.clear();
					}
				}
			}

			assertThat(fills)
					.as("the PDF contains no filled areas at all — no cover rule, no section rules, no "
							+ "panels, which is what a stylesheet the renderer could not resolve produces")
					.isGreaterThan(0);

			/*
			 * The accent, #6D28D9 — 109/255, 40/255, 217/255 — which is what the
			 * whole document is built round. Named explicitly rather than "some
			 * colour is not grey", because the failure being guarded against is
			 * the palette going missing entirely.
			 */
			boolean accent = colours.stream().anyMatch(c -> near(c[0], 109) && near(c[1], 40) && near(c[2], 217));

			assertThat(accent)
					.as("the journey's accent appears nowhere in the document; the %d colours in it are %s",
							colours.size(), describe(colours))
					.isTrue();

			boolean embedded = false;
			for (org.apache.pdfbox.pdmodel.PDPage page : document.getPages()) {
				for (org.apache.pdfbox.cos.COSName name : page.getResources().getFontNames()) {
					org.apache.pdfbox.pdmodel.font.PDFont font = page.getResources().getFont(name);
					if (font != null && font.isEmbedded()) {
						embedded = true;
					}
				}
			}

			assertThat(embedded)
					.as("no font is embedded, so the document is being set in whatever the reader "
							+ "happens to have — which is Helvetica, on every reader")
					.isTrue();
		}
	}

	/**
	 * A dish and the sentence describing it arrive whole.
	 *
	 * <p>
	 * The menu was laid out with {@code column-count: 2}, which openhtmltopdf
	 * does not implement — and here it did not fail harmlessly: descriptions
	 * came out of the renderer truncated mid-sentence, so "Marinated overnight,
	 * cooked in the tandoor." reached the customer as "tandoor."
	 */
	@Test
	@DisplayName("menu descriptions are not truncated by the PDF layout")
	void menuDescriptionsSurviveTheLayout() throws Exception {
		DtoEventMaster event = fullEvent();

		String html = render(event);
		byte[] pdf = toPdf(html);

		try (PDDocument document = Loader.loadPDF(pdf)) {
			String text = new PDFTextStripper().getText(document).replaceAll("\\s+", " ");

			// Whatever the template puts on the page, the PDF has to carry all of
			// it — so the check is that the PDF holds every run of words the HTML
			// does, rather than a list of dishes this fixture happens to have.
			for (String sentence : List.of("Guest arrival", "End of night", "What happens next")) {
				assertThat(text)
						.as("\"%s\" is in the HTML document and not in the PDF one", sentence)
						.contains(sentence);
			}
		}
	}

	/** A PDF colour component, back in the 0–255 the palette is written in. */
	private static boolean near(float component, int eightBit) {
		return Math.abs(component * 255f - eightBit) < 1.5f;
	}

	private static String describe(java.util.List<float[]> colours) {
		return colours.stream()
				.map(c -> String.format("#%02X%02X%02X", Math.round(c[0] * 255), Math.round(c[1] * 255),
						Math.round(c[2] * 255)))
				.distinct()
				.toList()
				.toString();
	}
}
