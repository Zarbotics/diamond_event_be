
package com.zbs.de.config.security;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.*;
import org.springframework.stereotype.Component;
import org.springframework.util.*;
import org.springframework.web.client.RestClient;

@Component
public class AppleTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

	private final AppleClientSecretGenerator generator;
	private final RestClient restClient;

	public AppleTokenResponseClient(AppleClientSecretGenerator generator) {
		this.generator = generator;
		this.restClient = RestClient.builder().build();
	}

	@Override
	public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {

		String tokenUri = request.getClientRegistration().getProviderDetails().getTokenUri();

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("code", request.getAuthorizationExchange().getAuthorizationResponse().getCode());
//		params.add("redirect_uri", request.getClientRegistration().getRedirectUri());
		params.add("redirect_uri",
		        request.getAuthorizationExchange()
		               .getAuthorizationRequest()
		               .getRedirectUri());
		params.add("client_id", request.getClientRegistration().getClientId());
		params.add("client_secret", generator.generateClientSecret());

		Map<String, Object> tokenResponse = (Map<String, Object>) restClient.post().uri(tokenUri)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).body(params).retrieve().body(Map.class);

		return OAuth2AccessTokenResponse.withToken((String) tokenResponse.get("access_token"))
				.tokenType(OAuth2AccessToken.TokenType.BEARER)
				.expiresIn(((Number) tokenResponse.get("expires_in")).longValue())
				.refreshToken((String) tokenResponse.get("refresh_token")).additionalParameters(tokenResponse).build();
	}
}
