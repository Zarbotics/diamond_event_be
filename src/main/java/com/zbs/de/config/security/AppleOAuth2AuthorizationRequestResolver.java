package com.zbs.de.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AppleOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

	private final OAuth2AuthorizationRequestResolver defaultResolver;
	private final AppleClientSecretGenerator generator;

	public AppleOAuth2AuthorizationRequestResolver(ClientRegistrationRepository repo,
			AppleClientSecretGenerator generator) {

		this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");

		this.generator = generator;
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
		return customize(defaultResolver.resolve(request));
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientId) {
		return customize(defaultResolver.resolve(request, clientId));
	}

	private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req) {
		if (req == null)
			return null;

		if ("apple".equals(req.getAttributes().get("registration_id"))) {

			Map<String, Object> params = new HashMap<>(req.getAdditionalParameters());
			params.put("client_secret", generator.generateClientSecret());

			return OAuth2AuthorizationRequest.from(req).additionalParameters(params).build();
		}

		return req;
	}
}