package com.plana.infli.security.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtManager jwtManager;
	private final JwtProperties jwtProperties;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain chain) throws ServletException, IOException {

		String accessTokenHeader = parseAccessTokenHeader(request);
		if (accessTokenHeader == null || !accessTokenHeader.startsWith(jwtProperties.getTokenPrefix())) {
			chain.doFilter(request, response);
			return;
		}

		String accessToken = accessTokenHeader.substring(jwtProperties.getTokenPrefix().length() + 1);
		Authentication authResult = jwtManager.resolveAccessToken(accessToken);

		SecurityContextHolder.getContext().setAuthentication(authResult);

		chain.doFilter(request, response);
	}

	private String parseAccessTokenHeader(HttpServletRequest request) {
		return request.getHeader(jwtProperties.getAccessTokenHeaderName());
	}
}
