package com.zbs.de.config.security;

import com.zbs.de.model.RefreshToken;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.service.ServiceRefreshToken;
import com.zbs.de.util.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final RepositoryUserMaster userRepo;
	private final JwtTokenUtil jwtUtil;
	private final ServiceRefreshToken serviceRefreshToken;

	public CustomOAuth2SuccessHandler(RepositoryUserMaster userRepo, JwtTokenUtil jwtUtil,
			ServiceRefreshToken serviceRefreshToken) {
		this.userRepo = userRepo;
		this.jwtUtil = jwtUtil;
		this.serviceRefreshToken = serviceRefreshToken;
	}

//	@Override
//	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//			Authentication authentication) throws IOException, ServletException {
//		OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
//		String email = oauthUser.getAttribute("email");
//
//		Optional<UserMaster> userOpt = userRepo.findByTxtEmail(email);
//
//		if (userOpt.isPresent()) {
//			UserMaster user = userOpt.get();
//			String token = jwtUtil.generateToken(user.getSerUserId().intValue(), user.getTxtEmail(), user.getTxtRole()); // implement
//			RefreshToken refreshToken = serviceRefreshToken.createRefreshToken(user);																										// this
//			// method in
//			// JwtUtil
//
//			// Send token as JSON response
//			response.setContentType("application/json");
//			response.setCharacterEncoding("UTF-8");
//
//			new ObjectMapper().writeValue(response.getWriter(), new JwtResponse(token,refreshToken.getToken(), user.getSerUserId().intValue(),
//					user.getTxtName(), user.getTxtEmail(), user.getTxtRole()));
//		} else {
//			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//			response.getWriter().write("User not found in DB");
//		}
//	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
		String email = oauthUser.getAttribute("email");

		Optional<UserMaster> userOpt = userRepo.findByTxtEmail(email);

		if (userOpt.isPresent()) {
			UserMaster user = userOpt.get();

			String accessToken = jwtUtil.generateToken(user.getSerUserId().intValue(), user.getTxtEmail(),
					user.getTxtRole());

			RefreshToken refreshToken = serviceRefreshToken.createRefreshToken(user);

			// Construct redirect URL with query params
			String baseRedirectUrl = "http://localhost:5173"; //Local Testing URL
//			String baseRedirectUrl = "https://frosty-jang.87-106-101-41.plesk.page";
			
			String redirectPath = "ROLE_ADMIN".equals(user.getTxtRole()) ? "/admin" : "/client-journey";

			String redirectUrl = String.format("%s%s?accessToken=%s&refreshToken=%s", baseRedirectUrl, redirectPath,
					accessToken, refreshToken.getToken());

			response.sendRedirect(redirectUrl);
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("User not found in DB");
		}
	}

	// Helper inner class for the JSON response
	static class JwtResponse {
		public String accessToken;
		public String refreshToken;
		public Integer userId;
		public String name;
		public String email;
		public String role;

		public JwtResponse(String accessToken, String refreshToken, Integer userId, String name, String email,
				String role) {
			this.accessToken = accessToken;
			this.refreshToken = refreshToken;
			this.userId = userId;
			this.name = name;
			this.email = email;
			this.role = role;
		}
	}
}