package com.plana.infli.web.dto.response.profile;

import static com.plana.infli.domain.type.Role.*;

import com.plana.infli.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MyProfileToUnregisterResponse {

    private final String username;

    private final String realName;

    private final String companyName;

    @Builder
    private MyProfileToUnregisterResponse(String username, String realName, String companyName) {
        this.username = username;
        this.realName = realName;
        this.companyName = companyName;
    }

    public static MyProfileToUnregisterResponse of(Member member) {

        return MyProfileToUnregisterResponse.builder()
                .username(member.getLoginCredentials().getUsername())
                .realName(loadRealName(member))
                .companyName(loadCompanyName(member))
                .build();
    }

    private static String loadCompanyName(Member member) {
        return member.getRole() == COMPANY ?
                member.getCompanyCredentials().getCompany().getName() : null;
    }

    private static String loadRealName(Member member) {
        return member.getRole() == STUDENT ?
                member.getStudentCredentials().getRealName() : null;
    }
}
