package com.zbs.de.config.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class AppleClientSecretGenerator {

	@Value("${apple.enabled:false}")
	private boolean enabled;

	@Value("${apple.team-id:}")
	private String teamId;

	@Value("${apple.client-id:}")
	private String clientId;

	@Value("${apple.key-id:}")
	private String keyId;

	@Value("${apple.private-key-file:}")
	private String privateKeyPath;

	/**
	 * Signs a short-lived client secret for Apple's token endpoint.
	 *
	 * <p>
	 * Apple has no static client secret: it is a JWT signed with the .p8 key
	 * downloaded from the developer account. Every property is optional so the
	 * application starts on a machine that has none — Apple sign-in is production
	 * only, because there is a single developer account and a single key.
	 *
	 * <p>
	 * The checks below turn what used to be an opaque NullPointerException or
	 * NoSuchFileException, surfacing to a customer mid-sign-in, into a message
	 * that names the missing piece.
	 */
	public String generateClientSecret() {
		if (!enabled) {
			throw new IllegalStateException(
					"Apple sign-in is not enabled on this environment. "
							+ "Activate the 'apple' profile (SPRING_PROFILES_ACTIVE=prod,apple) to use it.");
		}
		if (isBlank(teamId) || isBlank(keyId) || isBlank(clientId)) {
			throw new IllegalStateException(
					"Apple sign-in is enabled but incomplete: APPLE_TEAM_ID, APPLE_KEY_ID and "
							+ "APPLE_CLIENT_ID must all be set.");
		}
		if (isBlank(privateKeyPath) || !java.nio.file.Files.isReadable(java.nio.file.Paths.get(privateKeyPath))) {
			throw new IllegalStateException(
					"Apple signing key is missing or unreadable at '" + privateKeyPath
							+ "'. Set APPLE_PRIVATE_KEY_FILE to the .p8 downloaded from the Apple developer account.");
		}

		try {
			PrivateKey privateKey = loadPrivateKey();

			long now = System.currentTimeMillis();

			return Jwts.builder().setHeaderParam("kid", keyId).setIssuer(teamId).setIssuedAt(new Date(now))
					.setExpiration(new Date(now + 86400000L * 180)) // 6 months
					.setAudience("https://appleid.apple.com").setSubject(clientId)
					.signWith(privateKey, SignatureAlgorithm.ES256).compact();

		} catch (Exception e) {
			throw new IllegalStateException(
					"Could not sign the Apple client secret. Check that APPLE_PRIVATE_KEY_FILE is the "
							+ "unmodified .p8 for key id '" + keyId + "'.", e);
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private PrivateKey loadPrivateKey() throws Exception {
		String key = Files.readString(Paths.get(privateKeyPath)).replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");

		byte[] decoded = Base64.getDecoder().decode(key);

		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
		KeyFactory kf = KeyFactory.getInstance("EC");

		return kf.generatePrivate(spec);
	}
}