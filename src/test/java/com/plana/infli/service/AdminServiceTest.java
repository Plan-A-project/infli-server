package com.plana.infli.service;

import static org.assertj.core.api.Assertions.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.response.admin.verification.company.LoadCompanyVerificationsResponse;
import com.plana.infli.web.dto.response.admin.verification.student.LoadStudentVerificationsResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private MemberFactory memberFactory;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }

    @DisplayName("관리자가 학생 인증을 신청한 회원 조회 - 성공")
    @Test
    void loadStudentVerificationRequest() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member admin = memberFactory.createAdminMember(university);

        Member member1 = memberFactory.createVerificationRequestedStudentMember("member", university);
        Member member2 = memberFactory.createVerificationRequestedStudentMember("qwert", university);

        //when
        LoadStudentVerificationsResponse response = adminService.loadCertificateUploadedStudentMembers(
                admin.getLoginCredentials().getUsername());

        //then
        assertThat(response.getStudentVerifications()).size().isEqualTo(2);
        assertThat(response.getStudentVerifications()).extracting("memberId", "imageUrl",
                        "realName", "universityName")
                .contains(
                        tuple(member1.getId(),
                                member1.getStudentCredentials().getUniversityCertificateUrl(),
                                member1.getStudentCredentials().getRealName(),
                                university.getName()),

                        tuple(member2.getId(),
                                member2.getStudentCredentials().getUniversityCertificateUrl(),
                                member2.getStudentCredentials().getRealName(),
                                university.getName())
                );

    }

    @DisplayName("관리자가 학생 인증을 신청한 회원 조회 - 신청한 회원이 없는 경우")
    @Test
    void loadStudentVerificationRequestWhenNoOneUploadedCertificate() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member admin = memberFactory.createAdminMember(university);

        Member member = memberFactory.createVerificationRequestedStudentMember("member", university);

        memberRepository.delete(member);

        //when
        LoadStudentVerificationsResponse response = adminService.loadCertificateUploadedStudentMembers(
                admin.getLoginCredentials().getUsername());

        //then
        assertThat(response.getStudentVerifications()).isEmpty();
    }

    @DisplayName("관리자가 학생 인증을 신청한 회원 조회 - 신청을 위해 증명서를 업로드 했지만, 이미 탈퇴한 회원은 조회에 포함되지 않는다")
    @Test
    void loadStudentVerificationRequest_DeletedMemberIsNotIncluded() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member admin = memberFactory.createAdminMember(university);

        //when
        LoadStudentVerificationsResponse response = adminService.loadCertificateUploadedStudentMembers(
                admin.getLoginCredentials().getUsername());

        //then
        assertThat(response.getStudentVerifications()).isEmpty();
    }


    @DisplayName("관리자가 기업 인증을 신청한 회원 조회 성공")
    @Test
    void loadCompanyVerificationRequest() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member admin = memberFactory.createAdminMember(university);

        Member member1 = memberFactory.createVerificationRequestedCompanyMember("member", university);
        Member member2 = memberFactory.createVerificationRequestedCompanyMember("qwert", university);

        //when
        LoadCompanyVerificationsResponse response = adminService
                .loadCertificateUploadedCompanyMembers(admin.getLoginCredentials().getUsername());

        //then
        assertThat(response.getCompanyVerifications()).size().isEqualTo(2);
        assertThat(response.getCompanyVerifications()).extracting("memberId", "imageUrl",
                        "companyName")
                .contains(
                        tuple(member1.getId(),
                                member1.getCompanyCredentials().getCompanyCertificateUrl(),
                                member1.getCompanyCredentials().getCompany().getName()),

                        tuple(member2.getId(),
                                member2.getCompanyCredentials().getCompanyCertificateUrl(),
                                member2.getCompanyCredentials().getCompany().getName())
                );

    }

    @DisplayName("관리자가 기업 인증을 신청한 회원 조회 - 신청한 회원이 없는 경우")
    @Test
    void loadCompanyVerificationRequestUploadNotExist() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member admin = memberFactory.createAdminMember(university);

        //when
        LoadCompanyVerificationsResponse response = adminService
                .loadCertificateUploadedCompanyMembers(admin.getLoginCredentials().getUsername());

        //then
        assertThat(response.getCompanyVerifications()).isEmpty();
    }

    @DisplayName("관리자가 기업 인증을 신청한 회원 조회 - 신청을 위해 사업자 증명서를 업로드 했지만 탈퇴한 회원은 조회되지 않는다")
    @Test
    void loadCompanyVerificationRequest_DeletedMemberIsExcluded() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member admin = memberFactory.createAdminMember(university);

        Member member = memberFactory.createVerificationRequestedCompanyMember("member", university);

        memberRepository.delete(member);

        //when
        LoadCompanyVerificationsResponse response = adminService
                .loadCertificateUploadedCompanyMembers(admin.getLoginCredentials().getUsername());

        //then
        assertThat(response.getCompanyVerifications()).isEmpty();
    }




}
