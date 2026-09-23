package com.zbs.de.config.security;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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

				Optional<UserMaster> userOpt = repositoryUserMaster.findByTxtEmail(email);
				if (userOpt.isPresent()) {
					UserMaster user = userOpt.get();

					// The role is read from the user record rather than from the token claim.
					// A token minted before a role change must not keep the old privileges, and
					// a tampered claim must never be able to grant one.
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
							authoritiesFor(user));
					auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(auth);
				} else {
					unauthorized(response, "Invalid user");
					return;
				}

			} catch (ExpiredJwtException ex) {
				unauthorized(response, "Access token expired");
				return;

			} catch (Exception ex) {
				unauthorized(response, "Invalid or malformed token");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * Maps the user's stored role onto granted authorities.
	 *
	 * <p>
	 * An account with a missing or unrecognised role is treated as a customer,
	 * never as an administrator, so that bad data fails closed.
	 */
	private List<SimpleGrantedAuthority> authoritiesFor(UserMaster user) {
		String role = user.getTxtRole();
		if (SecurityRoles.ADMIN.equals(role)) {
			return List.of(new SimpleGrantedAuthority(SecurityRoles.ADMIN));
		}
		return List.of(new SimpleGrantedAuthority(SecurityRoles.USER));
	}

	private void unauthorized(HttpServletResponse response, String message) throws IOException {
		SecurityContextHolder.clearContext();
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"status\":401,\"message\":\"" + message + "\"}");
	}

	private String extractToken(HttpServletRequest request) {
		String bearer = request.getHeader("Authorization");
		if (bearer != null && bearer.startsWith("Bearer ")) {
			return bearer.substring(7);
		}
		return null;
	}
}
