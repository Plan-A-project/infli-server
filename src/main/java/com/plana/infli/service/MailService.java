package com.plana.infli.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plana.infli.domain.EmailAuthentication;
import com.plana.infli.domain.Member;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.email_authentication.EmailAuthenticationRepository;
import com.plana.infli.repository.member.MemberRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MailService {

	private final JavaMailSender mailSender;
	private final MemberRepository memberRepository;
	private final EmailAuthenticationRepository emailAuthenticationRepository;

	@Transactional
	public void sendMemberAuthenticationEmail(String email) {
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new NotFoundException(NotFoundException.MEMBER_NOT_FOUND));

		EmailAuthentication emailAuthentication = EmailAuthentication.createEmailAuthentication(member);

		String subject = "INFLI 회원 인증 메일";
		String secret = emailAuthentication.getSecret();
		String text = "안녕하세요. INFLI 입니다. 다음 링크를 클릭하시면 인증이 완료됩니다.\n" +
			"http://localhost:8080/member/email/auth/" + secret + "\n" +
			"30분안에 인증하셔야 합니다.";

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(member.getEmail());
		message.setSubject(subject);
		message.setText(text);

		mailSender.send(message);

		emailAuthenticationRepository.save(emailAuthentication);
	}

	@Transactional
	public void authenticateMemberEmail(String secret) {
		EmailAuthentication emailAuthentication = emailAuthenticationRepository.findAvailableMemberEmailAuthentication(
				secret)
			.orElseThrow(() -> new NotFoundException(NotFoundException.AUTHENTICATION_NOT_FOUND));

		Member member = emailAuthentication.getMember();

		member.authenticate();
	}
}
