package com.plana.infli.controller;

import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.annotation.MockMvcTest;
import com.plana.infli.annotation.WithMockMember;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.emailVerification.EmailVerificationRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.request.member.email.SendVerificationMailRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@MockMvcTest
public class MemberControllerTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper om;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private MemberService memberService;


    @AfterEach
    void tearDown() {
        emailVerificationRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }


    @DisplayName("글 작성 규정 동의했는지 여부 확인 - 동의 하지 않은 경우")
    @WithMockMember(policyAccepted = false)
    @Test
    void checkMemberAgreedOnWritePolicy_False() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(get("/policy")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().string("false"))
                .andDo(print());
    }


    @DisplayName("글 작성 규정 동의했는지 여부 확인 - 동의한 경우")
    @WithMockMember(policyAccepted = true)
    @Test
    void checkMemberAgreedOnWritePolicy_True() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(get("/policy")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(print());
    }

    @DisplayName("글 작성 규정 동의했는지 여부 확인 실패 - 로그인을 하지 않은 경우")
    @Test
    void checkAgreedOnWritePolicyWithoutLogin() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(get("/policy")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("글 작성 규정 동의함 요청")
    @WithMockMember(policyAccepted = false)
    @Test
    void agreedOnWritePolicy() throws Exception {
        //given
        Member member = findContextMember();

        //when
        ResultActions resultActions = mvc.perform(post("/policy")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isOk()).andDo(print());

        Member findMember = memberRepository.findActiveMemberBy(member.getId()).get();
        assertThat(findMember.getBasicCredentials().isPolicyAccepted()).isTrue();
    }


    @DisplayName("글 작성 규정 동의함 요청 실패 - 로그인을 하지 않은 경우")
    @Test
    void agreedOnWritePolicyWithoutLogin() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/policy")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }


    @DisplayName("대학교 이메일로 인증 메일 발송 실패 - 로그인을 하지 않은 경우")
    @Test
    void sendVerificationEmailWithoutLogin() throws Exception {
        //given
        String json = om.writeValueAsString(SendVerificationMailRequest.builder()
                .universityEmail("1234@fudan.edu.cn")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/verification/student/email")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("대학교 이메일로 인증 메일 발송 실패 - 대학교 이메일 입력은 필수다")
    @WithMockMember
    @Test
    void sendVerificationEmailWithoutUniversityEmail() throws Exception {
        //given
        String json = om.writeValueAsString(SendVerificationMailRequest.builder()
                .universityEmail(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/verification/student/email")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.universityEmail").value("대학교 이메일을 입력해주세요"))
                .andDo(print());
    }


    @DisplayName("대학교 이메일로 인증 메일 발송 실패 - 대학교 이메일 입력은 필수다2")
    @WithMockMember
    @Test
    void sendVerificationEmailWithoutUniversityEmail2() throws Exception {
        //given
        String json = om.writeValueAsString(SendVerificationMailRequest.builder()
                .universityEmail("")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/verification/student/email")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.universityEmail").value("대학교 이메일을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("학생 회원의 대학교 이메일로 인증 메일 발송 실패 - 이메일 형식이 아닌 경우")
    @WithMockMember
    @Test
    void sendVerificationEmailWithInvalidEmailType() throws Exception {
        //given
        String json = om.writeValueAsString(SendVerificationMailRequest.builder()
                .universityEmail("aaa")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/verification/student/email")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.universityEmail").value("이메일 형식으로 입력해주세요"))
                .andDo(print());
    }



    Member findContextMember() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findActiveMemberBy(username).get();
    }
}
