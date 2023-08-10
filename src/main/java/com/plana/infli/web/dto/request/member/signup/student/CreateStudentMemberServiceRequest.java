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
                .username(username)
                .encodedPassword(encodedPassword)
                .name(of(realName, nickname))
                .role(UNCERTIFIED_STUDENT)
                .university(university)
                .profileImage(defaultProfileImage())
                .status(defaultStatus())
                .build();
    }
}
