package com.plana.infli.annotation;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.MemberName;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class MockMemberFactory implements WithSecurityContextFactory<WithMockMember> {

    private final MemberRepository memberRepository;

    private final UniversityRepository universityRepository;

    private final PasswordEncoder passwordEncoder;


    @Override
    public SecurityContext createSecurityContext(WithMockMember withMockMember) {

        University university = universityRepository.save(University.builder()
                .name("푸단대학교")
                .build());

        Member member = Member.builder()
                .email(withMockMember.email())
                .encodedPassword("Test1234!")
                .name(MemberName.of(withMockMember.nickname(), withMockMember.nickname()))
                .role(withMockMember.role())
                .university(university)
                .build();

        Member savedMember = memberRepository.save(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedMember.getEmail(), null,
                List.of(new SimpleGrantedAuthority(savedMember.getRole().toString())));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        return context;
    }
}
