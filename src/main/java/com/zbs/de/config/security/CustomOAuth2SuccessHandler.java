package com.zbs.de.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
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

	public CustomOAuth2SuccessHandler(RepositoryUserMaster userRepo, JwtTokenUtil jwtUtil) {
		this.userRepo = userRepo;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
		String email = oauthUser.getAttribute("email");

		Optional<UserMaster> userOpt = userRepo.findByTxtEmail(email);

		if (userOpt.isPresent()) {
			UserMaster user = userOpt.get();
			String token = jwtUtil.generateToken(user.getSerUserId().intValue(), user.getTxtEmail(), user.getTxtRole()); // implement this
																										// method in
																										// JwtUtil

			// Send token as JSON response
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");

			new ObjectMapper().writeValue(response.getWriter(),
					new JwtResponse(token, user.getSerUserId().intValue(), user.getTxtName(), user.getTxtEmail()));
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("User not found in DB");
		}
	}

	// Helper inner class for the JSON response
	static class JwtResponse {
		public String token;
		public Integer userId;
		public String name;
		public String email;

		public JwtResponse(String token, Integer userId, String name, String email) {
			this.token = token;
			this.userId = userId;
			this.name = name;
			this.email = email;
		}
	}
}