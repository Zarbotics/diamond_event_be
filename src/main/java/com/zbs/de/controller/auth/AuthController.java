package com.zbs.de.controller.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import com.zbs.de.model.EmailVerificationToken;
import com.zbs.de.model.RefreshToken;
import com.zbs.de.model.UserMaster;
import com.zbs.de.model.dto.DtoLoginRequest;
import com.zbs.de.model.dto.DtoSignupRequest;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.service.ServiceEmailSender;
import com.zbs.de.service.ServiceEmailVerification;
import com.zbs.de.service.ServiceRefreshToken;
import com.zbs.de.util.JwtTokenUtil;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	@Autowired
	private ServiceRefreshToken refreshTokenService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private ServiceEmailVerification serviceEmailVerification;

	@Autowired
	private ServiceEmailSender serviceEmailSender;

	@Value("${app.frontend-email-verification-url}")
	private String frontEndVerificationPageUrl;

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
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseMessage(
					HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED, "No account found with this email."));

		}

		UserMaster user = userOpt.get();

		// check password (BCrypt)
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		if (!encoder.matches(req.getPassword(), user.getTxtPassword())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseMessage(
					HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED, "Invalid email or password.", null));

		}

		// check email verified
		if (!Boolean.TRUE.equals(user.getBlnEmailVerified())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMessage(HttpStatus.FORBIDDEN.value(),
					HttpStatus.FORBIDDEN, "Please verify your email before logging in.", null));

		}

		// Generate tokens using your JwtTokenUtil and RefreshTokenService
		String accessToken = jwtTokenUtil.generateToken(user.getSerUserId().intValue(), user.getTxtEmail(),
				user.getTxtRole());
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

		Map<String, Object> data = Map.of("accessToken", accessToken, "refreshToken", refreshToken.getToken(), "userId",
				user.getSerUserId(), "email", user.getTxtEmail(), "name", user.getTxtName(), "role", user.getTxtRole());

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Success", data));

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
		newUser.setTxtRole("ROLE_ADMIN");
		newUser.setTxtLastName(req.getLastName());
		newUser.setBlnEmailVerified(false); // Require email verification before login
		newUser.setBlnIsActive(true);
		newUser.setBlnIsDeleted(false);

		repositoryUserMaster.save(newUser);

		// *********** Email Verification Token Generation **********
		EmailVerificationToken token = serviceEmailVerification.createToken(newUser);
		String confirmationLink = null;
		if (token != null) {
			confirmationLink = frontEndVerificationPageUrl + "?token=" + token.getToken();
		}

		String expiryTime = "24 hours"; // or read from your token expiration setting

		String emailContent = """
				    Dear %s,

				    Welcome to Diamond Events! 🎉

				    Thank you for creating your account with us.
				    To complete your registration and start exploring our services, please verify your email address.

				    ✅ Click the link below to verify your email:
				    %s

				    📅 This link will expire in %s for security purposes.
				    If the link expires before you verify, you will need to request a new verification email.

				    ❓ If you did not sign up for a Diamond Events account, or if you have any questions,
				    please contact our support team immediately at support@diamondevents.com.

				    We look forward to making your events unforgettable!

				    Best regards,
				    Diamond Events Team
				""".formatted(newUser.getTxtFirstName(), confirmationLink, expiryTime);

		// Send confirmation email
		serviceEmailSender.sendEmail(newUser.getTxtEmail(), "Confirm Your Diamond Events Account", emailContent);

		// TODO: Send verification email (implement later)

		return ResponseEntity.ok(Map.of("message", "Signup successful. Please verify your email before logging in.",
				"email", newUser.getTxtEmail()));
	}

	@GetMapping("/confirm")
	public ResponseEntity<Map<String, Object>> confirmEmail(@RequestParam String token) {
		Map<String, Object> response = new HashMap<>();
		boolean isVerified = serviceEmailVerification.verifyToken(token);
		if (isVerified) {
			response.put("status", "success");
			response.put("message", "Email verified successfully!");
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			response.put("status", "error");
			response.put("message", "Invalid or expired token.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}
}