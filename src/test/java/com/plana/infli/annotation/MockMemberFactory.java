package com.plana.infli.annotation;

import static com.plana.infli.domain.embedded.member.LoginCredentials.*;
import static com.plana.infli.domain.embedded.member.StudentCredentials.*;
import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.domain.type.VerificationStatus.*;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.BasicCredentials;
import com.plana.infli.domain.embedded.member.CompanyCredentials;
import com.plana.infli.domain.embedded.member.LoginCredentials;
import com.plana.infli.domain.embedded.member.ProfileImage;
import com.plana.infli.domain.embedded.member.StudentCredentials;
import com.plana.infli.domain.type.VerificationStatus;
import com.plana.infli.repository.company.CompanyRepository;
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

    private final CompanyRepository companyRepository;

    private final PasswordEncoder encoder;


    @Override
    public SecurityContext createSecurityContext(WithMockMember withMockMember) {

        University university = universityRepository.save(University.builder()
                .name("푸단대학교")
                .build());

        StudentCredentials studentCredentials = generateStudentInfo(withMockMember);
        CompanyCredentials companyCredentials = generateCompanyInfo(withMockMember);

        Member member = Member.builder()
                .university(university)
                .role(withMockMember.role())
                .verificationStatus(SUCCESS)
                .loginCredentials(
                        LoginCredentials.of(withMockMember.username(), encoder.encode("password")))
                .profileImage(ProfileImage.ofDefaultProfileImage())
                .basicCredentials(BasicCredentials.ofDefaultWithNickname(withMockMember.nickname()))
                .companyCredentials(companyCredentials)
                .studentCredentials(studentCredentials)
                .build();

        Member savedMember = memberRepository.save(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedMember.getLoginCredentials().getUsername(), null,
                List.of(new SimpleGrantedAuthority(savedMember.getRole().toString())));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }


    private CompanyCredentials generateCompanyInfo(WithMockMember withMockMember) {
        if (withMockMember.role() == COMPANY) {
            Company company = companyRepository.save(Company.create("카카오"));
            return CompanyCredentials.ofWithCertificate(CompanyCredentials.ofDefault(company), "aaa.com");
        }
        return null;
    }

    private StudentCredentials generateStudentInfo(WithMockMember withMockMember) {
        if (withMockMember.role() == STUDENT) {

            return ofWithEmail(ofDefault("이영진"), "aaa@infli.com");
        }
        return null;
    }
}
