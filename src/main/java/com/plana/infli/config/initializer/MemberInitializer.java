package com.plana.infli.config.initializer;

import static com.plana.infli.domain.editor.MemberEditor.*;
import static com.plana.infli.domain.embedded.member.BasicCredentials.*;
import static com.plana.infli.domain.embedded.member.ProfileImage.*;
import static com.plana.infli.domain.embedded.member.StudentCredentials.*;
import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.domain.University.*;
import static com.plana.infli.domain.type.VerificationStatus.*;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.embedded.member.CompanyCredentials;
import com.plana.infli.domain.embedded.member.LoginCredentials;
import com.plana.infli.domain.embedded.member.StudentCredentials;
import com.plana.infli.domain.type.Role;
import com.plana.infli.domain.University;
import com.plana.infli.domain.type.VerificationStatus;
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
@Profile({"dev"})
@Order(1)
@Transactional
@Getter
public class MemberInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;

    private final PasswordEncoder encoder;

    private final UniversityRepository universityRepository;

    private final CompanyRepository companyRepository;

    private University university;

    @Override
    public void run(String... args)   {

        university = universityRepository.findByName("푸단대학교")
                .orElseGet(() -> universityRepository.save(create("푸단대학교")));

        createVerifiedStudentMember("student1");
        createVerifiedStudentMember("student2");
        createUnVerifiedStudentMember();

        createMemberWithRole(ADMIN);
        createMemberWithRole(STUDENT_COUNCIL);

        createVerifiedCompanyMember();
        createUnVerifiedCompanyMember();
    }

    private void createVerifiedStudentMember(String username) {
        Member member = memberRepository.save(Member.builder()
                .university(university)
                .role(STUDENT)
                .verificationStatus(SUCCESS)
                .loginCredentials(LoginCredentials.of(username, encoder.encode("password")))
                .profileImage(ofDefaultProfileImage())
                .basicCredentials(ofDefaultWithNickname(username))
                .companyCredentials(null)
                .studentCredentials(ofWithEmail(ofDefault("이영진"), "aaa@infli.com"))
                .build());

        acceptPolicy(member);
    }

    private void createUnVerifiedStudentMember() {
        Member member = memberRepository.save(Member.builder()
                .university(university)
                .role(STUDENT)
                .verificationStatus(NOT_STARTED)
                .loginCredentials(
                        LoginCredentials.of("unverifiedStudent", encoder.encode("password")))
                .profileImage(ofDefaultProfileImage())
                .basicCredentials(ofDefaultWithNickname("unverifiedStudent"))
                .companyCredentials(null)
                .studentCredentials(ofDefault("이영진"))
                .build());

        acceptPolicy(member);
    }

    private void createMemberWithRole(Role role) {
        Member member = memberRepository.save(Member.builder()
                .university(university)
                .role(role)
                .verificationStatus(SUCCESS)
                .loginCredentials(
                        LoginCredentials.of(role.name().toLowerCase(), encoder.encode("password")))
                .profileImage(ofDefaultProfileImage())
                .basicCredentials(ofDefaultWithNickname(role.name().toLowerCase()))
                .companyCredentials(null)
                .studentCredentials(null)
                .build());

        acceptPolicy(member);
    }

    private void createVerifiedCompanyMember() {
        Company company = Company.create("카카오");
        companyRepository.save(company);

        Member member = memberRepository.save(Member.builder()
                .university(university)
                .role(COMPANY)
                .verificationStatus(SUCCESS)
                .loginCredentials(LoginCredentials.of("company", encoder.encode("password")))
                .profileImage(ofDefaultProfileImage())
                .basicCredentials(ofDefaultWithNickname("company"))
                .companyCredentials(
                        CompanyCredentials.ofWithCertificate(CompanyCredentials.ofDefault(company),
                                "aaa.com"))
                .studentCredentials(null)
                .build());

        acceptPolicy(member);
    }

    private void createUnVerifiedCompanyMember() {
        Company company = Company.create("삼성전자");
        companyRepository.save(company);

        Member member = memberRepository.save(Member.builder()
                .university(university)
                .role(COMPANY)
                .verificationStatus(NOT_STARTED)
                .loginCredentials(
                        LoginCredentials.of("unverifiedCompany", encoder.encode("password")))
                .profileImage(ofDefaultProfileImage())
                .basicCredentials(ofDefaultWithNickname("unverifiedCompany"))
                .companyCredentials(CompanyCredentials.ofDefault(company))
                .studentCredentials(null)
                .build());

        acceptPolicy(member);
    }
}
