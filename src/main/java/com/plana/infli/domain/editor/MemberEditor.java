package com.plana.infli.domain.editor;

import static com.plana.infli.domain.embedded.member.LoginCredentials.*;
import static com.plana.infli.domain.embedded.member.BasicCredentials.*;
import static com.plana.infli.domain.type.VerificationStatus.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.embedded.member.BasicCredentials;
import com.plana.infli.domain.embedded.member.CompanyCredentials;
import com.plana.infli.domain.embedded.member.LoginCredentials;
import com.plana.infli.domain.embedded.member.StudentCredentials;
import com.plana.infli.domain.embedded.member.ProfileImage;
import com.plana.infli.domain.type.VerificationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberEditor {

    private final VerificationStatus verificationStatus;

    private final LoginCredentials loginCredentials;

    private final ProfileImage profileImage;

    private final BasicCredentials basicCredentials;

    private final CompanyCredentials companyCredentials;

    private final StudentCredentials studentCredentials;

    @Builder
    private MemberEditor(VerificationStatus verificationStatus, LoginCredentials loginCredentials,
            ProfileImage profileImage, BasicCredentials basicCredentials,
            CompanyCredentials companyCredentials, StudentCredentials studentCredentials) {

        this.verificationStatus = verificationStatus;
        this.loginCredentials = loginCredentials;
        this.profileImage = profileImage;
        this.basicCredentials = basicCredentials;
        this.companyCredentials = companyCredentials;
        this.studentCredentials = studentCredentials;
    }

    public static void editNickname(Member member, String newNickname) {

        BasicCredentials newBasicCredentials = ofNewNickname(member.getBasicCredentials(),
                newNickname);

        member.edit(member.toEditor()
                .basicCredentials(newBasicCredentials)
                .build());
    }

    public static void editPassword(Member member, String newEncryptedPassword) {

        LoginCredentials newLoginCredentials = ofNewPassword(member.getLoginCredentials(),
                newEncryptedPassword);

        member.edit(member.toEditor()
                .loginCredentials(newLoginCredentials)
                .build());
    }

    public static void editProfileImage(Member member, ProfileImage newProfileImage) {
        member.edit(member.toEditor()
                .profileImage(newProfileImage)
                .build());
    }

    public static void unregister(Member member) {

        BasicCredentials newBasicCredentials = ofDeleted(member.getBasicCredentials());

        member.edit(member.toEditor()
                .basicCredentials(newBasicCredentials)
                .build());
    }

    public static void acceptPolicy(Member member) {

        BasicCredentials newBasicCredentials = ofPolicyAccepted(member.getBasicCredentials());

        member.edit(member.toEditor()
                .basicCredentials(newBasicCredentials)
                .build());
    }

    public static void setVerificationStatusAsPendingByUniversityEmail(Member member, String universityEmail) {

        StudentCredentials newStudentCredentials = StudentCredentials.ofWithEmail(
                member.getStudentCredentials(), universityEmail);

        member.edit(member.toEditor()
                .verificationStatus(PENDING)
                .studentCredentials(newStudentCredentials)
                .build());
    }

    public static void setVerificationStatusAsPendingByCompanyCertificate(Member member,
            String imageUrl) {

        CompanyCredentials newCompanyCredentials = CompanyCredentials.ofWithEmail(
                member.getStudentCredentials(), imageUrl);

        member.edit(member.toEditor()
                .verificationStatus(PENDING)
                .studentCredentials(newStudentCredentials)
                .build());
    }

//    public static void authenticateAsCompany(Member member) {
//        member.edit(member.toEditor()
//                .role(VERIFIED_COMPANY)
//                .build());
//    }
}
