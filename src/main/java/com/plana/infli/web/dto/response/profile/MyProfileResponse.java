package com.plana.infli.web.dto.response.profile;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.type.MemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MyProfileResponse {

    private final String nickname;

    private final MemberRole memberRole;

    private final String email;

    @Builder
    private MyProfileResponse(String nickname, MemberRole memberRole, String email) {
        this.nickname = nickname;
        this.memberRole = memberRole;
        this.email = email;
    }

    //TODO 기업 회원인 경우
    // null 해결 해야됨
    public static MyProfileResponse of(Member member) {
        return MyProfileResponse.builder()
                .nickname(null)
                .memberRole(member.getRole())
                .email(member.getUsername())
                .build();
    }
}
