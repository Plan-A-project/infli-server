package com.plana.infli.exception.advice;

import static java.lang.String.valueOf;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.plana.infli.exception.custom.DefaultException;
import com.plana.infli.web.dto.response.error.ErrorResponse;

@RestControllerAdvice
public class DefaultExceptionAdvice {

	@ExceptionHandler(DefaultException.class)
	public ResponseEntity<ErrorResponse> defaultExceptionHandler(DefaultException e) {
		int statusCode = e.getStatusCode();

        ErrorResponse body = ErrorResponse.builder()
                .code(statusCode)
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(statusCode)
                .body(body);
    }

    
}
