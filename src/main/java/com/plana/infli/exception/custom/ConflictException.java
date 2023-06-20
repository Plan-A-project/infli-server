package com.plana.infli.exception.custom;

public class ConflictException extends DefaultException {

	public static final String DUPLICATED_BOARDNAME = "이미 존재하는 게시판 이름입니다.";
	public static final String DUPLICATED_EMAIL = "이미 존재하는 이메일입니다.";
	public static final String DUPLICATED_NICKNAME = "이미 존재하는 닉네임입니다.";

	public ConflictException(String message) {
		super(message);
	}

	@Override
	public int getStatusCode() {
		return 409;
	}
}