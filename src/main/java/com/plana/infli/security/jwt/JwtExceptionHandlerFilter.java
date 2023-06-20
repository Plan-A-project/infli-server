package com.plana.infli.security.jwt;

import java.io.IOException;

import org.springframework.web.filter.GenericFilterBean;

import com.auth0.jwt.exceptions.TokenExpiredException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtExceptionHandlerFilter extends GenericFilterBean {

	private final JwtTokenExpiredEntrypoint jwtTokenExpiredEntrypoint;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException, ServletException {
		doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
	}

	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		try {
			chain.doFilter(request, response);
		} catch (TokenExpiredException e) {
			jwtTokenExpiredEntrypoint.commence(request, response);
		}
	}

}
