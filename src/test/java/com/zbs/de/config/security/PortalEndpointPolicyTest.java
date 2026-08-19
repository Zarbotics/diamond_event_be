package com.zbs.de.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Guards the customer-accessible allowlist against drift.
 *
 * <p>
 * Authorisation is default-deny, which fails safe but fails quietly: if an
 * endpoint in {@link PortalEndpoints} is renamed or removed, the entry silently
 * stops matching and the endpoint becomes administrator-only. The customer
 * journey then breaks in production with a 403 and no other signal.
 *
 * <p>
 * This test scans the real controller mappings and asserts every allowlisted
 * path still exists, so that drift fails the build instead.
 */
class PortalEndpointPolicyTest {

	// Controllers are not confined to one package — the reporting module declares
	// its own. Scanning the whole application avoids the allowlist silently
	// disagreeing with a controller the test never looked at.
	private static final String APPLICATION_PACKAGE = "com.zbs.de";

	@Test
	@DisplayName("every customer-accessible path maps to a real controller endpoint")
	void allowlistMatchesRealEndpoints() {
		Set<String> declared = declaredEndpointPaths();

		Set<String> missing = Arrays.stream(PortalEndpoints.allCustomerAccessible())
				.filter(path -> !declared.contains(path))
				.collect(Collectors.toCollection(LinkedHashSet::new));

		assertThat(missing)
				.as("""
						These paths are on the customer allowlist but no controller declares them. \
						Either the endpoint was renamed (update PortalEndpoints, or the booking \
						portal will start getting 403s) or the entry is stale and should be removed. \
						Declared paths found: %s""".formatted(declared.size()))
				.isEmpty();
	}

	@Test
	@DisplayName("the allowlist contains no duplicate entries")
	void allowlistHasNoDuplicates() {
		String[] all = PortalEndpoints.allCustomerAccessible();
		assertThat(Set.of(all)).hasSize(all.length);
	}

	@Test
	@DisplayName("no allowlist entry uses a multi-segment wildcard")
	void allowlistHasNoBroadWildcards() {
		// A single-segment `*` is allowed because it stands in for a path variable
		// such as {eventId}. A `**` is not: a stray /eventMaster/** here would
		// re-open every administrative operation on events, which is exactly the
		// failure this authorisation model exists to prevent.
		assertThat(PortalEndpoints.allCustomerAccessible())
				.as("customer-accessible paths must never use a multi-segment wildcard")
				.allSatisfy(path -> assertThat(path).doesNotContain("**"));
	}

	@Test
	@DisplayName("no consultation administration endpoint is customer-accessible")
	void consultationAdministrationStaysAdminOnly() {
		/*
		 * /admin is not a protected prefix in itself — several menu reads live
		 * under /admin/menu because the customer journey has always called them
		 * there, and they are on the allowlist. So "it starts with /admin" is not
		 * what keeps these safe; being absent from the allowlist is.
		 *
		 * Worth a test of its own because the consequence of a slip is not a 403
		 * a customer notices. It is a customer being able to rewrite the team's
		 * working hours, read every consultation booked by every other customer,
		 * or confirm their own request.
		 */
		assertThat(PortalEndpoints.allCustomerAccessible())
				.as("consultation administration must never be reachable by a customer")
				.allSatisfy(path -> assertThat(path).doesNotStartWith("/admin/consultation"));
	}

	@Test
	@DisplayName("the customer's own consultation endpoints are still reachable")
	void consultationBookingStaysReachable() {
		// The mirror of the test above: default-deny fails silently, so the
		// endpoints the booking journey depends on need asserting positively.
		assertThat(PortalEndpoints.allCustomerAccessible())
				.contains("/consultation/types", "/consultation/slots",
						"/consultation/book", "/consultation/forEvent");
		assertThat(PortalEndpoints.PUBLIC)
				.as("cancelling comes from a link in an email, by somebody who may not be signed in")
				.contains("/consultation/cancel");
	}

	/** Every request path declared by an @RestController in the application. */
	private Set<String> declaredEndpointPaths() {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

		Set<String> paths = new LinkedHashSet<>();
		for (BeanDefinition definition : scanner.findCandidateComponents(APPLICATION_PACKAGE)) {
			Class<?> controller;
			try {
				controller = Class.forName(definition.getBeanClassName());
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Could not load scanned controller", e);
			}

			for (String base : basePaths(controller)) {
				for (Method method : controller.getDeclaredMethods()) {
					for (String mapped : methodPaths(method)) {
						paths.add(join(base, mapped));
					}
				}
			}
		}
		return paths;
	}

	private String[] basePaths(Class<?> controller) {
		RequestMapping mapping = controller.getAnnotation(RequestMapping.class);
		if (mapping == null || mapping.value().length == 0) {
			return new String[] { "" };
		}
		return mapping.value();
	}

	private String[] methodPaths(Method method) {
		if (method.isAnnotationPresent(PostMapping.class)) {
			return method.getAnnotation(PostMapping.class).value();
		}
		if (method.isAnnotationPresent(GetMapping.class)) {
			return method.getAnnotation(GetMapping.class).value();
		}
		if (method.isAnnotationPresent(PutMapping.class)) {
			return method.getAnnotation(PutMapping.class).value();
		}
		if (method.isAnnotationPresent(PatchMapping.class)) {
			return method.getAnnotation(PatchMapping.class).value();
		}
		if (method.isAnnotationPresent(DeleteMapping.class)) {
			return method.getAnnotation(DeleteMapping.class).value();
		}
		if (method.isAnnotationPresent(RequestMapping.class)) {
			return method.getAnnotation(RequestMapping.class).value();
		}
		return new String[0];
	}

	/**
	 * Joins a controller base path to a method path and normalises it into the form
	 * the security chain matches on, so {@code /report/eventClientSide/{eventId}}
	 * compares equal to the allowlist's {@code /report/eventClientSide/*}.
	 */
	private String join(String base, String path) {
		String left = base.startsWith("/") ? base : "/" + base;
		left = left.endsWith("/") ? left.substring(0, left.length() - 1) : left;
		String right = path.startsWith("/") ? path : "/" + path;
		return (left + right).replaceAll("\\{[^}]+}", "*");
	}
}
