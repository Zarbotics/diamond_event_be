package com.zbs.de.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.util.UtilFileStorage;

/**
 * Serves uploaded images and payment receipts.
 *
 * <p>
 * This endpoint is public — venue photographs and decor pictures are shown
 * before a customer signs in — so what it will hand back matters.
 *
 * <p>
 * It read {@code /root/diamondevent_be/uploads/} directly, one of three
 * hardcoded paths in the file with the other two commented out, one of them a
 * {@code C:/Users/hp/Pictures} directory on somebody's laptop. It now reads
 * whatever {@link UtilFileStorage} was configured with, so the reader and the
 * writer cannot drift apart.
 *
 * <p>
 * Two things it deliberately does now:
 *
 * <ul>
 * <li><b>It checks the resolved path.</b> Tomcat rejects {@code ../} in a
 * request path before this method is reached — that was tested, not assumed —
 * but this is a public endpoint reading from disk in a process running as root,
 * and the check that makes that safe belongs in the code that depends on it
 * rather than in a container setting somebody may later relax.
 * <li><b>It serves a known content type, not a detected one.</b> The type came
 * from {@code Files.probeContentType}, which reports what the file really is —
 * so an uploaded {@code .svg} or {@code .html} came back as
 * {@code image/svg+xml} or {@code text/html} from the API's own origin and ran
 * as script there. Uploads are now restricted at the point of writing; this
 * maps from the stored extension rather than the content, adds {@code nosniff}
 * so the browser will not second-guess it, and refuses anything else.
 * </ul>
 */
@RestController
@RequestMapping("/deimg")
public class ControllerFileServe {

	@GetMapping("/{category}/{filename:.+}")
	public ResponseEntity<Resource> serveFile(@PathVariable String category, @PathVariable String filename)
			throws IOException {

		Path base = UtilFileStorage.baseDirectory();
		Path filePath = base.resolve(category).resolve(filename).normalize();

		if (!filePath.startsWith(base) || !Files.isRegularFile(filePath)) {
			return ResponseEntity.notFound().build();
		}

		MediaType contentType = typeFor(filename);
		if (contentType == null) {
			/*
			 * Stored before the allowlist existed, or something we will not vouch
			 * for. Not found rather than octet-stream: this endpoint exists to show
			 * pictures and receipts, and a file it cannot identify is neither.
			 */
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok()
				.contentType(contentType)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline")
				.header("X-Content-Type-Options", "nosniff")
				.body(new UrlResource(filePath.toUri()));
	}

	/** The content type for a stored file, or null if it is not one we serve. */
	private MediaType typeFor(String filename) {
		int dot = filename.lastIndexOf('.');
		String extension = dot >= 0 ? filename.substring(dot + 1).toLowerCase(Locale.ROOT) : "";

		return switch (extension) {
			case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
			case "png" -> MediaType.IMAGE_PNG;
			case "gif" -> MediaType.IMAGE_GIF;
			case "webp" -> MediaType.parseMediaType("image/webp");
			case "heic" -> MediaType.parseMediaType("image/heic");
			case "pdf" -> MediaType.APPLICATION_PDF;
			default -> null;
		};
	}
}
