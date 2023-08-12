package com.plana.infli.factory;

import static com.plana.infli.domain.type.MemberRole.*;
import static com.plana.infli.domain.embedded.member.MemberProfileImage.*;
import static com.plana.infli.domain.embedded.member.MemberStatus.*;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.type.MemberRole;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.MemberName;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import jakarta.annotation.Nullable;
import java.util.Random;
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
        Member member = of(nickname, university, EMAIL_UNCERTIFIED_STUDENT, null, true);

        return memberRepository.save(member);
    }

    public Member createUncertifiedCompanyMember(String companyName, University university) {

        Company company = createCompany(companyName);

        Member member = of(null, university, EMAIL_UNCERTIFIED_COMPANY, company, true);

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

    public Member parameterizedTest_PolicyAccepted(University university, MemberRole memberRole) {

        Company company = (memberRole == COMPANY || memberRole == EMAIL_UNCERTIFIED_COMPANY) ?
                companyRepository.save(Company.create("카카오")) : null;

        String nickname = (memberRole == COMPANY || memberRole == EMAIL_UNCERTIFIED_COMPANY) ? null : "nickname";

        Member member = of(nickname, university, memberRole, company, true);

        return memberRepository.save(member);
    }

    public Member parameterizedTest_PolicyNotAccepted(University university, MemberRole memberRole) {

        Company company = (memberRole == COMPANY || memberRole == EMAIL_UNCERTIFIED_COMPANY) ?
                companyRepository.save(Company.create("카카오")) : null;

        Member member = of("nickname", university, memberRole, company, false);

        return memberRepository.save(member);
    }

    private Member of(String nickname, University university, MemberRole memberRole, Company company, boolean hasAcceptedPolicy) {

        MemberName name = createMemberName(nickname);

        return Member.builder()
                .username("" + new Random().nextInt(1000000))
                .encodedPassword(encoder.encode("1234"))
                .name(name)
                .status(create(false, hasAcceptedPolicy))
                .company(company)
                .role(memberRole)
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
