package com.zbs.de.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

@RestController
public class AuthController {

	@GetMapping("/set-redirect")
	public void setRedirect(HttpServletRequest request, HttpServletResponse response, @RequestParam String target)
			throws IOException {
		request.getSession().setAttribute("REDIRECT_URL", target);

		// Start Google OAuth login flow
		response.sendRedirect("/diamond/oauth2/authorization/google");
	}

	@GetMapping("/auth/status")
	public ResponseEntity<?> checkAuthStatus(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
		}
		return ResponseEntity.ok(authentication.getPrincipal());
	}
}