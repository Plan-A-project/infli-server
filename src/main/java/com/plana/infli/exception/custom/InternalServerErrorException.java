package com.plana.infli.exception.custom;

public class InternalServerErrorException extends DefaultException{

    //TODO
    public static final String IMAGE_UPLOAD_FAILED = "사진 업로드에 실패했습니다";

    public static final String POST_VIEW_FAILED = "글 조회에 실패했습니다";



    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalServerErrorException(String message) {
        super(message);
    }



    @Override
    public int getStatusCode() {
        return 500;
    }

}
