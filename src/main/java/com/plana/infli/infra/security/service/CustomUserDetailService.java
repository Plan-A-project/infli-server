package com.plana.infli.infra.security.service;

import com.plana.infli.domain.Member;
import com.plana.infli.repository.member.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Member member = memberRepository.findActiveMemberBy(username)
                .orElseThrow(() -> new UsernameNotFoundException("아이디 또는 비밀번호를 잘못 입력했습니다."));

        return new CustomUser(member);
    }
}
