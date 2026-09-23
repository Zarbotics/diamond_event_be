package com.zbs.de.config.security;

/**
 * Canonical role names.
 *
 * <p>
 * These strings are stored verbatim in {@code user_master.txt_role} and are
 * granted as Spring Security authorities by {@link JwtAuthenticationFilter}.
 * Because they already carry the {@code ROLE_} prefix they are used with
 * {@code hasAuthority(..)} rather than {@code hasRole(..)}.
 */
public final class SecurityRoles {

	/** Back-office staff. Full access to the admin surface. */
	public static final String ADMIN = "ROLE_ADMIN";

	/** A customer using the booking portal. Access limited to their own data. */
	public static final String USER = "ROLE_USER";

	private SecurityRoles() {
	}
}
