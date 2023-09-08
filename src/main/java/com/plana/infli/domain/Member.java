package com.plana.infli.domain;

import static com.plana.infli.domain.type.Role.ADMIN;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.PROTECTED;

import com.plana.infli.domain.editor.MemberEditor;
import com.plana.infli.domain.embedded.member.CompanyCredentials;
import com.plana.infli.domain.embedded.member.LoginCredentials;
import com.plana.infli.domain.embedded.member.ProfileImage;
import com.plana.infli.domain.embedded.member.BasicCredentials;
import com.plana.infli.domain.embedded.member.StudentCredentials;
import com.plana.infli.domain.type.Role;
import com.plana.infli.domain.type.VerificationStatus;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE member SET is_deleted = true WHERE member_id=?")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Enumerated(STRING)
    private Role role;

    @Enumerated(STRING)
    private VerificationStatus verificationStatus;

    @Embedded
    private LoginCredentials loginCredentials;

    @Embedded
    private ProfileImage profileImage;

    @Embedded
    private BasicCredentials basicCredentials;

    @Embedded
    @Nullable
    private CompanyCredentials companyCredentials;

    @Embedded
    @Nullable
    private StudentCredentials studentCredentials;

    @Builder
    public Member(University university, Role role, VerificationStatus verificationStatus,
            LoginCredentials loginCredentials, ProfileImage profileImage,
            BasicCredentials basicCredentials, @Nullable CompanyCredentials companyCredentials,
            @Nullable StudentCredentials studentCredentials) {

        this.university = university;
        this.role = role;
        this.verificationStatus = verificationStatus;
        this.loginCredentials = loginCredentials;
        this.profileImage = profileImage;
        this.basicCredentials = basicCredentials;
        this.companyCredentials = companyCredentials;
        this.studentCredentials = studentCredentials;
    }

    public MemberEditor.MemberEditorBuilder toEditor() {
        return MemberEditor.builder()
                .verificationStatus(verificationStatus)
                .loginCredentials(loginCredentials)
                .profileImage(profileImage)
                .basicCredentials(basicCredentials)
                .companyCredentials(companyCredentials)
                .studentCredentials(studentCredentials);
    }

    public void edit(MemberEditor memberEditor) {
        this.verificationStatus = memberEditor.getVerificationStatus();
        this.loginCredentials = memberEditor.getLoginCredentials();
        this.profileImage = memberEditor.getProfileImage();
        this.basicCredentials = memberEditor.getBasicCredentials();
        this.companyCredentials = memberEditor.getCompanyCredentials();
        this.studentCredentials = memberEditor.getStudentCredentials();
    }

    public static boolean isAdmin(Member member) {
        return member.role == ADMIN;
    }
}

