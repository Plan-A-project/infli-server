package com.plana.infli.web.dto.response.error;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ErrorResponse {

	private final Integer code;

	private final String message;

    private final Map<String, String> validation;

	@Builder
	private ErrorResponse(Integer code, String message, Map<String, String> validation) {
		this.code = code;
		this.message = message;
		this.validation = validation != null ? validation : new HashMap<>();
	}

    public void addValidation(String field, String defaultMessage) {
        validation.put(field, defaultMessage);
    }
}
