package com.zbs.de.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.zbs.de.config.security.CustomOAuth2SuccessHandler;
import com.zbs.de.config.security.ApiAuthenticationEntryPoint;
import com.zbs.de.config.security.DelegatingTokenResponseClient;
import com.zbs.de.config.security.JwtAuthenticationFilter;
import com.zbs.de.config.security.PortalEndpoints;
import com.zbs.de.config.security.UnconfiguredProviderFilter;
import com.zbs.de.config.security.SecurityRoles;

/**
 * Application security.
 *
 * <p>
 * The authorisation model is <strong>default-deny</strong>:
 *
 * <ol>
 * <li>{@link PortalEndpoints#PUBLIC} — open, no authentication.</li>
 * <li>{@link PortalEndpoints#allCustomerAccessible()} — any authenticated user
 * (customer or staff).</li>
 * <li>Everything else — {@link SecurityRoles#ADMIN} only.</li>
 * </ol>
 *
 * <p>
 * This replaces a chain that ended in {@code anyRequest().authenticated()},
 * under which any customer who signed in could call every administrative
 * endpoint in the application, including reading the full customer list.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Autowired
	private CustomOAuth2UserService customOAuth2UserService;

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Autowired
	private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

	@Autowired
	private DelegatingTokenResponseClient delegatingTokenResponseClient;

	@Autowired
	private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

	@Autowired
	private UnconfiguredProviderFilter unconfiguredProviderFilter;

	@Value("${app.frontend.logout-redirect-url}")
	private String logoutRedirectUrl;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// The API is stateless and token-authenticated; there is no session cookie for
				// a cross-site request to ride, which is what CSRF protection defends.
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				// Without this, oauth2Login answers an unauthenticated API call with a
				// 302 to the provider. The frontend refreshes its token on a 401 and
				// follows a 302 silently, so a redirect leaves the customer looking at
				// a screen that never recovers.
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(apiAuthenticationEntryPoint)
						.accessDeniedHandler(apiAuthenticationEntryPoint))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(PortalEndpoints.PUBLIC).permitAll()
						.requestMatchers(PortalEndpoints.allCustomerAccessible())
						.hasAnyAuthority(SecurityRoles.USER, SecurityRoles.ADMIN)
						// Default deny. Anything not explicitly opened above is back-office only.
						.anyRequest().hasAuthority(SecurityRoles.ADMIN))
				.oauth2Login(oauth2 -> oauth2
						.tokenEndpoint(token -> token.accessTokenResponseClient(delegatingTokenResponseClient))
						.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
						.successHandler(customOAuth2SuccessHandler))
				.logout(logout -> logout.logoutSuccessUrl(logoutRedirectUrl).permitAll());

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		/*
		 * Ahead of oauth2Login's own redirect. Once that filter has run the
		 * browser is already on its way to the provider, and the only party who
		 * can explain a placeholder credential has lost its chance to.
		 */
		http.addFilterBefore(unconfiguredProviderFilter, OAuth2AuthorizationRequestRedirectFilter.class);

		return http.build();
	}
}
