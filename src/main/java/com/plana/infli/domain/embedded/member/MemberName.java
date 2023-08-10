package com.plana.infli.domain.embedded.member;

import static lombok.AccessLevel.*;

import jakarta.annotation.Nullable;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Embeddable
public class MemberName {

    // 회원 실명 Ex) 유재석
    private String realName;

    private String nickname;

    @Builder
    private MemberName(String realName, String nickname) {
        this.realName = realName;
        this.nickname = nickname;
    }

    public static MemberName of(String realName, String nickname) {
        return MemberName.builder()
                .realName(realName)
                .nickname(nickname)
                .build();
    }

}
