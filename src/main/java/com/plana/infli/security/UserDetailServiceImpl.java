package com.plana.infli.security;

import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.exception.custom.NotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.plana.infli.domain.Member;
import com.plana.infli.repository.member.MemberRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserDetailServiceImpl implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Member member = memberRepository.findActiveMemberBy(username)
				.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

		return User.builder()
			.username(member.getUsername())
			.password(member.getPassword())
			.roles(member.getRole().name())
			.build();
	}
}
