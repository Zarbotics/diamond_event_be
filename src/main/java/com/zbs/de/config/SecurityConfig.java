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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.zbs.de.config.security.CustomOAuth2SuccessHandler;
import com.zbs.de.config.security.JwtAuthenticationFilter;
import com.zbs.de.repository.RepositoryUserMaster;

@Configuration
public class SecurityConfig {

	@Autowired
	private CustomOAuth2UserService customOAuth2UserService;

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@Autowired
	private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.requestMatchers("/", "/login**", "/error", "/public/**").permitAll()
						.anyRequest().authenticated())
				.oauth2Login(
						oauth2 -> oauth2.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
								.successHandler(customOAuth2SuccessHandler))
				.logout(logout -> logout.logoutSuccessUrl("http://localhost:5173/login") // React login page
						.permitAll());
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler() {
		return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
			OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
			String email = oAuth2User.getAttribute("email");

			var userOpt = repositoryUserMaster.findByTxtEmail(email);
			if (userOpt.isEmpty()) {
				response.sendRedirect("http://localhost:5173/error");
				return;
			}

			var user = userOpt.get();

			// Local role-based redirection to React frontend
			if ("ROLE_ADMIN".equals(user.getTxtRole())) {
				response.sendRedirect("http://localhost:5173/admin");
			} else {
				response.sendRedirect("http://localhost:5173/client-journey");
			}
		};
	}
}