package com.zbs.de.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.zbs.de.config.security.PortalEndpoints;

/**
 * Generates and checks {@code API.md}, the inventory of every endpoint.
 *
 * <h2>Why this and not springdoc</h2>
 *
 * springdoc-openapi is the right long-term answer for a Spring Boot
 * application, and it should be added when the build can resolve a new
 * dependency again — see §10 C5. What it cannot do is the thing most needed
 * here, which is answer <em>who is allowed to call this</em>. Authorisation
 * lives in {@link PortalEndpoints}, and an OpenAPI document generated from the
 * controllers alone would show 340 endpoints with no indication that some are
 * open to the world and the rest are back-office only.
 *
 * <h2>How it stays honest</h2>
 *
 * Run with {@code -Dapi.docs.write=true} to regenerate the file. Otherwise the
 * test compares what is committed against what the source says, and fails when
 * they have drifted — so a new endpoint either appears in the documentation or
 * fails the build. Documentation that is generated but never checked is
 * documentation nobody can trust after the second month.
 */
class ApiInventoryTest {

	private static final Path CONTROLLERS = Path.of("src/main/java/com/zbs/de/controller");
	private static final Path DOCUMENT = Path.of("API.md");

	/**
	 * The class-level prefix.
	 *
	 * <p>
	 * Anchored to the one immediately before the class declaration, because
	 * {@code @RequestMapping} is also used on methods here and taking the first
	 * match in the file would sometimes pick up a method's.
	 */
	private static final Pattern CLASS_MAPPING = Pattern.compile(
			"@RequestMapping\\(\\s*(?:value\\s*=\\s*)?\"([^\"]*)\"[^)]*\\)[\\s\\S]{0,400}?public\\s+class");

	private static final Pattern METHOD_MAPPING = Pattern.compile(
			"@(Get|Post|Put|Patch|Delete)Mapping\\s*(?:\\(\\s*(?:value\\s*=\\s*)?\"([^\"]*)\")?");

	/**
	 * The older form, which a third of this codebase still uses:
	 * {@code @RequestMapping(value = "/x", method = RequestMethod.POST)}.
	 *
	 * <p>
	 * Missing it is not a cosmetic gap in the documentation. The audience column
	 * is derived by matching these paths against the authorisation lists, so an
	 * endpoint the scan cannot see is an endpoint whose access rules nobody is
	 * checking — and the first version of this test reported four allowlist
	 * entries as pointing at nothing when all four were real, live endpoints.
	 */
	private static final Pattern LEGACY_METHOD_MAPPING = Pattern.compile(
			"@RequestMapping\\(\\s*(?:value\\s*=\\s*)?\"([^\"]*)\"[^)]*?method\\s*=\\s*RequestMethod\\.(\\w+)");

	/** One endpoint, as the source describes it. */
	private record Endpoint(String verb, String path, String controller) {
	}

	@Test
	@DisplayName("API.md matches the endpoints that actually exist")
	void theInventoryIsCurrent() throws IOException {
		List<Endpoint> endpoints = scan();

		assertThat(endpoints)
				.as("no endpoints were found, so this test is asserting nothing")
				.hasSizeGreaterThan(100);

		String generated = render(endpoints);

		if (Boolean.getBoolean("api.docs.write")) {
			Files.writeString(DOCUMENT, generated);
			return;
		}

		assertThat(Files.exists(DOCUMENT))
				.as("API.md is missing — regenerate it with -Dapi.docs.write=true")
				.isTrue();

		assertThat(Files.readString(DOCUMENT))
				.as("""
						API.md no longer matches the controllers. An endpoint has been added, \
						removed or renamed without the inventory being updated. Regenerate it \
						with -Dapi.docs.write=true and commit the result.""")
				.isEqualTo(generated);
	}

	@Test
	@DisplayName("every endpoint is accounted for by the authorisation rules")
	void everyEndpointHasAnAudience() throws IOException {
		/*
		 * Not that each one is listed — most are deliberately absent, which is
		 * what makes them administrator-only under the default-deny rule. This
		 * asserts the other direction: that nothing in the allowlists points at
		 * an endpoint that no longer exists.
		 *
		 * A stale entry is not harmless. It is a path that was opened to
		 * customers once, and if a future controller ever claims that path back
		 * it arrives already public, with nobody having decided so.
		 */
		List<String> paths = scan().stream().map(Endpoint::path).toList();

		List<String> pointingAtNothing = new ArrayList<>();
		for (String allowed : PortalEndpoints.allCustomerAccessible()) {
			if (allowed.contains("*")) {
				continue; // wildcards are matched by prefix, not by exact path
			}
			if (!paths.contains(allowed)) {
				pointingAtNothing.add(allowed);
			}
		}

		assertThat(pointingAtNothing)
				.as("""
						These paths are open to customers but no controller serves them. \
						Remove them: a path opened once and then vacated is a path a future \
						controller can claim while already being public.""")
				.isEmpty();
	}

	// -----------------------------------------------------------------

