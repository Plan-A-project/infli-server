package com.plana.infli.factory;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Role;
import com.plana.infli.domain.University;
import com.plana.infli.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MemberFactory {

    @Autowired
    private MemberRepository memberRepository;

    public Member createStudentMember(String nickname, University university) {
        Member member = Member.builder()
                .nickname(nickname)
                .name(nickname)
                .email(nickname + "@gmail.com")
                .password("1234")
                .university(university)
                .role(Role.STUDENT)
                .passwordEncoder(new BCryptPasswordEncoder())
                .build();

        return memberRepository.save(member);
    }

    public Member createUncertifiedMember(String nickname, University university) {
        Member member = Member.builder()
                .nickname(nickname)
                .name(nickname)
                .email(nickname + "@gmail.com")
                .password("1234")
                .university(university)
                .role(Role.UNCERTIFIED)
                .passwordEncoder(new BCryptPasswordEncoder())
                .build();

        return memberRepository.save(member);
    }

    public Member createAdminMember(String nickname, University university) {
        Member member = Member.builder()
                .nickname(nickname)
                .name(nickname)
                .email(nickname + "@gmail.com")
                .password("1234")
                .university(university)
                .role(Role.ADMIN)
                .passwordEncoder(new BCryptPasswordEncoder())
                .build();

        return memberRepository.save(member);
    }
}
