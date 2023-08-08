package com.plana.infli.domain;

import static com.plana.infli.domain.Role.*;
import static com.plana.infli.domain.Role.ADMIN;
import static com.plana.infli.domain.embeddable.MemberProfileImage.*;
import static com.plana.infli.domain.embeddable.MemberStatus.*;
import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import com.plana.infli.domain.editor.member.MemberEditor;
import com.plana.infli.domain.embeddable.MemberProfileImage;
import com.plana.infli.domain.embeddable.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE member SET is_deleted = true WHERE member_id=?")
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  private String name;

  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "university_id")
  private University university;

  @Embedded
  private MemberProfileImage profileImage;

  @Embedded
  private MemberStatus memberStatus;

  public Member(String email, String password, String name, String nickname,
          University university, PasswordEncoder passwordEncoder) {
    this(email, password, name, nickname, UNCERTIFIED, university, passwordEncoder);
  }

  @Builder
  public Member(String email, String password, String name, String nickname, Role role,
      University university, PasswordEncoder passwordEncoder) {
    this.email = email;
    this.password = passwordEncoder.encode(password);
    this.name = name;
    this.nickname = nickname;
    this.role = role;
    this.university = university;
    this.memberStatus = defaultMemberStatus();
    this.profileImage = defaultProfileImage();
  }

  public Member(String email, String password, String companyName,
      PasswordEncoder passwordEncoder) {
    this.email = email;
    this.nickname = companyName;
    this.password = passwordEncoder.encode(password);
    this.role = UNCERTIFIED;
  }

  public static Boolean isAdmin(Member member) {
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
            .nickname(nickname)
            .password(password)
            .status(memberStatus)
            .profileImage(profileImage);
  }

  public void edit(MemberEditor memberEditor) {
    this.password = memberEditor.getPassword();
    this.nickname = memberEditor.getNickname();
    this.memberStatus = memberEditor.getStatus();
    this.profileImage = memberEditor.getProfileImage();
  }
}

