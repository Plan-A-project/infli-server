package com.plana.infli.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class EmailAuthentication extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "email_authentication_id")
	private Long id;

	private String email;

	@Column(nullable = false)
	private String secret;

	@Column(nullable = false)
	private LocalDateTime expirationTime;

	private String certificateUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	public static EmailAuthentication createEmailAuthentication(Member member) {
		EmailAuthentication emailAuthentication = new EmailAuthentication();
		emailAuthentication.secret = UUID.randomUUID().toString();
		emailAuthentication.expirationTime = LocalDateTime.now().plusMinutes(30);
		emailAuthentication.member = member;

		return emailAuthentication;
	}

	public static EmailAuthentication createStudentAuthentication(Member member, String studentEmail) {
		EmailAuthentication emailAuthentication = new EmailAuthentication();
		emailAuthentication.secret = UUID.randomUUID().toString();
		emailAuthentication.expirationTime = LocalDateTime.now().plusMinutes(30);
		emailAuthentication.member = member;
		emailAuthentication.email = studentEmail;

		return emailAuthentication;
	}
}

