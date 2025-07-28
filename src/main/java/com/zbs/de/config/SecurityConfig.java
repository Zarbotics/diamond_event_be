package com.zbs.de.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.zbs.de.repository.RepositoryUserMaster;

@Configuration
public class SecurityConfig {

	@Autowired
	private CustomOAuth2UserService customOAuth2UserService;

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.requestMatchers("/", "/login**", "/error", "/public/**").permitAll()
						.anyRequest().authenticated())
				.oauth2Login(
						oauth2 -> oauth2.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
								.successHandler(authenticationSuccessHandler()))
				.logout(logout -> logout.logoutSuccessUrl("/").permitAll());

		return http.build();
	}

	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler() {
		return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {

			OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
			String email = oAuth2User.getAttribute("email");

			var userOpt = repositoryUserMaster.findByTxtEmail(email);
			if (userOpt.isEmpty()) {
				response.sendRedirect("/error");
				return;
			}

			var user = userOpt.get();

			// Role-based redirect
			if ("ROLE_ADMIN".equals(user.getTxtRole())) {
				response.sendRedirect("https://frosty-jang.87-106-101-41.plesk.page/admin");
			} else {
				response.sendRedirect("https://frosty-jang.87-106-101-41.plesk.page/client-journey");
			}
		};
	}
}