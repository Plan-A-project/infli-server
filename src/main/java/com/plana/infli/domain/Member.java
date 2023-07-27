package com.plana.infli.domain;

import static com.plana.infli.domain.Role.ADMIN;
import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import com.plana.infli.exception.custom.AuthenticationFailedException;
import jakarta.persistence.Column;
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

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "university_id")
  private University university;

  private String profileImageUrl;

  private boolean isDeleted = false;

  private boolean isAuthenticated = false;

  private boolean agreedOnPostPolicy = false;

  public Member(String email, String password, String name, String nickname,
      University university,
      PasswordEncoder passwordEncoder) {
    this(email, password, name, nickname, Role.UNCERTIFIED, university, passwordEncoder);
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
  }

  public Member(String email, String password, String companyName,
      PasswordEncoder passwordEncoder) {
    this.email = email;
    this.nickname = companyName;
    this.password = passwordEncoder.encode(password);
    this.role = Role.UNCERTIFIED;
  }

  public static Boolean isAdmin(Member member) {
    return member.role.equals(ADMIN);
  }

  public void authenticateStudent() {
    role = Role.STUDENT;
  }

  public void authenticate() {
    isAuthenticated = true;
  }

  public void authenticateCompany() {
    role = Role.COMPANY;
  }

  public void agreedOnPostWritePolicy() {
    this.agreedOnPostPolicy = true;
  }

  public void changeNickname(String nickname) {
    this.nickname = nickname;
  }

  public void changePassword(String password) {
    this.password = password;
  }

  public void changeProfileImage(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  public void deleteMember() {
    this.isDeleted = true;
  }
}

