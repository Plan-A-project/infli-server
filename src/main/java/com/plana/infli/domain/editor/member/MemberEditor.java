package com.plana.infli.domain.editor.member;

import static com.plana.infli.domain.embeddable.MemberStatus.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.embeddable.MemberProfileImage;
import com.plana.infli.domain.embeddable.MemberStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberEditor {

    private final String nickname;

    private final String password;

    private final MemberStatus status;

    private final MemberProfileImage profileImage;


    @Builder
    private MemberEditor(String nickname, String password,
            MemberStatus status, MemberProfileImage profileImage) {
        this.nickname = nickname;
        this.password = password;
        this.status = status;
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
        MemberStatus currentStatus = member.getMemberStatus();

        member.edit(member.toEditor()
                .status(create(
                        true,
                        currentStatus.isAuthenticated(),
                        currentStatus.isPolicyAccepted()))
                .build());
    }

    public static void acceptPolicy(Member member) {
        MemberStatus currentStatus = member.getMemberStatus();

        member.edit(member.toEditor()
                .status(create(
                        currentStatus.isDeleted(),
                        currentStatus.isAuthenticated(),
                        true))
                .build());
    }

    public static void authenticate(Member member) {
        MemberStatus currentStatus = member.getMemberStatus();

        member.edit(member.toEditor()
                .status(create(
                        currentStatus.isDeleted(),
                        true,
                        currentStatus.isPolicyAccepted()))
                .build());
    }
}
