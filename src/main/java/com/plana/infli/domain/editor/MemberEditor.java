package com.plana.infli.domain.editor;

import static com.plana.infli.domain.embedded.member.MemberStatus.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Role;
import com.plana.infli.domain.embedded.member.MemberName;
import com.plana.infli.domain.embedded.member.MemberProfileImage;
import com.plana.infli.domain.embedded.member.MemberStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberEditor {

    private final String nickname;

    private final String password;

    private final Role role;

    private final MemberStatus status;

    private final MemberName name;

    private final MemberProfileImage profileImage;

    @Builder
    private MemberEditor(String nickname, String password, Role role,
            MemberStatus status, MemberName name, MemberProfileImage profileImage) {

        this.nickname = nickname;
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
}
