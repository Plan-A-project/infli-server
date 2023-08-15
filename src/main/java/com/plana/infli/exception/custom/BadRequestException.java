package com.plana.infli.exception.custom;

public class BadRequestException extends DefaultException{

    public static final String CHILD_COMMENTS_NOT_ALLOWED = "대댓글에는 자식댓글을 작성할수 없습니다";

    public static final String MAX_COMMENT_SIZE_EXCEEDED = "최대 댓글 길이를 초과하였습니다";

    public static final String COMMENT_LIKE_NOT_FOUND = "해당 댓글에 좋아요를 누르지 않았습니다";

    public static final String POST_LIKE_NOT_FOUND = "해당 댓글에 좋아요를 누르지 않았습니다";

    public static final String NOT_ALL_POPULARBOARD_WAS_CHOSEN = "모든 보고싶은 게시판이 선택되지 않았습니다";

    public static final String PASSWORD_NOT_MATCH = "비밀번호가 일치하지 않습니다.";

    public static final String IMAGE_NOT_PROVIDED = "업로드할 사진이 선택되지 않았습니다 ";

    public static final String BOARD_TYPE_IS_NOT_RECRUITMENT = "채용글을 작성할수 있는 게시판이 아닙니다";

    public static final String POST_TYPE_NOT_ALLOWED = "해당 글 종류는 허용되지 않습니다";

    public static final String INVALID_BOARD_TYPE = "게시판 정보가 옳바르지 않습니다";

    public static final String INVALID_RECRUITMENT_DATE = "모집 종료일이 시작일보다 빠를수 없습니다";

    public static final String NOT_MATCHES_PASSWORD_CONFIRM = "비밀번호와 비밀번호 확인이 일치하지 않습니다.";

    public static final String NOT_MATCHES_NEW_PASSWORD_CONFIRM = "새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.";

    public static final String INVALID_MEMBER_INFO = "회원정보가 일치하지 않습니다";

    public static final String IMAGE_IS_EMPTY = "업로드할 파일이 비어있습니다";

    public static final String MAX_IMAGES_EXCEEDED = "최대 10개까지 업로드 할 수 있습니다";

    public static final String WRITING_WITHOUT_POLICY_AGREEMENT_NOT_ALLOWED = "글 작성 규칙 동의를 먼저 진행해주세요";

    public static final String INVALID_UNIVERSITY_EMAIL = "유효하지 않은 대학 이메일 주소 입니다";

    public static final String EMAIL_VERIFICATION_ALREADY_EXISTS = "이미 대학교 이메일 인증을 완료했습니다";

    public static final String COMPANY_VERIFICATION_ALREADY_EXISTS = "기업 인증을 완료했습니다";

    public static final String INVALID_EMAIL_CODE = "유효하지 않은 이메일 코드 입니다";

    public static final String INVALID_STUDENT_VERIFICATION_REQUEST = "유효하지 않은 이메일 인증 요청 입니다";

    public static final String EMAIL_VERIFICATION_CODE_EXPIRED = "이메일 인증 코드가 만료되었습니다";


    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}