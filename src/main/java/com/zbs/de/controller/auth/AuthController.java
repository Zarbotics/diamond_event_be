package com.zbs.de.controller.auth;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import com.zbs.de.model.RefreshToken;
import com.zbs.de.model.UserMaster;
import com.zbs.de.model.dto.DtoLoginRequest;
import com.zbs.de.model.dto.DtoSignupRequest;
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
	public ResponseEntity<?> logout(@RequestBody Map<String, String> body, HttpServletRequest request)
			throws ServletException {
		String refreshToken = body.get("refreshToken");
		if (refreshToken != null) {
			refreshTokenService.findByToken(refreshToken).ifPresent(token -> {
				refreshTokenService.deleteByUserId(token.getUser().getSerUserId().longValue());
			});
		}

		request.logout(); // ends SSO session if still in memory (mostly useful in UI-based login)

		return ResponseEntity.ok("Logged out successfully");
	}

	@PostMapping("/login")
	public ResponseEntity<?> loginWithEmailPassword(@RequestBody DtoLoginRequest req) throws Exception {
		Optional<UserMaster> userOpt = repositoryUserMaster.findByTxtEmail(req.getEmail());
		if (userOpt.isEmpty()) {
			throw new Exception("No account found with this email.");
		}

		UserMaster user = userOpt.get();

		// check password (BCrypt)
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		if (!encoder.matches(req.getPassword(), user.getTxtPassword())) {
			throw new Exception("Invalid email or password.");
		}

		// check email verified
		if (!Boolean.TRUE.equals(user.getBlnEmailVerified())) {
			throw new Exception("Please verify your email before logging in.");
		}

		// Generate tokens using your JwtTokenUtil and RefreshTokenService
		String accessToken = jwtTokenUtil.generateToken(user.getSerUserId().intValue(), user.getTxtEmail(),
				user.getTxtRole());
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

		return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken.getToken(), "userId",
				user.getSerUserId(), "email", user.getTxtEmail(), "name", user.getTxtName(), "role",
				user.getTxtRole()));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody DtoSignupRequest req) throws Exception {
		// 1. Check if user exists
		Optional<UserMaster> existingUser = repositoryUserMaster.findByTxtEmail(req.getEmail());
		if (existingUser.isPresent()) {
			throw new Exception("Email is already registered.");
		}

		// 2. Encode password
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String hashedPassword = encoder.encode(req.getPassword());

		// 3. Create new user
		UserMaster newUser = new UserMaster();
		newUser.setTxtEmail(req.getEmail());
		newUser.setTxtPassword(hashedPassword);
		if (req.getFirstName() != null) {
			if (req.getLastName() != null) {
				newUser.setTxtName(req.getFirstName() + " " + req.getLastName());
			} else {
				newUser.setTxtName(req.getFirstName());
			}

		}
		newUser.setTxtFirstName(req.getFirstName());
		newUser.setTxtLastName(req.getLastName());
		newUser.setBlnEmailVerified(false); // Require email verification before login
		newUser.setBlnIsActive(true);
		newUser.setBlnIsDeleted(false);

		repositoryUserMaster.save(newUser);

		// TODO: Send verification email (implement later)

		return ResponseEntity.ok(Map.of("message", "Signup successful. Please verify your email before logging in.",
				"email", newUser.getTxtEmail()));
	}
}