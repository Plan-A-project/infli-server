package com.plana.infli.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final String tokenPrefix;
	private final String accessTokenHeaderName;
	private final String refreshTokenHeaderName;

	private final JwtManager jwtManager;

	public JwtAuthenticationSuccessHandler(
		@Value("${jwt.header.token-prefix}") String tokenPrefix,
		@Value("${jwt.header.access-token-header-name}") String accessTokenHeaderName,
		@Value("${jwt.header.refresh-token-header-name}") String refreshTokenHeaderName,
		JwtManager jwtManager) {

		this.tokenPrefix = tokenPrefix;
		this.accessTokenHeaderName = accessTokenHeaderName;
		this.refreshTokenHeaderName = refreshTokenHeaderName;
		this.jwtManager = jwtManager;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		UserDetails userDetails = (UserDetails)authentication.getPrincipal();

		String accessToken = jwtManager.generateAccessToken(userDetails);
		String refreshToken = jwtManager.generateRefreshToken(userDetails);

		response.addHeader(accessTokenHeaderName, tokenPrefix + " " + accessToken);
		response.addHeader(refreshTokenHeaderName, tokenPrefix + " " + refreshToken);
	}
}
