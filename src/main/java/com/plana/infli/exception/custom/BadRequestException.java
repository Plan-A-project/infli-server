package com.plana.infli.exception.custom;

public class BadRequestException extends DefaultException{

    public static final String CHILD_COMMENTS_NOT_ALLOWED = "대댓글에는 자식댓글을 작성할수 없습니다";

    public static final String INVALID_REQUIRED_PARAM = "필수 파라미터가 누락되었습니다.";

    public static final String AT_LEAST_ONE_BOARD_IS_REQUIRED = "보고싶은 게시판을 하나 이상 선택해 주세요";

    public static final String PARENT_COMMENT_IS_NOT_FOUND = "존재하지 않는 댓글에 대댓글을 작성할수 없습니다";

    public static final String PARENT_COMMENT_IS_DELETED = "삭제된 댓글에 대댓글을 작성할수 없습니다";

    public static final String MAX_COMMENT_SIZE_EXCEEDED = "최대 댓글 길이를 초과하였습니다";

    public static final String COMMENT_LIKE_NOT_FOUND = "해당 댓글에 좋아요를 누르지 않았습니다";

    public static final String POST_LIKE_NOT_FOUND = "해당 댓글에 좋아요를 누르지 않았습니다";

    public static final String NOT_ANONYMOUS_POST = "해당 글은 익명 글이 아닙니다";

    public static final String NOT_ALL_POPULARBOARD_WAS_CHOSEN = "모든 보고싶은 게시판이 선택되지 않았습니다";

    public static final String PASSWORD_NOT_MATCH = "비밀번호가 일치하지 않습니다.";

    public static final String IMAGE_NOT_PROVIDED = "업로드할 사진이 선택되지 않았습니다 ";

    public static final String BOARD_TYPE_IS_NOT_RECRUITMENT = "채용글을 작성할수 있는 게시판이 아닙니다";

    public static final String POST_TYPE_NOT_ALLOWED = "해당 글 종류는 허용되지 않습니다";

    public static final String INVALID_BOARD_TYPE = "게시판 정보가 옳바르지 않습니다";

    public static final String INVALID_RECRUITMENT_DATE = "모집 종료일이 시작일보다 빠를수 없습니다";

    public static final String NOT_MATCHES_NEW_PASSWORD_CONFIRM = "새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.";

    public static final String INVALID_MEMBER_INFO = "회원정보가 일치하지 않습니다";

    public static final String IMAGE_IS_EMPTY = "업로드할 파일이 비어있습니다";


    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}