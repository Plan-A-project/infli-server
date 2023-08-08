package com.plana.infli.web.dto.response.profile;

import com.plana.infli.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MyProfileToUnregisterResponse {

    private final String email;

    private final String name;

    @Builder
    public MyProfileToUnregisterResponse(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public static MyProfileToUnregisterResponse of(Member member) {
        return MyProfileToUnregisterResponse.builder()
                .email(member.getEmail())
                .name(member.getName())
                .build();
    }
}
