package com.plana.infli.config;

import java.io.IOException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtVerificationFilter extends OncePerRequestFilter {

	private final String tokenPrefix;
	private final String accessTokenHeaderName;

	private final JwtManager jwtManager;

	public JwtVerificationFilter(
		@Value("${jwt.header.token-prefix}") String tokenPrefix,
		@Value("${jwt.header.access-token-header-name}") String accessTokenHeaderName,
		JwtManager jwtManager) {
		this.tokenPrefix = tokenPrefix;
		this.accessTokenHeaderName = accessTokenHeaderName;
		this.jwtManager = jwtManager;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String token = parseAccessToken(request);

		try {
			Authentication authentication = jwtManager.getAuthentication(token);
			if (Objects.nonNull(authentication)) {
				SecurityContextHolder.getContext().setAuthentication(authentication);

			}
		} catch (JWTVerificationException e) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	private String parseAccessToken(HttpServletRequest request) {
		String token = request.getHeader(accessTokenHeaderName);
		if (!StringUtils.hasText(token)) {
			return null;
		}

		return token.substring(tokenPrefix.length() + 1);
	}
}
