package com.plana.infli.infra.exception.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.plana.infli.web.dto.response.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class S3ExceptionAdvice {


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(SdkClientException e) {
        ErrorResponse response = ErrorResponse.builder()
                .code(500)
                .message("이미지 업로드에 실패했습니다")
                .build();

        return ResponseEntity.status(500)
                .body(response);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(AmazonServiceException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(AmazonServiceException e) {
        ErrorResponse response = ErrorResponse.builder()
                .code(500)
                .message("이미지 업로드에 실패했습니다")
                .build();

        return ResponseEntity.status(500)
                .body(response);
    }
}
