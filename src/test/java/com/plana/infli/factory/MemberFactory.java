package com.plana.infli.factory;

import static com.plana.infli.domain.Role.*;
import static com.plana.infli.domain.embedded.member.MemberProfileImage.*;
import static com.plana.infli.domain.embedded.member.MemberStatus.*;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Role;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.MemberName;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import jakarta.annotation.Nullable;
import java.util.Random;
import java.util.UUID;
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


    public Member createStudentMember(String nickname, University university) {
        Member member = of(nickname, university, STUDENT, null, true);
        return memberRepository.save(member);
    }


    public Member createUncertifiedStudentMember(String nickname, University university) {
        Member member = of(nickname, university, UNCERTIFIED_STUDENT, null, true);

        return memberRepository.save(member);
    }

    public Member createUncertifiedCompanyMember(String companyName, University university) {

        Company company = createCompany(companyName);

        Member member = of(null, university, UNCERTIFIED_COMPANY, company, true);

        return memberRepository.save(member);
    }


    public Member createCompanyMember(String companyName, University university) {

        Company company = createCompany(companyName);

        Member member = of(null, university, COMPANY, company, true);

        return memberRepository.save(member);
    }

    public Member createStudentCouncilMember(String nickname, University university) {

        Member member = of(nickname, university, STUDENT_COUNCIL, null, true);

        return memberRepository.save(member);
    }

    public Member createAdminMember(University university) {

        Member member = of("관리자", university, ADMIN, null, true);

        return memberRepository.save(member);
    }

    public Member parameterizedTest_PolicyAccepted(University university, Role role) {

        Company company = (role == COMPANY || role == UNCERTIFIED_COMPANY) ?
                companyRepository.save(Company.create("카카오")) : null;

        String nickname = (role == COMPANY || role == UNCERTIFIED_COMPANY) ? null : "nickname";

        Member member = of(nickname, university, role, company, true);

        return memberRepository.save(member);
    }

    public Member parameterizedTest_PolicyNotAccepted(University university, Role role) {

        Company company = (role == COMPANY || role == UNCERTIFIED_COMPANY) ?
                companyRepository.save(Company.create("카카오")) : null;

        Member member = of("nickname", university, role, company, false);

        return memberRepository.save(member);
    }

    private Member of(String nickname, University university, Role role, Company company, boolean hasAcceptedPolicy) {

        MemberName name = createMemberName(nickname);

        return Member.builder()
                .username("" + new Random().nextInt(1000000))
                .encodedPassword(encoder.encode("1234"))
                .name(name)
                .status(create(false, hasAcceptedPolicy))
                .company(company)
                .role(role)
                .university(university)
                .profileImage(defaultProfileImage())
                .build();
    }

    @Nullable
    private static MemberName createMemberName(String nickname) {
        return nickname != null ? MemberName.of(nickname, nickname) : null;
    }

    private Company createCompany(String companyName) {
        return companyRepository.save(Company.create(companyName));
    }

}
