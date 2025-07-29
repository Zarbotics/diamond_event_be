package com.zbs.de.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	@GetMapping("/status")
	public ResponseEntity<?> checkAuthStatus(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(401).body("Not logged in");
		}
		return ResponseEntity.ok(authentication.getPrincipal());
	}

	@GetMapping("/me")
	public ResponseMessage getLoggedInUser(@AuthenticationPrincipal OAuth2User principal) {
		if (principal == null) {
			return new ResponseMessage(false, "User not logged in");
		}

		String email = principal.getAttribute("email");
		var userOpt = repositoryUserMaster.findByTxtEmail(email);
		if (userOpt.isPresent()) {
			UserMaster user = userOpt.get();
			return new ResponseMessage(true, "Success", user); // You can create DTO for privacy if needed
		} else {
			return new ResponseMessage(false, "User not found in DB");
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		request.logout(); // this clears the Spring session
		return ResponseEntity.ok("Logged out");
	}
}