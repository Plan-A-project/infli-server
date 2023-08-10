package com.plana.infli.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

public class JsonLoginProcessingFilter extends AbstractAuthenticationProcessingFilter {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public JsonLoginProcessingFilter(
		RequestMatcher requiresAuthenticationRequestMatcher,
		AuthenticationManager authenticationManager,
		AuthenticationSuccessHandler authenticationSuccessHandler,
		AuthenticationFailureHandler authenticationFailureHandler) {
		super(requiresAuthenticationRequestMatcher, authenticationManager);
		setAuthenticationSuccessHandler(authenticationSuccessHandler);
		setAuthenticationFailureHandler(authenticationFailureHandler);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException, IOException, ServletException {
		if (!request.getMethod().equals("POST")) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}

		if (!request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) {
			throw new AuthenticationServiceException(
				"Authentication content-type not supported: " + request.getContentType());
		}

		ServletInputStream inputStream = request.getInputStream();
		LoginUser loginUser = objectMapper.readValue(inputStream, LoginUser.class);
		UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
			loginUser.getUsername(), loginUser.getPassword());
		return getAuthenticationManager().authenticate(authRequest);
	}

	@Getter
	private static class LoginUser {
		private String username;
		private String password;
	}

}
