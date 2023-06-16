package com.plana.infli.exception.custom;

import lombok.Getter;

@Getter
public abstract class DefaultException extends RuntimeException {

    public DefaultException(String message) {
        super(message);
    }

    public abstract int getStatusCode();
}
