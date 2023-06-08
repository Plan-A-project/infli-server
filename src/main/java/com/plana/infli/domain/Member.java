package com.plana.infli.domain;

import static lombok.AccessLevel.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

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
	private Role role;

	private String profileImageUrl;

	private boolean isEnabled = true;

	private boolean acceptPostRule = false;

	public Member(String email, String password, String name, String nickname) {
		this(email, password, name, nickname, Role.UNCERTIFIED);
	}

	public Member(String email, String password, String name, String nickname, Role role) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.nickname = nickname;
		this.role = role;
	}
}
