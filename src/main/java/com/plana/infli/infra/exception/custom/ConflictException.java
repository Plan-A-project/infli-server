package com.plana.infli.infra.exception.custom;

public class ConflictException extends DefaultException {

	public static final String DUPLICATED_USERNAME = "이미 사용중인 ID 입니다";

	public static final String DUPLICATED_NICKNAME = "이미 존재하는 닉네임입니다.";

	public static final String DUPLICATED_UNIVERSITY_EMAIL = "이미 사용중인 대학교 이메일 입니다";

	public static final String ALREADY_PRESSED_LIKE_ON_THIS_COMMENT = "이미 해당 댓글에 좋아요를 눌렀습니다";

	public static final String ALREADY_PRESSED_LIKE_ON_THIS_POST = "이미 해당 글에 좋아요를 눌렀습니다";

	public static final String SCRAP_ALREADY_EXISTS = "이미 해당글을 스크랩 했습니다";


    public static final String DEFAULT_POPULAR_BOARD_EXISTS = "인기 게시판 기본값이 이미 생성되었습니다";


    public ConflictException(String message) {
        super(message);
    }

	@Override
	public int getStatusCode() {
		return 409;
	}
}
