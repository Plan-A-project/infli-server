package com.plana.infli.domain.editor;

import static com.plana.infli.domain.embedded.member.MemberStatus.*;
import static com.plana.infli.domain.type.MemberRole.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.type.MemberRole;
import com.plana.infli.domain.embedded.member.MemberName;
import com.plana.infli.domain.embedded.member.MemberProfileImage;
import com.plana.infli.domain.embedded.member.MemberStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberEditor {

    private final String nickname;

    private final String universityEmail;

    private final String password;

    private final MemberRole role;

    private final MemberStatus status;

    private final MemberName name;

    private final MemberProfileImage profileImage;

    @Builder
    public MemberEditor(String nickname, String universityEmail,
            String password, MemberRole role, MemberStatus status,
            MemberName name, MemberProfileImage profileImage) {

        this.nickname = nickname;
        this.universityEmail = universityEmail;
        this.password = password;
        this.role = role;
        this.status = status;
        this.name = name;
        this.profileImage = profileImage;
    }

    public static void editNickname(Member member, String newNickname) {
        member.edit(member.toEditor()
                .nickname(newNickname)
                .build());
    }

    public static void editPassword(Member member, String newEncryptedPassword) {
        member.edit(member.toEditor()
                .nickname(newEncryptedPassword)
                .build());
    }

    public static void editProfileImage(Member member, MemberProfileImage newProfileImage) {
        member.edit(member.toEditor()
                .profileImage(newProfileImage)
                .build());
    }

    public static void unregister(Member member) {
        boolean policyAccepted = member.getStatus().isPolicyAccepted();

        member.edit(member.toEditor()
                .status(ofDeleted(policyAccepted))
                .build());
    }

    public static void acceptPolicy(Member member) {
        member.edit(member.toEditor()
                .status(ofPolicyAccepted())
                .build());
    }

    public static void authenticateAsStudent(Member member, String universityEmail) {
        member.edit(member.toEditor()
                .universityEmail(universityEmail)
                .role(STUDENT)
                .build());
    }

    public static void authenticateAsCompany(Member member) {
        member.edit(member.toEditor()
                .role(COMPANY)
                .build());
    }
}
