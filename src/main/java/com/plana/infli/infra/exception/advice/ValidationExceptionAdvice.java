package com.plana.infli.infra.exception.advice;

import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.plana.infli.web.dto.response.error.ErrorResponse;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ValidationExceptionAdvice {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
		ErrorResponse body = ErrorResponse.builder()
			.code(HttpServletResponse.SC_BAD_REQUEST)
			.message(e.getBindingResult().getFieldErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.collect(Collectors.joining(" ")))
			.build();

		return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(body);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> constraintViolationExceptionHandler(ConstraintViolationException e) {
		ErrorResponse body = ErrorResponse.builder()
			.code(HttpServletResponse.SC_BAD_REQUEST)
			.message(e.getMessage())
			.build();

		return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(body);
	}
}
