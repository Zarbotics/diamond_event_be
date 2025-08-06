package com.zbs.de.config.security;

import java.io.IOException;
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
				String email = claims.getSubject();
				Integer userId = (Integer) claims.get("userId");

				Optional<UserMaster> userOpt = repositoryUserMaster.findByTxtEmail(email);
				if (userOpt.isPresent()) {
					UserMaster user = userOpt.get();

					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
							List.of());
					SecurityContextHolder.getContext().setAuthentication(auth);
				}

			} catch (Exception e) {
				// token invalid
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