package com.plana.infli.web.dto.request.member.signup.student;

import static com.plana.infli.domain.Role.*;
import static com.plana.infli.domain.embedded.member.MemberName.*;
import static com.plana.infli.domain.embedded.member.MemberProfileImage.defaultProfileImage;
import static com.plana.infli.domain.embedded.member.MemberStatus.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateStudentMemberServiceRequest {

    private final String email;

    private final String name;

    private final String password;

    private final String passwordConfirm;

    private final String nickname;

    private final Long universityId;

    @Builder
    private CreateStudentMemberServiceRequest(String email, String name, String password,
            String passwordConfirm, String nickname, Long universityId) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.nickname = nickname;
        this.universityId = universityId;
    }

    public Member toEntity(University university, String encodedPassword) {
        return Member.builder()
                .email(email)
                .encodedPassword(encodedPassword)
                .name(of(name, nickname))
                .role(UNCERTIFIED_STUDENT)
                .university(university)
                .profileImage(defaultProfileImage())
                .status(defaultStatus())
                .build();
    }
}
