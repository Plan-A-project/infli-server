package com.plana.infli.factory;

import static com.plana.infli.domain.Role.*;

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
                .role(STUDENT)
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
                .role(UNCERTIFIED)
                .passwordEncoder(new BCryptPasswordEncoder())
                .build();

        return memberRepository.save(member);
    }

    public Member createCompanyMember(String nickname, University university) {
        Member member = Member.builder()
                .nickname(nickname)
                .name(nickname)
                .email(nickname + "@gmail.com")
                .password("1234")
                .university(university)
                .role(COMPANY)
                .passwordEncoder(new BCryptPasswordEncoder())
                .build();

        return memberRepository.save(member);
    }

    public Member createStudentCouncilMember(String nickname, University university) {
        Member member = Member.builder()
                .nickname(nickname)
                .name(nickname)
                .email(nickname + "@gmail.com")
                .password("1234")
                .university(university)
                .role(STUDENT_COUNCIL)
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
                .role(ADMIN)
                .passwordEncoder(new BCryptPasswordEncoder())
                .build();

        return memberRepository.save(member);
    }

    public Member createMember(String nickname, University university, Role role) {
        Member member = Member.builder()
                .nickname(nickname)
                .name(nickname)
                .email(nickname + "@gmail.com")
                .password("1234")
                .university(university)
                .role(role)
                .passwordEncoder(new BCryptPasswordEncoder())
                .build();

        return memberRepository.save(member);
    }
}
