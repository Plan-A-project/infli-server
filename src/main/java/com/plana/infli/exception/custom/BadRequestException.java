package com.plana.infli.exception.custom;

public class BadRequestException extends DefaultException{

    public static final String Child_Comments_NOT_ALLOWED = "대댓글에는 자식댓글을 작성할수 없습니다";

    public static final String INVALID_REQUIRED_PARAM = "필수 파라미터가 누락되었습니다.";


    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}