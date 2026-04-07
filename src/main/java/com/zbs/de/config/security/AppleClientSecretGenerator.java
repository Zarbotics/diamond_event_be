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

	@Value("${apple.team-id}")
	private String teamId;

	@Value("${apple.client-id}")
	private String clientId;

	@Value("${apple.key-id}")
	private String keyId;

	@Value("${apple.private-key-file}")
	private String privateKeyPath;

	public String generateClientSecret() {
		try {
			PrivateKey privateKey = loadPrivateKey();

			long now = System.currentTimeMillis();

			return Jwts.builder().setHeaderParam("kid", keyId).setIssuer(teamId).setIssuedAt(new Date(now))
					.setExpiration(new Date(now + 86400000L * 180)) // 6 months
					.setAudience("https://appleid.apple.com").setSubject(clientId)
					.signWith(privateKey, SignatureAlgorithm.ES256).compact();

		} catch (Exception e) {
			throw new RuntimeException("Apple client secret generation failed", e);
		}
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