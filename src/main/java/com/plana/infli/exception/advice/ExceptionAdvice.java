package com.plana.infli.exception.advice;

import static java.lang.Integer.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.plana.infli.web.dto.response.error.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAdvice {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(MethodArgumentNotValidException e) {
        ErrorResponse response = ErrorResponse.builder()
                .code("400")
                .message("잘못된 요청입니다")
                .build();

        e.getFieldErrors().forEach(fieldError -> response.addValidation(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.status(e.getStatusCode())
                .body(response);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(BindException e) {
        ErrorResponse response = ErrorResponse.builder()
                .code("400")
                .message("잘못된 요청입니다")
                .build();

        e.getFieldErrors().forEach(fieldError -> response.addValidation(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.status(parseInt(response.getCode()))
                .body(response);
    }
}
