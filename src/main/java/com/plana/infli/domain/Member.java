package com.plana.infli.domain;

import static com.plana.infli.domain.Role.*;
import static jakarta.persistence.FetchType.*;
import static lombok.AccessLevel.*;

import com.plana.infli.exception.custom.AuthenticationFailedException;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import org.hibernate.annotations.SQLDelete;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

	@Column(nullable = false)
	private String name;

	@Column(unique = true, nullable = false)
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
	}

	public void authenticate() {
		isAuthenticated = true;
	}

	public void authenticateStudent() {
		role = STUDENT;
	}

	public void changeNickname(String nickname) {
		this.nickname = nickname;
	}

	public void changePassword(String password) {
		this.password = password;
	}

	public void changeProfileImage(String profileImageUrl){
		this.profileImageUrl = profileImageUrl;
	}

	public void deleteMember() {
		this.isDeleted = true;
	}

	public static Boolean isAdmin(Member member) {
		return member.role.equals(ADMIN);
	}

	public static void checkIsLoggedIn(String email) {
		if (email == null) {
			throw new AuthenticationFailedException();
		}
	}

	public void agreedOnPostWritePolicy() {
		this.agreedOnPostPolicy = true;
	}
}

