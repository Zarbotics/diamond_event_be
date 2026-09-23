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

	/*
	 * The default is here as well as in application.properties, deliberately.
	 *
	 * These four properties — this one and the three frontend URLs — live only
	 * in the base application.properties. Anyone running with their own
	 * properties file, which is the normal way to hold local credentials, gets
	 * them from the base layered underneath. When that layering does not happen
	 * the application refused to start, four times in a row, each time naming
	 * one property and saying nothing about why a file full of settings had
	 * stopped being read.
	 *
	 * A development default turns that into an application that starts and
	 * works on localhost. It is safe to have one because production cannot
	 * inherit it: ProductionConfigCheck refuses to start when the allowed
	 * origins still mention localhost, and there is a test for that.
	 */
	@Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost:5174,http://127.0.0.1:5174,http://localhost:5175,http://127.0.0.1:5175,http://localhost:3000,http://127.0.0.1:3000}")
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
