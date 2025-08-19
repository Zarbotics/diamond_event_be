package com.zbs.de.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.zbs.de.config.security.CustomOAuth2SuccessHandler;
import com.zbs.de.config.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Autowired
	private CustomOAuth2UserService customOAuth2UserService;

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Autowired
	private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults()) // <-- enable CORS for security layer
				.authorizeHttpRequests(auth -> auth
						// Allow OPTIONS preflight requests
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/", "/auth/login**", "/auth/logout**", "/auth/signup**", "/auth/confirm**","/deimg**",
								"/auth/refresh-token**", "/login**", "/error", "/public/**")
						.permitAll().anyRequest().authenticated())
				.oauth2Login(
						oauth2 -> oauth2.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
								.successHandler(customOAuth2SuccessHandler))
				.logout(logout -> logout.logoutSuccessUrl("http://localhost:5173/login").permitAll());

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}