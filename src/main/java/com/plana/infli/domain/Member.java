package com.plana.infli.domain;

import static com.plana.infli.domain.type.MemberRole.*;
import static com.plana.infli.domain.type.MemberRole.ADMIN;
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
import com.plana.infli.domain.type.MemberRole;
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
    private String username;

    @Column(nullable = false)
    private String password;

    private String universityEmail;

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
    private MemberRole role;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "university_id")
    private University university;

    @Embedded
    private MemberProfileImage profileImage;

    @Builder
    public Member(String username, @Nullable MemberName name, String encodedPassword,
            @Nullable Company company, MemberRole role, University university,
            MemberProfileImage profileImage, MemberStatus status) {

        this.username = username;
        this.name = name;
        this.password = encodedPassword;
        this.universityEmail = null;
        this.company = company;
        this.university = university;
        this.role = role;
        this.status = status != null ? status : defaultStatus();
        this.profileImage = profileImage != null ? profileImage : defaultProfileImage();
    }

    public static boolean isAdmin(Member member) {
        return member.role == ADMIN;
    }

    public void authenticateStudent() {
        role = STUDENT;
    }


    public MemberEditor.MemberEditorBuilder toEditor() {
        return MemberEditor.builder()
                .name(name)
                .password(password)
                .universityEmail(universityEmail)
                .role(role)
                .status(status)
                .profileImage(profileImage);
    }

    public void edit(MemberEditor memberEditor) {
        this.password = memberEditor.getPassword();
        this.name = memberEditor.getName();
        this.role = memberEditor.getRole();
        this.universityEmail = memberEditor.getUniversityEmail();
        this.status = memberEditor.getStatus();
        this.profileImage = memberEditor.getProfileImage();
    }
}

