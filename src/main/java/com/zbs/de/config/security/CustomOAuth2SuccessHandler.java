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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final RepositoryUserMaster userRepo;
	private final JwtTokenUtil jwtUtil;
	private final ServiceRefreshToken serviceRefreshToken;
	private final SsoHandoffService ssoHandoffService;

	/** Where a customer lands after signing in. Configured, not compiled in. */
	@Value("${app.frontend.base-url}")
	private String clientBaseUrl;

	/** Where a member of staff lands after signing in. */
	@Value("${app.frontend.admin-url}")
	private String adminBaseUrl;

	public CustomOAuth2SuccessHandler(RepositoryUserMaster userRepo, JwtTokenUtil jwtUtil,
			ServiceRefreshToken serviceRefreshToken, SsoHandoffService ssoHandoffService) {
		this.userRepo = userRepo;
		this.jwtUtil = jwtUtil;
		this.serviceRefreshToken = serviceRefreshToken;
		this.ssoHandoffService = ssoHandoffService;
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

			// The redirect used to carry ?accessToken=…&refreshToken=… directly.
			// Tokens in a URL land in browser history, server access logs, proxy logs
			// and any Referer header sent onward — and a leaked refresh token is a
			// durable account takeover. It now carries a single-use code that is
			// worthless on its own and is exchanged over POST within two minutes.
			String handoffCode = ssoHandoffService.issue(accessToken, refreshToken.getToken());

			String baseRedirectUrl = SecurityRoles.ADMIN.equals(user.getTxtRole()) ? adminBaseUrl : clientBaseUrl;
			String redirectPath = SecurityRoles.ADMIN.equals(user.getTxtRole()) ? "/admin" : "/client-journey";

			String redirectUrl = String.format("%s%s?code=%s", baseRedirectUrl, redirectPath,
					URLEncoder.encode(handoffCode, StandardCharsets.UTF_8));

			response.sendRedirect(redirectUrl);
			
//			Cookie accessCookie = new Cookie("accessToken", accessToken);
//			accessCookie.setHttpOnly(true);
//			accessCookie.setSecure(true);
//			accessCookie.setPath("/");
//			accessCookie.setMaxAge(60 * 60);
//
//			Cookie refreshCookie = new Cookie("refreshToken", refreshToken.getToken());
//			refreshCookie.setHttpOnly(true);
//			refreshCookie.setSecure(true);
//			refreshCookie.setPath("/");
//			refreshCookie.setMaxAge(7 * 24 * 60 * 60);
//
//			response.addCookie(accessCookie);
//			response.addCookie(refreshCookie);
//
//			response.sendRedirect(baseRedirectUrl + redirectPath);
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