package com.plana.infli.domain;

import static com.plana.infli.domain.Role.*;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BoardType {

    EMPLOYMENT("채용", 1, List.of(COMPANY)),

    ACTIVITY("대외활동", 2, List.of(COMPANY)),

    CLUB("동아리", 3, List.of(STUDENT_COUNCIL)),

    ANONYMOUS("익명", 4, List.of(COMPANY, STUDENT_COUNCIL, STUDENT)),

    CAMPUS_LIFE("학교생활", 5, List.of(COMPANY, STUDENT_COUNCIL, STUDENT)),

    ;

    // 게시판 한글 이름
    private final String boardName;

    // 게시판 기본 정렬순서
    private final int defaultSequence;

    // 각 게시판 종류마다 글 작성이 허용되는 회원 Role
    private final List<Role> roles;
}
