package com.zbs.de.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

/**
 * Stores an uploaded file and returns the URL it will be served from.
 *
 * <p>
 * <b>The uploaded file's own name is never used to build the path.</b> It was,
 * and because that name arrives in the multipart body rather than the URL,
 * nothing in Tomcat or Spring inspects it — the request-path firewall that
 * rejects {@code ../} in a URL never sees it. A file named
 *
 * <pre>../../../../../../tmp/anything</pre>
 *
 * resolved, with the folder and the UUID prefix in front of it, to a path
 * outside the upload directory entirely. {@code UtilFileStorageTest} shows it
 * landing in {@code /tmp}. The application was writing to {@code /root/...},
 * so it runs as root: that made it an arbitrary file write as root by anyone
 * who could reach an upload endpoint.
 *
 * <p>
 * The stored name is now generated in full, and only a checked extension is
 * carried over. Everything else about the customer's filename is discarded —
 * nothing downstream needs it, and it is the one part of an upload the
 * uploader controls.
 *
 * <p>
 * The extension is checked against an allowlist too. {@code /deimg} is public
 * and serves back a content type derived from the stored file, so an uploaded
 * {@code .svg} or {@code .html} would run as script on the API's own origin.
 */
public final class UtilFileStorage {

	private UtilFileStorage() {
	}

	/**
	 * What an upload is allowed to be.
	 *
	 * <p>
	 * Every call site stores a picture — decor references, venue photos, event
	 * type images, decor options — except payments, which take a receipt, hence
	 * PDF. Deliberately no SVG: it is a document that can carry script, and it
	 * would be served from the API's own origin.
	 */
	private static final Set<String> ALLOWED_EXTENSIONS =
			Set.of("jpg", "jpeg", "png", "gif", "webp", "heic", "pdf");

	/**
	 * Where uploads live, and how they are addressed.
	 *
	 * <p>
	 * Both were hardcoded: the directory to {@code /root/diamondevent_be/uploads}
	 * and the returned URL to {@code https://diamondevents.uk:8081}, so a file
	 * uploaded on a developer's machine came back with a URL pointing at
	 * production. An {@code app.upload.dir} property already existed and was
	 * ignored.
	 *
	 * <p>
	 * Static because {@code saveFile} is called statically from nineteen places
	 * across nine services. {@link UploadStorageConfiguration} sets these at
	 * startup.
	 */
	private static volatile Path baseDirectory =
			Paths.get("./uploads").toAbsolutePath().normalize();
	private static volatile String publicBaseUrl = "/api/diamond/deimg";

	static void configure(String directory, String urlPrefix) {
		baseDirectory = Paths.get(directory).toAbsolutePath().normalize();
		publicBaseUrl = urlPrefix.endsWith("/")
				? urlPrefix.substring(0, urlPrefix.length() - 1)
				: urlPrefix;
	}

	/** Where files are written. The file server reads the same value. */
	public static Path baseDirectory() {
		return baseDirectory;
	}

	/**
	 * Saves {@code file} under {@code category} and returns its public URL.
	 *
	 * @throws IOException              if the file cannot be written
	 * @throws IllegalArgumentException if the upload is empty, or is of a type
	 *                                  that is not accepted
	 */
	public static String saveFile(MultipartFile file, String category) throws IOException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("The uploaded file is empty.");
		}

		String extension = checkedExtension(file.getOriginalFilename());
		String safeCategory = checkedCategory(category);

		Path folder = baseDirectory.resolve(safeCategory).normalize();
		Files.createDirectories(folder);

		// Generated in full: nothing the uploader chose reaches the filesystem
		// except the extension, and that was checked above.
		String storedName = UUID.randomUUID() + "." + extension;
		Path target = folder.resolve(storedName).normalize();

		/*
		 * Belt and braces. Neither the name nor the category can escape after the
		 * checks above, but this is the assertion that actually matters and it
		 * costs nothing — a later change to either check cannot quietly
		 * reintroduce a write outside the upload directory.
		 */
		if (!target.startsWith(baseDirectory)) {
			throw new IllegalArgumentException("Refusing to write outside the upload directory.");
		}

		file.transferTo(target);
		return publicBaseUrl + "/" + safeCategory + "/" + storedName;
	}

	/** The extension, lowercased, if it is one we accept. */
	private static String checkedExtension(String originalFilename) {
		String name = originalFilename == null ? "" : originalFilename;
		int dot = name.lastIndexOf('.');
		String extension = dot >= 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "";

		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new IllegalArgumentException(
					"Files of this type cannot be uploaded. Accepted: "
							+ String.join(", ", ALLOWED_EXTENSIONS.stream().sorted().toList()) + ".");
		}
		return extension;
	}

	/**
	 * The category is chosen by the calling service and never by a request — but
	 * it becomes a directory name, so it is checked rather than trusted.
	 */
	private static String checkedCategory(String category) {
		if (category == null || !category.matches("[A-Za-z0-9._-]{1,64}") || category.contains("..")) {
			throw new IllegalArgumentException("Invalid upload category: " + category);
		}
		return category;
	}
}
