package com.plana.infli.config.initializer;

import static com.plana.infli.domain.Role.*;
import static com.plana.infli.domain.University.*;
import static com.plana.infli.domain.embedded.member.MemberName.*;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Role;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.MemberName;
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
@Profile({"dev"})
@Order(1)
@Transactional
@Getter
public class MemberInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final UniversityRepository universityRepository;

    private University university;

    @Override
    public void run(String... args)   {

        university = universityRepository.findByName("푸단대학교")
                .orElseGet(() -> universityRepository.save(create("푸단대학교")));

        createMemberWithRole(STUDENT);
        createMemberWithRole(ADMIN);
        createMemberWithRole(STUDENT_COUNCIL);
        createMemberWithRole(UNCERTIFIED_STUDENT);
        createCompanyMemberWithRole(COMPANY);
        createCompanyMemberWithRole(UNCERTIFIED_COMPANY);

    }

    private void createMemberWithRole(Role role) {
        memberRepository.save(Member.builder()
                .email(role.name().toLowerCase() + "@infli.com")
                .encodedPassword(passwordEncoder.encode("password"))
                .name(of("인플리 " + role.name(), "인플리 " + role.name()))
                .role(role)
                .university(university)
                .build());
    }

    private void createCompanyMemberWithRole(Role role) {
        memberRepository.save(Member.builder()
                .email(role.name().toLowerCase() + "@infli.com")
                .encodedPassword(passwordEncoder.encode("password"))
                .name(of("인플리 " + role.name(), "인플리 " + role.name()))
                .company(Company.create("카카오" + role.name()))
                .role(role)
                .university(university)
                .build());
    }
}
