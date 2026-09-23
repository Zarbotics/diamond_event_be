package com.zbs.de.service.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import org.mockito.Mockito;

/**
 * Whether a calendar adapter registers at all.
 *
 * <p>
 * A small thing with a disproportionate failure. {@code @ConditionalOnProperty}
 * was the obvious choice and the wrong one: it matches a property that exists,
 * and an empty one exists, because {@code application.properties} declares every
 * calendar setting with an empty default so the application starts without them.
 * Both adapters therefore registered on every installation.
 *
 * <p>
 * The whole design is that an unconfigured provider is <em>absent</em> — a state
 * the caller handles correctly by leaving the booking alone — rather than
 * present and failing on use. The integration tests caught it only because two
 * adapters then both claimed to be Google. Without that collision it would have
 * reached production and shown up as consultations failing to sync on a system
 * nobody had connected anything to.
 */
class CalendarProviderConfiguredTest {

	private boolean matches(CalendarProviderConfigured condition, String property, String value) {
		MockEnvironment environment = new MockEnvironment();
		if (value != null) {
			environment.setProperty(property, value);
		}

		ConditionContext context = Mockito.mock(ConditionContext.class);
		Mockito.when(context.getEnvironment()).thenReturn((Environment) environment);

		return condition.matches(context, null);
	}

	@Test
	@DisplayName("a real client id registers the adapter")
	void aConfiguredProviderRegisters() {
		assertThat(matches(new CalendarProviderConfigured.Google(),
				"app.calendar.google.client-id", "123456.apps.googleusercontent.com")).isTrue();

		assertThat(matches(new CalendarProviderConfigured.Microsoft(),
				"app.calendar.microsoft.client-id", "a-guid-from-entra")).isTrue();
	}

	@Test
	@DisplayName("a declared but empty property does not register the adapter")
	void anEmptyPropertyDoesNotRegister() {
		// The actual bug. The property is declared with an empty default so the
		// application starts without credentials, so "is it set?" is not the
		// same question as "does it have a value?".
		assertThat(matches(new CalendarProviderConfigured.Google(),
				"app.calendar.google.client-id", "")).isFalse();

		assertThat(matches(new CalendarProviderConfigured.Microsoft(),
				"app.calendar.microsoft.client-id", "")).isFalse();
	}

	@Test
	@DisplayName("whitespace is not a client id either")
	void whitespaceDoesNotRegister() {
		// A trailing space in an environment variable or a deployment template
		// is common enough to be worth not treating as configuration.
		assertThat(matches(new CalendarProviderConfigured.Google(),
				"app.calendar.google.client-id", "   ")).isFalse();
	}

	@Test
	@DisplayName("an absent property does not register the adapter")
	void anAbsentPropertyDoesNotRegister() {
		assertThat(matches(new CalendarProviderConfigured.Google(),
				"app.calendar.google.client-id", null)).isFalse();
	}

	@Test
	@DisplayName("the two providers read their own settings, not each other's")
	void eachProviderReadsItsOwnProperty() {
		// Configuring Google must not bring Microsoft along with it.
		assertThat(matches(new CalendarProviderConfigured.Microsoft(),
				"app.calendar.google.client-id", "a-google-client-id")).isFalse();
	}
}
