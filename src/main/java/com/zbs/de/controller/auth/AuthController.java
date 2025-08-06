package com.zbs.de.controller.auth;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import com.zbs.de.model.RefreshToken;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.service.ServiceRefreshToken;
import com.zbs.de.util.JwtTokenUtil;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	@Autowired
	private ServiceRefreshToken refreshTokenService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@PostMapping("/refresh-token")
	public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
		String requestToken = body.get("refreshToken");

		if (requestToken == null || requestToken.isBlank()) {
			return ResponseEntity.badRequest().body("Refresh token is required");
		}

		Optional<RefreshToken> optionalToken = refreshTokenService.findByToken(requestToken);

		if (optionalToken.isEmpty()) {
			return ResponseEntity.status(403).body("Refresh token not found");
		}

		try {
			RefreshToken verifiedToken = refreshTokenService.verifyExpiration(optionalToken.get());
			UserMaster user = verifiedToken.getUser();

			String newAccessToken = jwtTokenUtil.generateToken(user.getSerUserId().intValue(), user.getTxtEmail(),
					user.getTxtRole());

			return ResponseEntity.ok(Map.of("accessToken", newAccessToken, "refreshToken", verifiedToken.getToken()));
		} catch (Exception ex) {
			return ResponseEntity.status(403).body("Refresh token expired or invalid");
		}
	}

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