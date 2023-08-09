package com.plana.infli.web.dto.response.profile;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MyProfileResponse {

    private final String nickname;

    private final Role role;

    private final String email;

    @Builder
    private MyProfileResponse(String nickname, Role role, String email) {
        this.nickname = nickname;
        this.role = role;
        this.email = email;
    }

    //TODO 기업 회원인 경우
    // null 해결 해야됨
    public static MyProfileResponse of(Member member) {
        return MyProfileResponse.builder()
                .nickname(null)
                .role(member.getRole())
                .email(member.getEmail())
                .build();
    }

}
