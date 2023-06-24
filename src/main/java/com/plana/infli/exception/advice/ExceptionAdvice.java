package com.plana.infli.exception.advice;

import static org.springframework.http.HttpStatus.*;

import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.plana.infli.web.dto.response.error.ErrorResponse;

@RestControllerAdvice
public class ExceptionAdvice {

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ErrorResponse exceptionHandler(MethodArgumentNotValidException e) {
		ErrorResponse response = ErrorResponse.builder()
			.code(400)
			.message("잘못된 요청입니다")
			.build();

		e.getFieldErrors()
			.forEach(fieldError -> response.addValidation(fieldError.getField(), fieldError.getDefaultMessage()));
		return response;
	}

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(BindException.class)
	public ErrorResponse exceptionHandler(BindException e) {
		ErrorResponse response = ErrorResponse.builder()
			.code(400)
			.message("잘못된 요청입니다")
			.build();

		e.getFieldErrors()
			.forEach(fieldError -> response.addValidation(fieldError.getField(), fieldError.getDefaultMessage()));
		return response;
	}

}
