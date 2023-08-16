package com.plana.infli.web.dto.response.profile;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.type.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MyProfileResponse {

    private final String nickname;

    private final String username;

    private final Role role;

    @Builder
    private MyProfileResponse(String nickname, String username, Role role) {
        this.nickname = nickname;
        this.username = username;
        this.role = role;
    }

    public static MyProfileResponse of(Member member) {
        return MyProfileResponse.builder()
                .nickname(member.getBasicCredentials().getNickname())
                .username(member.getLoginCredentials().getUsername())
                .role(member.getRole())
                .build();
    }
}
