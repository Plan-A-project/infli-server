package com.plana.infli.controller;

import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.domain.type.VerificationStatus.*;
import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.annotation.MockMvcTest;
import com.plana.infli.annotation.WithMockMember;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.type.VerificationStatus;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@MockMvcTest
class AdminControllerTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper om;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CompanyRepository companyRepository;

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

    @DisplayName("인증을 받기위해 대학 증명서를 업로드한 학생 회원 목록 조회")
    @WithMockMember(role = ADMIN)
    @Test
    void loadCertificateUploadedStudentMembers() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = memberFactory.createVerificationRequestedStudentMember(
                "member", university);

        //when
        ResultActions resultActions = mvc.perform(get("/admin/certificate/members/student")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentVerifications.size()").value(1))
                .andExpect(jsonPath("$.studentVerifications.[0].memberId").value(member.getId()))
                .andExpect(jsonPath("$.studentVerifications.[0].imageUrl")
                        .value(member.getStudentCredentials().getUniversityCertificateUrl()))
                .andExpect(jsonPath("$.studentVerifications.[0].realName")
                        .value(member.getStudentCredentials().getRealName()))
                .andExpect(jsonPath("$.studentVerifications.[0].universityName")
                        .value(university.getName()))
                .andDo(print());
    }

    @DisplayName("인증을 받기위해 대학 증명서를 업로드한 학생 회원 목록 조회 실패 - 로그인 하지 않은 상태로 조회할수 없다")
    @Test
    void loadCertificateUploadedStudentMembersWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerificationRequestedStudentMember(
                "member", university);

        //when
        ResultActions resultActions = mvc.perform(get("/admin/certificate/members/student")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("인증을 받기위해 대학 증명서를 업로드한 학생 회원 목록 조회 실패 - 관리자 회원만 조회 가능하다")
    @WithMockMember(role = STUDENT)
    @Test
    void loadCertificateUploadedStudentMembersByNotAdminMember() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = memberFactory.createVerificationRequestedStudentMember(
                "member", university);

        //when
        ResultActions resultActions = mvc.perform(get("/admin/certificate/members/student")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(content().string("해당 권한이 없습니다"))
                .andDo(print());
    }

    @DisplayName("인증을 받기 위해 사업자 등록증을 업로드한 기업 회원 조회")
    @WithMockMember(role = ADMIN)
    @Test
    void loadCertificateUploadedCompanyMember() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = memberFactory.createVerificationRequestedCompanyMember(
                "member", university);

        //when
        ResultActions resultActions = mvc.perform(get("/admin/certificate/members/company")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyVerifications.size()").value(1))
                .andExpect(jsonPath("$.companyVerifications.[0].memberId").value(member.getId()))
                .andExpect(jsonPath("$.companyVerifications.[0].imageUrl")
                        .value(member.getCompanyCredentials().getCompanyCertificateUrl()))

                .andExpect(jsonPath("$.companyVerifications.[0].companyName")
                        .value(member.getCompanyCredentials().getCompany().getName()))
                .andDo(print());
    }

    @DisplayName("인증을 받기 위해 사업자 등록증을 업로드한 기업 회원 조회 실패 - 로그인 하지 않은 상태로 조회할수 없다")
    @Test
    void loadCertificateUploadedCompanyMemberWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerificationRequestedCompanyMember(
                "member", university);

        //when
        ResultActions resultActions = mvc.perform(get("/admin/certificate/members/company")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("인증을 받기 위해 사업자 등록증을 업로드한 기업 회원 조회 실패 - 관리자 회원만 조회할수 있다")
    @WithMockMember(role = STUDENT)
    @Test
    void loadCertificateUploadedCompanyMemberByNotAdminMember() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = memberFactory.createVerificationRequestedCompanyMember(
                "member", university);

        //when
        ResultActions resultActions = mvc.perform(get("/admin/certificate/members/company")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(content().string("해당 권한이 없습니다"))
                .andDo(print());
    }

    @DisplayName("학생 회원 인증 요청을 관리자가 승인")
    @WithMockMember(role = ADMIN)
    @Test
    void setStudentVerificationStatusAsSuccess() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = memberFactory.createVerificationRequestedCompanyMember(
                "member", university);

        //when
        ResultActions resultActions = mvc.perform(
                post("/admin/certificate/members/{memberId}", member.getId())
                        .with(csrf()));

        //then
        resultActions
                .andExpect(status().isOk())
                .andDo(print());

        Member findMember = memberRepository.findActiveMemberBy(member.getId()).get();
        assertThat(findMember.getVerificationStatus()).isEqualTo(SUCCESS);
    }

    @DisplayName("학생 회원 인증 요청을 관리자가 승인 실패 - 로그인 하지 않은 상태로 승인을 할수 없다")
    @Test
    void setStudentVerificationStatusAsSuccessWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerificationRequestedCompanyMember(
                "member", university);

        //when
        ResultActions resultActions = mvc.perform(
                post("/admin/certificate/members/{memberId}", member.getId())
                        .with(csrf()));

        //then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("학생 회원 인증 요청을 관리자가 승인 실패 - 관리자 회원만 승인을 할수 있다")
    @WithMockMember(role = STUDENT)
    @Test
    void setStudentVerificationStatusAsSuccessByNotAdminMember() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = memberFactory.createVerificationRequestedCompanyMember(
                "member", university);

        //when
        ResultActions resultActions = mvc.perform(
                post("/admin/certificate/members/{memberId}", member.getId())
                        .with(csrf()));

        //then
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(content().string("해당 권한이 없습니다"))
                .andDo(print());
    }

    @DisplayName("기업 회원 인증 요청을 관리자가 승인")
    @WithMockMember(role = ADMIN)
    @Test
    void setCompanyVerificationStatusAsSuccess() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = memberFactory.createVerificationRequestedCompanyMember(
                "member", university);

        //when
        ResultActions resultActions = mvc.perform(
                post("/admin/certificate/members/{memberId}", member.getId())
                        .with(csrf()));

        //then
        resultActions
                .andExpect(status().isOk())
                .andDo(print());

        Member findMember = memberRepository.findActiveMemberBy(member.getId()).get();
        assertThat(findMember.getVerificationStatus()).isEqualTo(SUCCESS);
    }

    @DisplayName("기업 회원 인증 요청을 관리자가 승인 실패 - 로그인을 하지 않은 상태로 승인할수 없다")
    @Test
    void setCompanyVerificationStatusAsSuccessWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerificationRequestedCompanyMember(
                "member", university);

        //when
        ResultActions resultActions = mvc.perform(
                post("/admin/certificate/members/{memberId}", member.getId())
                        .with(csrf()));

        //then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("기업 회원 인증 요청을 관리자가 승인 실패 - 관리자 회원만 승인을 할수 있다")
    @WithMockMember(role = STUDENT)
    @Test
    void setCompanyVerificationStatusAsSuccessByNotAdminMember() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = memberFactory.createVerificationRequestedCompanyMember(
                "member", university);

        //when
        ResultActions resultActions = mvc.perform(
                post("/admin/certificate/members/{memberId}", member.getId())
                        .with(csrf()));

        //then
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(content().string("해당 권한이 없습니다"))
                .andDo(print());
    }
}


