package com.plana.infli.exception.custom;

public class BadRequestException extends DefaultException{

    public static final String CHILD_COMMENTS_NOT_ALLOWED = "대댓글에는 자식댓글을 작성할수 없습니다";

    public static final String INVALID_REQUIRED_PARAM = "필수 파라미터가 누락되었습니다.";

    public static final String AT_LEAST_ONE_BOARD_IS_REQUIRED = "보고싶은 게시판을 하나 이상 선택해 주세요";

    public static final String PARENT_COMMENT_IS_NOT_FOUND = "존재하지 않는 댓글에 대댓글을 작성할수 없습니다";

    public static final String PARENT_COMMENT_IS_DELETED = "삭제된 댓글에 대댓글을 작성할수 없습니다";

    public static final String MAX_COMMENT_SIZE_EXCEEDED = "최대 댓글 길이를 초과하였습니다";

    public static final String DIDNT_PRESSED_LIKE_ON_THIS_COMMENT = "해당 댓글에 좋아요를 누르지 않았습니다";

    public static final String NOT_ANONYMOUS_POST = "해당 글은 익명 글이 아닙니다";






    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
