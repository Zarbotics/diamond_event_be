package com.zbs.de.service.calendar;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Registers a calendar adapter only when it has a client id that is not blank.
 *
 * <p>
 * {@code @ConditionalOnProperty} cannot express this. It matches a property that
 * exists, and an empty one exists — {@code application.properties} declares
 * every calendar setting with an empty default so the application starts
 * without them. The adapters therefore registered on every installation,
 * configured or not.
 *
 * <p>
 * That is worth more than tidiness. An adapter with no credentials is a
 * provider that <em>fails on use</em>, where the whole design is that an
 * unconfigured provider is <em>absent</em> — a state the caller already handles
 * correctly by leaving the booking alone and recording that there was no
 * calendar to write to. The tests caught it as two adapters both claiming to be
 * Google; in production it would have surfaced as consultations failing to sync
 * on a system nobody had connected anything to.
 */
public abstract class CalendarProviderConfigured implements Condition {

	/** The property that must hold a real value. */
	protected abstract String property();

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		String value = context.getEnvironment().getProperty(property());
		return value != null && !value.isBlank();
	}

	public static class Google extends CalendarProviderConfigured {
		@Override
		protected String property() {
			return "app.calendar.google.client-id";
		}
	}

	public static class Microsoft extends CalendarProviderConfigured {
		@Override
		protected String property() {
			return "app.calendar.microsoft.client-id";
		}
	}
}
