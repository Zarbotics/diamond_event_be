package com.zbs.de.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

/**
 * An uploaded file must land inside the upload directory, whatever it is
 * called.
 *
 * <p>
 * The stored path was built as {@code folder + "/" + UUID + "_" +
 * file.getOriginalFilename()}. That name comes from the multipart body, not
 * from the URL, so none of the request-path checking in Tomcat or Spring ever
 * looks at it — a point worth being precise about, because the same
 * {@code ../} in a URL is rejected with a 400 before any controller runs, and
 * that made the write path look safer than it was.
 *
 * <p>
 * The application writes to {@code /root/...}, so it runs as root. An
 * arbitrary write as root, reachable by anyone who could hit an upload
 * endpoint, is the most serious thing on this codebase after the signup
 * endpoint that handed out {@code ROLE_ADMIN}.
 */
class UtilFileStorageTest {

	@TempDir
	Path uploads;

	@BeforeEach
	void pointStorageAtTheTempDirectory() {
		UtilFileStorage.configure(uploads.toString(), "/diamond/deimg");
	}

	private MockMultipartFile upload(String filename) {
		return new MockMultipartFile("files", filename, "image/png", "not really a png".getBytes());
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("a filename that climbs out of the directory cannot escape it")
	void traversalInTheFilenameCannotEscape() throws IOException {
		/*
		 * Six levels is enough to clear the folder, the category and the UUID
		 * segment in front of it. Against the old implementation this landed
		 * outside the upload directory entirely.
		 */
		String climbing = "../../../../../../escaped.png";

		String url = UtilFileStorage.saveFile(upload(climbing), "decor");

		Path escaped = uploads.getParent().resolve("escaped.png");
		assertThat(Files.exists(escaped))
				.as("the upload wrote outside its directory, to %s", escaped)
				.isFalse();

		assertThat(url).startsWith("/diamond/deimg/decor/");
		assertThat(Files.list(uploads.resolve("decor")))
				.as("exactly one file, inside the category folder")
				.hasSize(1);
	}

	@Test
	@DisplayName("nothing of the uploader's filename is kept but the extension")
	void theStoredNameIsGenerated() throws IOException {
		String url = UtilFileStorage.saveFile(upload("my holiday photo (1).PNG"), "venues");

		assertThat(url).endsWith(".png");
		assertThat(url)
				.as("the customer's filename must not appear in the stored name")
				.doesNotContain("holiday")
				.doesNotContain(" ")
				.doesNotContain("(");
	}

	@Test
	@DisplayName("a file that could run as script is refused")
	void scriptableTypesAreRefused() {
		// /deimg is public and serves from the API's own origin, so an SVG or an
		// HTML file stored here would be stored cross-site scripting.
		for (String dangerous : new String[] { "payload.svg", "payload.html", "payload.js", "shell.jsp" }) {
			assertThatThrownBy(() -> UtilFileStorage.saveFile(upload(dangerous), "decor"))
					.as("%s should have been refused", dangerous)
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("cannot be uploaded");
		}
	}

	@Test
	@DisplayName("a file with no extension at all is refused")
	void extensionlessFilesAreRefused() {
		assertThatThrownBy(() -> UtilFileStorage.saveFile(upload("no-extension"), "decor"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("the formats these uploads are actually for are accepted")
	void realUploadsStillWork() throws IOException {
		for (String good : new String[] { "a.jpg", "b.JPEG", "c.png", "d.gif", "e.webp", "f.heic", "receipt.pdf" }) {
			assertThat(UtilFileStorage.saveFile(upload(good), "venues"))
					.as("%s should be accepted", good)
					.contains("/deimg/venues/");
		}
	}

	@Test
	@DisplayName("an empty upload is refused rather than written")
	void emptyUploadsAreRefused() {
		MockMultipartFile empty = new MockMultipartFile("files", "a.png", "image/png", new byte[0]);
		assertThatThrownBy(() -> UtilFileStorage.saveFile(empty, "decor"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("empty");
	}

	@Test
	@DisplayName("a category is a directory name, so it is checked rather than trusted")
	void categoriesAreChecked() {
		// No call site passes anything but a literal today. This is here so that a
		// future one taking a category from a request does not open the hole again.
		assertThatThrownBy(() -> UtilFileStorage.saveFile(upload("a.png"), "../../etc"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid upload category");
	}

	@Test
	@DisplayName("the returned URL is relative, not pinned to production")
	void theUrlIsNotHardcodedToProduction() throws IOException {
		String url = UtilFileStorage.saveFile(upload("a.png"), "decor");

		// It returned https://diamondevents.uk:8081/... regardless of where it was
		// running, so a file uploaded in development came back with a URL pointing
		// at a machine that does not have it.
		assertThat(url).doesNotContain("diamondevents.uk");
		assertThat(url).startsWith("/diamond/deimg/");
	}

	@Test
	@DisplayName("the base directory is whatever it was configured with")
	void theDirectoryIsConfigurable() {
		assertThat(UtilFileStorage.baseDirectory())
				.isEqualTo(Paths.get(uploads.toString()).toAbsolutePath().normalize());
	}
}
