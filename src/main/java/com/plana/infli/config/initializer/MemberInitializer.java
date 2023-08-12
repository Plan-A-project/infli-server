package com.plana.infli.config.initializer;

import static com.plana.infli.domain.type.MemberRole.*;
import static com.plana.infli.domain.University.*;
import static com.plana.infli.domain.embedded.member.MemberName.*;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.type.MemberRole;
import com.plana.infli.domain.University;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Profile({"dev", "local"})
@Order(1)
@Transactional
@Getter
public class MemberInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final UniversityRepository universityRepository;

    private final CompanyRepository companyRepository;

    private University university;

    @Override
    public void run(String... args)   {

        university = universityRepository.findByName("푸단대학교")
                .orElseGet(() -> universityRepository.save(create("푸단대학교")));

        createMemberWithRole(STUDENT);
        createMemberWithRole(ADMIN);
        createMemberWithRole(STUDENT_COUNCIL);
        createMemberWithRole(EMAIL_UNCERTIFIED_STUDENT);
        createCompanyMemberWithRole(COMPANY);
        createCompanyMemberWithRole(EMAIL_UNCERTIFIED_COMPANY);

    }

    private void createMemberWithRole(MemberRole memberRole) {
        memberRepository.save(Member.builder()
                .username(memberRole.name().toLowerCase())
                .encodedPassword(passwordEncoder.encode("password"))
                .name(of("인플리 " + memberRole.name(), "인플리 " + memberRole.name()))
                .role(memberRole)
                .university(university)
                .build());
    }

    private void createCompanyMemberWithRole(MemberRole memberRole) {
        Company company = Company.create("카카오" + memberRole.name());
        companyRepository.save(company);

        memberRepository.save(Member.builder()
                .username(memberRole.name().toLowerCase())
                .encodedPassword(passwordEncoder.encode("password"))
                .name(of("인플리 " + memberRole.name(), "인플리 " + memberRole.name()))
                .company(company)
                .role(memberRole)
                .university(university)
                .build());

    }
}
