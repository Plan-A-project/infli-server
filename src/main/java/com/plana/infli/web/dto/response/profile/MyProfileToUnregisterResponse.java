package com.plana.infli.web.dto.response.profile;

import com.plana.infli.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MyProfileToUnregisterResponse {

    private final String username;

    private final String name;

    @Builder
    public MyProfileToUnregisterResponse(String username, String realName) {
        this.username = username;
        this.name = realName;
    }

    //TODO 회원 탈퇴시 학생 회원과 기업 회원 구분해야됨
    public static MyProfileToUnregisterResponse of(Member member) {
        return MyProfileToUnregisterResponse.builder()
                .username(member.getLoginCredentials().getUsername())
                .realName(member.getStudentCredentials().getRealName())
                .build();
    }

}
