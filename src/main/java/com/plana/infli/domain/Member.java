package com.plana.infli.domain;

import static jakarta.persistence.FetchType.*;
import static lombok.AccessLevel.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE member SET is_enabled = false WHERE member_id=?")
@Where(clause = "is_enabled=true")
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

	private boolean isEnabled = true;

	private boolean isAuthenticated = false;

	public Member(String email, String password, String name, String nickname, University university,
		PasswordEncoder passwordEncoder) {
		this(email, password, name, nickname, Role.UNCERTIFIED, university, passwordEncoder);
	}

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
}
