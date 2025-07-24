package com.zbs.de.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsGlobalConfig {

//	@Bean
//	public WebMvcConfigurer corsConfigurer() {
//		return new WebMvcConfigurer() {
//			@Override
//			public void addCorsMappings(CorsRegistry registry) {
//				registry.addMapping("/**")
//						.allowedOrigins("http://localhost:5173", "https://localhost:5173", "http://87.106.101.41","https://frosty-jang.87-106-101-41.plesk.page:8081",
//								"https://87.106.101.41", "https://frosty-jang.87-106-101-41.plesk.page")
//						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").allowedHeaders("*")
//						.allowCredentials(true);
//			}
//		};
//	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/").allowedOrigins("*").allowedMethods("*").allowedHeaders("*")
						.allowCredentials(true);
			}
		};
	}
}
