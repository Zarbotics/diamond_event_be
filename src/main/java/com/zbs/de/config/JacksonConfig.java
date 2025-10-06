package com.zbs.de.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

	@Bean
	public Module hibernate6Module() {
		Hibernate6Module module = new Hibernate6Module();

		// Do NOT force initialization of lazy-loaded associations.
		// This prevents accidental SELECTs during serialization.
		module.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);

		// Instead, serialize only the identifier for lazy proxies that are NOT
		// initialized.
		// That way the client can see the id (useful) without loading the whole
		// association.
		module.enable(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);

		// Keep default behavior for USE_TRANSIENT_ANNOTATION (optional):
		// module.disable(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION);

		return module;
	}
}
