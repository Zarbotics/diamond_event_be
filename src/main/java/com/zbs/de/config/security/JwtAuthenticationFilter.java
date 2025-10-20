package com.zbs.de.config.security;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.util.JwtTokenUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = extractToken(request);

		if (token != null) {
			try {
				Claims claims = jwtTokenUtil.validateToken(token);

				// Check if token expired
				if (claims.getExpiration().before(new Date())) {
					throw new ExpiredJwtException(null, claims, "Token expired");
				}

				String email = claims.getSubject();
				Integer userId = (Integer) claims.get("userId");

				Optional<UserMaster> userOpt = repositoryUserMaster.findByTxtEmail(email);
				if (userOpt.isPresent()) {
					UserMaster user = userOpt.get();

					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
							List.of());
					SecurityContextHolder.getContext().setAuthentication(auth);
				} else {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.getWriter().write("Invalid user");
					return;
				}

			} catch (ExpiredJwtException ex) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Access token expired");
				return;

			} catch (Exception ex) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Invalid or malformed token");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private String extractToken(HttpServletRequest request) {
		String bearer = request.getHeader("Authorization");
		if (bearer != null && bearer.startsWith("Bearer ")) {
			return bearer.substring(7);
		}
		return null;
	}
}