package com.zbs.de.model;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * A single-use, short-lived code that hands an SSO session to the browser.
 *
 * <p>
 * After Google or Apple sign-in the backend used to redirect with
 * {@code ?accessToken=…&refreshToken=…} appended. Tokens in a URL end up in
 * browser history, server access logs, proxy logs and any {@code Referer}
 * header sent to a third party — and a leaked refresh token is a durable
 * account takeover, not a momentary one.
 *
 * <p>
 * The redirect now carries only this code, which is useless on its own: it is
 * exchanged once, over POST, for the real tokens, and destroyed in the process.
 * Its whole lifetime is measured in seconds.
 *
 * <p>
 * Stored in the database rather than in memory so that the exchange works when
 * more than one instance is running, and survives a restart between the
 * redirect and the exchange.
 */
@Entity
@Table(name = "sso_handoff_code", indexes = {
		@Index(name = "idx_sso_handoff_code_expires", columnList = "dte_expires_at")
})
public class SsoHandoffCode implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_sso_handoff_code_id")
	private Long id;

	/**
	 * A SHA-256 hash of the code, never the code itself.
	 *
	 * <p>
	 * The code is a bearer credential for the few seconds it lives. Hashing means
	 * a leaked database snapshot — or an operator reading the table — cannot
	 * replay one.
	 */
	@Column(name = "txt_code_hash", nullable = false, unique = true, length = 64)
	private String codeHash;

	@Column(name = "txt_access_token", nullable = false, length = 2048)
	private String accessToken;

	@Column(name = "txt_refresh_token", nullable = false, length = 512)
	private String refreshToken;

	@Column(name = "dte_expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "dte_created_at", nullable = false)
	private Instant createdAt;

	protected SsoHandoffCode() {
	}

	public SsoHandoffCode(String codeHash, String accessToken, String refreshToken, Instant expiresAt) {
		this.codeHash = codeHash;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.expiresAt = expiresAt;
		this.createdAt = Instant.now();
	}

	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}

	public Long getId() {
		return id;
	}

	public String getCodeHash() {
		return codeHash;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
