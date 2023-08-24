package com.plana.infli.infra.exception.custom;

import lombok.Getter;

@Getter
public abstract class DefaultException extends RuntimeException {

    public DefaultException(String message, Throwable cause) {
        super(message, cause);
    }

    public DefaultException(String message) {
        super(message);
    }

    public abstract int getStatusCode();
}
