package com.plana.infli.infra.exception.custom;

public class AuthorizationFailedException extends DefaultException {

    public static final String MESSAGE = "해당 권한이 없습니다";


    public AuthorizationFailedException() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 403;
    }

}
