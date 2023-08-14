package com.plana.infli.web.dto.request.member.signup.company;

import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.domain.type.VerificationStatus.*;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.BasicCredentials;
import com.plana.infli.domain.embedded.member.CompanyCredentials;
import com.plana.infli.domain.embedded.member.LoginCredentials;
import com.plana.infli.domain.embedded.member.ProfileImage;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateCompanyMemberServiceRequest {

    private final String username;

    private final String nickname;

    private final String password;

    private final String passwordConfirm;

    private final Long universityId;

    private final String companyName;

    @Builder
    private CreateCompanyMemberServiceRequest(String username, String nickname, String password,
            String passwordConfirm, Long universityId, String companyName) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.universityId = universityId;
        this.companyName = companyName;
    }

    public Member toEntity(Company company, String encodedPassword, University university) {
        return Member.builder()
                .university(university)
                .role(COMPANY)
                .verificationStatus(NOT_STARTED)
                .loginCredentials(LoginCredentials.of(username, encodedPassword))
                .profileImage(ProfileImage.ofDefaultProfileImage())
                .basicCredentials(BasicCredentials.ofDefaultWithNickname(nickname))
                .companyCredentials(CompanyCredentials.ofDefault(company))
                .studentCredentials(null)
                .build();
    }

}
