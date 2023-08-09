package com.plana.infli.web.dto.response;

import static org.springframework.http.HttpStatus.*;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {

    private final int code;

    private final HttpStatus status;

    private final String message;

    private final T data;

    public ApiResponse(HttpStatus status, String message, T data) {
        this.code = status.value();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> of(HttpStatus httpStatus, String message, T data) {

        return new ApiResponse<>(httpStatus, message, data);
    }

    public static <T> ApiResponse<T> of(HttpStatus httpStatus, T data) {
        return of(httpStatus, httpStatus.name(), data);
    }

    public static <T> ApiResponse<T> ok() {
        return of(OK, null);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return of(OK, message, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return of(OK, null, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return of(CREATED, null, data);
    }

    public static <T> ApiResponse<T> created() {
        return of(CREATED, null, null);
    }
}
