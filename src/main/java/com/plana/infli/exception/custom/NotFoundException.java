package com.plana.infli.exception.custom;

public class NotFoundException extends DefaultException{

    public static final String UNIVERSITY_NOT_FOUND = "대학교가 존재하지 않습니다";

    public static final String MEMBER_NOT_FOUND = "사용자를 찾을수 없습니다";

    public static final String BOARD_NOT_FOUND = "게시판을 찾을수 없습니다";

    public static final String POPULAR_BOARD_NOT_FOUND = "인기 게시판을 찾을수 없습니다";

    public static final String POST_NOT_FOUND = "게시글이 존재하지 않거나 삭제되었습니다";

    public static final String COMMENT_NOT_FOUND = "댓글이 존재하지 않거나 삭제되었습니다";

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 404;
    }

}