	private List<Endpoint> scan() throws IOException {
		List<Endpoint> endpoints = new ArrayList<>();

		try (Stream<Path> files = Files.walk(CONTROLLERS)) {
			for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
				String source = Files.readString(file);
				String controller = file.getFileName().toString().replace(".java", "");

				Matcher classMapping = CLASS_MAPPING.matcher(source);
				String base = classMapping.find() ? classMapping.group(1) : "";

				Matcher methods = METHOD_MAPPING.matcher(source);
				while (methods.find()) {
					String suffix = methods.group(2) == null ? "" : methods.group(2);
					endpoints.add(new Endpoint(methods.group(1).toUpperCase(), join(base, suffix), controller));
				}

				Matcher legacy = LEGACY_METHOD_MAPPING.matcher(source);
				while (legacy.find()) {
					endpoints.add(new Endpoint(legacy.group(2).toUpperCase(), join(base, legacy.group(1)), controller));
				}
			}
		}

		endpoints.sort(Comparator.comparing(Endpoint::path).thenComparing(Endpoint::verb));
		return endpoints;
	}

	private String join(String base, String suffix) {
		if (suffix.isEmpty()) {
			return base.isEmpty() ? "/" : base;
		}
		String joined = base + (suffix.startsWith("/") ? suffix : "/" + suffix);
		return joined.isEmpty() ? "/" : joined;
	}

	private String render(List<Endpoint> endpoints) {
		Map<String, List<Endpoint>> byController = new LinkedHashMap<>();
		endpoints.stream()
				.sorted(Comparator.comparing(Endpoint::controller).thenComparing(Endpoint::path))
				.forEach(e -> byController.computeIfAbsent(e.controller(), k -> new ArrayList<>()).add(e));

		long reads = endpoints.stream().filter(e -> e.path().matches(".*/(get|search|find|is|all).*")).count();

		StringBuilder out = new StringBuilder();
		out.append("# API inventory\n\n");
		out.append("**Generated.** Do not edit by hand — `ApiInventoryTest` regenerates this file\n");
		out.append("with `-Dapi.docs.write=true` and fails the build when it drifts from the\n");
		out.append("controllers.\n\n");
		out.append("Base address `http://localhost:8080/diamond`. Paths below are relative to it.\n\n");

		out.append("## The shape of this API\n\n");
		out.append("| | |\n|---|---|\n");
		out.append("| Endpoints | ").append(endpoints.size()).append(" |\n");
		out.append("| Controllers | ").append(byController.size()).append(" |\n");
		out.append("| Named like a read (`get…`, `search…`, `is…`) | ").append(reads).append(" |\n\n");

		out.append("Almost every endpoint is `POST`, including the reads. Nothing is cacheable,\n");
		out.append("no intermediary can safely retry a read, and which of these change data is\n");
		out.append("answerable only by reading each one. §15.4 of PLATFORM.md sets out how that\n");
		out.append("is being unwound — as a by-product of the booking model rather than as a\n");
		out.append("migration of its own.\n\n");

		out.append("## Who can call what\n\n");
		out.append("Authorisation is **default-deny**: anything absent from the lists below is\n");
		out.append("administrator-only. That is the rule `PortalEndpoints` encodes, and it is why\n");
		out.append("most of this inventory carries no audience marker.\n\n");
		out.append("- 🌍 **Public** — no authentication at all.\n");
		out.append("- 👤 **Signed in** — any customer or member of staff.\n");
		out.append("- *(unmarked)* — administrator only.\n\n");

		for (Map.Entry<String, List<Endpoint>> entry : byController.entrySet()) {
			out.append("### ").append(entry.getKey()).append("\n\n");
			out.append("| Verb | Path | Who |\n|---|---|---|\n");
			for (Endpoint endpoint : entry.getValue()) {
				out.append("| `").append(endpoint.verb()).append("` | `").append(endpoint.path())
						.append("` | ").append(audienceOf(endpoint.path())).append(" |\n");
			}
			out.append("\n");
		}

		return out.toString();
	}

	/**
	 * Who may call this path.
	 *
	 * <p>
	 * In the same order as the security chain, which is not a detail. Spring
	 * Security takes the first rule that matches, and
	 * {@code AUTHENTICATED_AUTH} exists precisely because it must beat
	 * {@code PUBLIC}'s {@code /auth/**}. Checking {@code PUBLIC} first here
	 * produced documentation stating that {@code /auth/handoff} — which mints a
	 * signed-in session — was open to the world. Documentation that contradicts
	 * the configuration is worse than none, because it is believed.
	 */
	private String audienceOf(String path) {
		for (String pattern : PortalEndpoints.AUTHENTICATED_AUTH) {
			if (matches(pattern, path)) {
				return "👤";
			}
		}
		for (String pattern : PortalEndpoints.PUBLIC) {
			if (matches(pattern, path)) {
				return "🌍";
			}
		}
		for (String pattern : PortalEndpoints.allCustomerAccessible()) {
			if (matches(pattern, path)) {
				return "👤";
			}
		}
		return "";
	}

	private boolean matches(String pattern, String path) {
		if (pattern.endsWith("/**")) {
			return path.startsWith(pattern.substring(0, pattern.length() - 3));
		}
		if (pattern.endsWith("/*")) {
			String prefix = pattern.substring(0, pattern.length() - 1);
			return path.startsWith(prefix) && !path.substring(prefix.length()).contains("/");
		}
		return pattern.equals(path);
	}
}
