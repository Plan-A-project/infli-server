package com.plana.infli.security.jwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.web.dto.response.error.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenExpiredEntrypoint {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static final int SC_TOKEN_EXPIRED = 499;

	private static final String TOKEN_EXPIRED_MESSAGE = "토큰이 만료되었습니다.";

	public void commence(HttpServletRequest request, HttpServletResponse response) throws IOException {

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code(SC_TOKEN_EXPIRED)
			.message(TOKEN_EXPIRED_MESSAGE)
			.build();

		String body = objectMapper.writeValueAsString(errorResponse);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(body);
	}
}
