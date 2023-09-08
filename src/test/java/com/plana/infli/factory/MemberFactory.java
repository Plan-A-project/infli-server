package com.plana.infli.factory;

import static com.plana.infli.domain.editor.MemberEditor.*;
import static com.plana.infli.domain.embedded.member.BasicCredentials.ofDefaultWithNickname;
import static com.plana.infli.domain.embedded.member.ProfileImage.ofDefaultProfileImage;
import static com.plana.infli.domain.embedded.member.StudentCredentials.*;
import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.domain.type.VerificationStatus.*;
import static java.util.UUID.*;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.embedded.member.BasicCredentials;
import com.plana.infli.domain.embedded.member.CompanyCredentials;
import com.plana.infli.domain.embedded.member.LoginCredentials;
import com.plana.infli.domain.embedded.member.ProfileImage;
import com.plana.infli.domain.embedded.member.StudentCredentials;
import com.plana.infli.domain.type.Role;
import com.plana.infli.domain.University;
import com.plana.infli.domain.type.VerificationStatus;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MemberFactory {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder encoder;

    private Member of(String nickname, University university, Role role, Company company,
            VerificationStatus verificationStatus, boolean hasAcceptedPolicy) {

        Member member = Member.builder()
                .university(university)
                .role(role)
                .verificationStatus(verificationStatus)
                .loginCredentials(LoginCredentials.of(randomUUID().toString().substring(0, 10),
                        encoder.encode("password")))
                .profileImage(ofDefaultProfileImage())
                .basicCredentials(ofDefaultWithNickname(nickname))
                .companyCredentials(generateCompanyCredentials(company))
                .studentCredentials(generateStudentCredentials(role))
                .build();

        if (hasAcceptedPolicy) {
            acceptPolicy(member);
        }
        return member;
    }

    private CompanyCredentials generateCompanyCredentials(Company company) {
        if (company != null) {
            return CompanyCredentials.ofWithCertificate(CompanyCredentials.ofDefault(company),
                    "aaa.com");
        }
        return null;
    }

    private StudentCredentials generateStudentCredentials(Role role) {
        if (role == STUDENT) {
            return ofWithCertificate(ofDefault("이영진"), "aaa.com");
        }
        return null;
    }

    public Member createVerifiedStudentMember(String nickname, University university) {
        Member member = of(nickname, university, STUDENT, null, SUCCESS, true);
        return memberRepository.save(member);
    }


    public Member createUnverifiedStudentMember(String nickname, University university) {
        Member member = of(nickname, university, STUDENT, null, NOT_STARTED, true);
        return memberRepository.save(member);
    }

    public Member createPendingStatusStudent(String nickname,
            String universityEmail, University university) {

        Member member = of(nickname, university, STUDENT, null, PENDING, true);
        return memberRepository.save(member);
    }


    public Member createVerifiedCompanyMember(University university) {

        Company company = createCompany(COMPANY);

        Member member = of(randomUUID().toString().substring(0, 10), university, COMPANY,
                company, SUCCESS, true);

        return memberRepository.save(member);
    }

    public Member createUnverifiedCompanyMember(University university) {

        Company company = createCompany(COMPANY);

        Member member = of(randomUUID().toString().substring(0, 10), university, COMPANY,
                company, NOT_STARTED, true);

        return memberRepository.save(member);
    }

    public Member createAdminMember(University university) {

        Member member = of("관리자", university, ADMIN, null, SUCCESS, true);

        return memberRepository.save(member);
    }


    public Member createPolicyAcceptedMemberWithRole(University university, Role role) {

        Company company = createCompany(role);

        String nickname = randomUUID().toString().substring(0, 10);

        Member member = of(nickname, university, role, company, SUCCESS, true);

        return memberRepository.save(member);
    }

    public Member createPolicyNotAcceptedMemberWithRole(University university, Role role) {

        Company company = createCompany(role);

        String nickname = randomUUID().toString().substring(0, 10);

        Member member = of(nickname, university, role, company, SUCCESS, false);

        return memberRepository.save(member);
    }

    public Member createStudentCouncilMember(University university) {

        Company company = createCompany(STUDENT_COUNCIL);

        String nickname = randomUUID().toString().substring(0, 10);

        Member member = of(nickname, university, STUDENT_COUNCIL, company, SUCCESS, true);

        return memberRepository.save(member);
    }


    private Company createCompany(Role role) {

        if (role == COMPANY) {
            Company company = Company.create(randomUUID().toString().substring(0, 10));

            return companyRepository.save(company);
        }
        return null;
    }


    public Member createVerificationRequestedStudentMember(
            String nickname, University university) {

        return memberRepository.save(Member.builder()
                .university(university)
                .role(STUDENT)
                .verificationStatus(PENDING)
                .loginCredentials(LoginCredentials.of(
                        randomUUID().toString().substring(0, 10), "password1234!"))
                .profileImage(ProfileImage.ofDefaultProfileImage())
                .basicCredentials(BasicCredentials.ofDefaultWithNickname(nickname))
                .companyCredentials(null)
                .studentCredentials(StudentCredentials.ofWithCertificate(
                        ofDefault(randomUUID().toString().substring(0, 4)),
                        randomUUID().toString()))
                .build());
    }

    public Member createVerificationRequestedCompanyMember(
            String nickname, University university) {

        Company company = companyRepository.save(
                Company.create(randomUUID().toString().substring(0, 5)));

        return memberRepository.save(Member.builder()
                .university(university)
                .role(COMPANY)
                .verificationStatus(PENDING)
                .loginCredentials(LoginCredentials.of(
                        randomUUID().toString().substring(0, 10), "password1234!"))
                .profileImage(ProfileImage.ofDefaultProfileImage())
                .basicCredentials(BasicCredentials.ofDefaultWithNickname(nickname))
                .companyCredentials(CompanyCredentials.ofWithCertificate(
                        CompanyCredentials.ofDefault(company), randomUUID().toString()))
                .studentCredentials(null)
                .build());
    }

}
