package com.plana.infli.exception.advice;

import static java.lang.Integer.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.plana.infli.web.dto.response.error.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ExceptionAdvice {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(MethodArgumentNotValidException e) {
        ErrorResponse response = ErrorResponse.builder()
                .code(400)
                .message("잘못된 요청입니다")
                .build();

        e.getFieldErrors().forEach(fieldError -> response.addValidation(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.status(e.getStatusCode())
                .body(response);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(MethodArgumentTypeMismatchException e) {
        ErrorResponse response = ErrorResponse.builder()
                .code(400)
                .message("잘못된 요청입니다")
                .build();

        response.addValidation(e.getPropertyName(), "필요한 파라미터 타입 : " + e.getRequiredType().getSimpleName());
        return ResponseEntity.status(response.getCode())
                .body(response);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(BindException e) {
        ErrorResponse response = ErrorResponse.builder()
                .code(400)
                .message("잘못된 요청입니다")
                .build();

        e.getFieldErrors().forEach(fieldError -> response.addValidation(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.status(response.getCode())
                .body(response);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(MissingPathVariableException e) {
        ErrorResponse response = ErrorResponse.builder()
                .code(400)
                .message("Path Variable 값이 입력되지 않았습니다")
                .build();
        response.addValidation(e.getVariableName(), e.getParameter().getParameterType().getSimpleName());

        return ResponseEntity.status(response.getCode())
                .body(response);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(HttpMessageNotReadableException e) {

        ErrorResponse response = ErrorResponse.builder()
                .code(400)
                .message("요청 본문의 형식이 올바르지 않습니다")
                .build();

        return ResponseEntity.status(response.getCode())
                .body(response);
    }
}
