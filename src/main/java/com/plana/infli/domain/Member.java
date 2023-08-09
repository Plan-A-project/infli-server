package com.plana.infli.domain;

import static com.plana.infli.domain.Role.*;
import static com.plana.infli.domain.Role.ADMIN;
import static com.plana.infli.domain.embedded.member.MemberProfileImage.*;
import static com.plana.infli.domain.embedded.member.MemberStatus.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.PROTECTED;

import com.plana.infli.domain.editor.MemberEditor;
import com.plana.infli.domain.embedded.member.MemberName;
import com.plana.infli.domain.embedded.member.MemberProfileImage;
import com.plana.infli.domain.embedded.member.MemberStatus;
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

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Nullable
    @Embedded
    private MemberName name;

    @Embedded
    private MemberStatus status;

    @Nullable
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Enumerated(STRING)
    private Role role;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "university_id")
    private University university;

    @Embedded
    private MemberProfileImage profileImage;

    @Builder
    public Member(String email, @Nullable MemberName name, String encodedPassword,
            @Nullable Company company, Role role, University university,
            MemberProfileImage profileImage, MemberStatus status) {

        this.email = email;
        this.name = name;
        this.password = encodedPassword;
        this.company = company;
        this.university = university;
        this.role = role;
        this.status = resolveStatusOrDefault(status);
        this.profileImage = resolveImageOrDefault(profileImage);
    }


    private MemberProfileImage resolveImageOrDefault(MemberProfileImage profileImage) {
        return profileImage != null ? profileImage : defaultProfileImage();
    }

    private MemberStatus resolveStatusOrDefault(MemberStatus memberStatus) {
        return memberStatus != null ? memberStatus : defaultStatus();
    }

    public static boolean isAdmin(Member member) {
        return member.role == ADMIN;
    }

    public void authenticateStudent() {
        role = STUDENT;
    }

    public void authenticateCompany() {
        role = COMPANY;
    }

    public MemberEditor.MemberEditorBuilder toEditor() {
        return MemberEditor.builder()
                .name(name)
                .password(password)
                .status(status)
                .profileImage(profileImage);
    }

    public void edit(MemberEditor memberEditor) {
        this.password = memberEditor.getPassword();
        this.name = memberEditor.getName();
        this.status = memberEditor.getStatus();
        this.profileImage = memberEditor.getProfileImage();
    }
}

