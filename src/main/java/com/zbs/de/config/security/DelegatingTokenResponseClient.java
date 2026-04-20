package com.zbs.de.config.security;

import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;

@Component
public class DelegatingTokenResponseClient
        implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final AppleTokenResponseClient appleClient;
    private final DefaultAuthorizationCodeTokenResponseClient defaultClient;

    public DelegatingTokenResponseClient(AppleTokenResponseClient appleClient) {
        this.appleClient = appleClient;
        this.defaultClient = new DefaultAuthorizationCodeTokenResponseClient();
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {

        String registrationId = request.getClientRegistration().getRegistrationId();

        if ("apple".equals(registrationId)) {
            return appleClient.getTokenResponse(request);
        }

        return defaultClient.getTokenResponse(request);
    }
}