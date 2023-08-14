package com.plana.infli.web.dto.request.member.signup.student;

import static com.plana.infli.domain.embedded.member.LoginCredentials.*;
import static com.plana.infli.domain.embedded.member.ProfileImage.*;
import static com.plana.infli.domain.embedded.member.StudentCredentials.*;
import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.domain.type.VerificationStatus.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.BasicCredentials;
import com.plana.infli.domain.embedded.member.LoginCredentials;
import com.plana.infli.domain.embedded.member.ProfileImage;
import com.plana.infli.domain.embedded.member.StudentCredentials;
import com.plana.infli.domain.type.VerificationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateStudentMemberServiceRequest {

    private final String username;

    private final String realName;

    private final String password;

    private final String passwordConfirm;

    private final String nickname;

    private final Long universityId;

    @Builder
    private CreateStudentMemberServiceRequest(String username, String realName, String password,
            String passwordConfirm, String nickname, Long universityId) {
        this.username = username;
        this.realName = realName;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.nickname = nickname;
        this.universityId = universityId;
    }

    public Member toEntity(University university, String encodedPassword) {
        return Member.builder()
                .university(university)
                .role(STUDENT)
                .verificationStatus(NOT_STARTED)
                .loginCredentials(LoginCredentials.of(username, encodedPassword))
                .profileImage(ProfileImage.ofDefaultProfileImage())
                .basicCredentials(BasicCredentials.ofDefaultWithNickname(nickname))
                .companyCredentials(null)
                .studentCredentials(StudentCredentials.ofDefault(realName))
                .build();
    }
}
