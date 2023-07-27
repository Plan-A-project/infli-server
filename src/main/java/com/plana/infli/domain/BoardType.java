package com.plana.infli.domain;

import static com.plana.infli.domain.PostType.*;
import static com.plana.infli.domain.Role.*;
import static java.util.List.*;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BoardType {

    EMPLOYMENT("채용", 1,  of(NORMAL, RECRUITMENT)),

    ACTIVITY("대외활동", 2,  of(NORMAL, RECRUITMENT)),

    CLUB("동아리", 3,  of(NORMAL)),

    ANONYMOUS("익명", 4, of(NORMAL)),

    CAMPUS_LIFE("학교생활", 5, of(NORMAL, ANNOUNCEMENT)),

    ;

    // 게시판 한글 이름
    private final String boardName;

    // 게시판 기본 정렬순서
    private final int defaultSequence;

    private final List<PostType> allowedPostTypes;


    public boolean isAllowedPostType(PostType postType) {
        return this.allowedPostTypes.contains(postType);
    }

}
