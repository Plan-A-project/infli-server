package com.plana.infli.domain.type;

import static com.plana.infli.domain.type.MemberRole.*;

import jakarta.annotation.Nullable;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BoardType {

    EMPLOYMENT("채용", 1),

    ACTIVITY("대외활동", 2),

    CLUB("동아리", 3),

    ANONYMOUS("익명", 4),

    CAMPUS_LIFE("학교생활", 5),

    ;

    // 게시판 한글 이름
    private final String boardName;

    // 게시판 기본 정렬 순서
    private final int defaultSequence;


    @RequiredArgsConstructor
    @Getter
    public enum SubBoardType {

        EMPLOYMENT_NORMAL("채용-일반", List.of(STUDENT, COMPANY, ADMIN)),

        EMPLOYMENT_RECRUITMENT("채용-모집", List.of(COMPANY, ADMIN)),

        ACTIVITY_NORMAL("대외활동-일반", List.of(STUDENT, COMPANY, ADMIN)),

        ACTIVITY_RECRUITMENT("대외활동-모집", List.of(STUDENT, COMPANY, ADMIN)),

        CLUB_NORMAL("동아리-일반", List.of(STUDENT, ADMIN)),

        ANONYMOUS_NORMAL("익명-일반", List.of(STUDENT, ADMIN)),

        CAMPUS_LIFE_NORMAL("학교생활-일반", List.of(STUDENT, STUDENT_COUNCIL, ADMIN)),

        CAMPUS_LIFE_ANNOUNCEMENT("학교생활-공지", List.of(STUDENT_COUNCIL, ADMIN)),

        ;
        private final String boardName;

        private final List<MemberRole> allowedMemberRoles;

        @Nullable
        public static SubBoardType of(BoardType boardType, PostType postType) {
            try {
                return valueOf(boardType.toString() + "_" + postType.toString());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public boolean hasWritePermission(MemberRole memberRole) {
            return allowedMemberRoles.contains(memberRole);
        }
    }


}
