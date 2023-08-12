package com.plana.infli.web.dto.request.member.signup.company;

import static com.plana.infli.domain.type.MemberRole.*;
import static com.plana.infli.domain.embedded.member.MemberProfileImage.*;
import static com.plana.infli.domain.embedded.member.MemberStatus.*;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateCompanyMemberServiceRequest {

    private final String username;

    private final String password;

    private final String passwordConfirm;

    private final Long universityId;

    private final String companyName;

    @Builder
    private CreateCompanyMemberServiceRequest(String username, String password,
            String passwordConfirm, Long universityId, String companyName) {

        this.username = username;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.universityId = universityId;
        this.companyName = companyName;
    }

    public Member toEntity(Company company, String encodedPassword, University university) {
        return Member.builder()
                .username(username)
                .encodedPassword(encodedPassword)
                .name(null)
                .status(defaultStatus())
                .company(company)
                .role(EMAIL_UNCERTIFIED_COMPANY)
                .university(university)
                .profileImage(defaultProfileImage())
                .build();
    }
}
