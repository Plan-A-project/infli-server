package com.plana.infli.exception.advice;

import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.plana.infli.web.dto.response.error.ErrorResponse;

@RestControllerAdvice
public class MethodArgumentNotValidAdvice {

	public static final int STATUS_CODE = 400;

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
		ErrorResponse body = ErrorResponse.builder()
			.code(String.valueOf(STATUS_CODE))
			.message(e.getBindingResult().getFieldErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.collect(Collectors.joining(" ")))
			.build();

		return ResponseEntity.status(STATUS_CODE).body(body);
	}
}
