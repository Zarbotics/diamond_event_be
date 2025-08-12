package com.zbs.de;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeApplication.class, args);
	}

}
