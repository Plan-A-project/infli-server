package com.plana.infli.exception.custom;

public class BadRequestException extends DefaultException{


    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
