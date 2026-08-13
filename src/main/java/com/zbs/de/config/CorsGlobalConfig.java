package com.zbs.de.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cross-origin rules for the two browser clients.
 *
 * <p>
 * The allowed origins come from {@code app.cors.allowed-origins} rather than
 * being compiled in, so that adding a staging host no longer requires a code
 * change and production no longer trusts {@code localhost} or retired preview
 * domains.
 */
@Configuration
public class CorsGlobalConfig {

	@Value("${app.cors.allowed-origins}")
	private String[] allowedOrigins;

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins(allowedOrigins)
						.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true)
						.maxAge(3600);
			}
		};
	}
}
