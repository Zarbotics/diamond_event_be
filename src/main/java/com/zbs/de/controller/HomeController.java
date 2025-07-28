package com.zbs.de.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

	@GetMapping("/")
	public String home() {
		return "Home Page - Not Secured";
	}

	@GetMapping("/secured")
	public String secured() {
		return "This is a secured page!";
	}
}