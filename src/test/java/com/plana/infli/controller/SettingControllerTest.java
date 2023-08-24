package com.plana.infli.controller;


import static com.plana.infli.domain.type.Role.*;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.core.context.SecurityContextHolder.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.annotation.MockMvcTest;
import com.plana.infli.annotation.WithMockMember;
import com.plana.infli.domain.Member;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.setting.modify.password.ModifyPasswordRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@MockMvcTest
class SettingControllerTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }

    @DisplayName("내 프로필 조회 성공 - 내가 학생 회원인 경우")
    @Test
    @WithMockMember(role = STUDENT)
    void loadMyStudentProfile() throws Exception {

        //given
        Member member = findContextMember();

        //when
        ResultActions resultActions = mvc.perform(get("/setting/profile").with(csrf()));

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(member.getBasicCredentials().getNickname()))
                .andExpect(jsonPath("$.username").value(member.getLoginCredentials().getUsername()))
                .andExpect(jsonPath("$.role").value(STUDENT.name()))
                .andExpect(jsonPath("$.thumbnailUrl").value(member.getProfileImage().getThumbnailUrl()))
                .andExpect(jsonPath("$.originalUrl").value(member.getProfileImage().getOriginalUrl()));
    }


    @DisplayName("내 프로필 조회 성공 - 내가 관리자 회원인 경우")
    @Test
    @WithMockMember(role = ADMIN)
    void loadMyAdminProfile() throws Exception {

        //given
        Member member = findContextMember();

        //when
        ResultActions resultActions = mvc.perform(get("/setting/profile").with(csrf()));

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(member.getBasicCredentials().getNickname()))
                .andExpect(jsonPath("$.username").value(member.getLoginCredentials().getUsername()))
                .andExpect(jsonPath("$.role").value(ADMIN.name()))
                .andExpect(jsonPath("$.thumbnailUrl").value(member.getProfileImage().getThumbnailUrl()))
                .andExpect(jsonPath("$.originalUrl").value(member.getProfileImage().getOriginalUrl()));
    }


    @DisplayName("내 프로필 조회 성공 - 내가 학생회 회원인 경우")
    @Test
    @WithMockMember(role = STUDENT_COUNCIL)
    void loadMyStudentCouncilProfile() throws Exception {

        //given
        Member member = findContextMember();

        //when
        ResultActions resultActions = mvc.perform(get("/setting/profile").with(csrf()));


        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(member.getBasicCredentials().getNickname()))
                .andExpect(jsonPath("$.username").value(member.getLoginCredentials().getUsername()))
                .andExpect(jsonPath("$.role").value(STUDENT_COUNCIL.name()))
                .andExpect(jsonPath("$.thumbnailUrl").value(member.getProfileImage().getThumbnailUrl()))
                .andExpect(jsonPath("$.originalUrl").value(member.getProfileImage().getOriginalUrl()));
    }


    @DisplayName("내 프로필 조회 성공 - 내가 기업 회원인 경우")
    @Test
    @WithMockMember(role = COMPANY)
    void loadMyCompanyProfile() throws Exception {

        //given
        Member member = findContextMember();

        //when
        ResultActions resultActions = mvc.perform(get("/setting/profile").with(csrf()));

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(member.getBasicCredentials().getNickname()))
                .andExpect(jsonPath("$.username").value(member.getLoginCredentials().getUsername()))
                .andExpect(jsonPath("$.role").value(COMPANY.name()))
                .andExpect(jsonPath("$.thumbnailUrl").value(member.getProfileImage().getThumbnailUrl()))
                .andExpect(jsonPath("$.originalUrl").value(member.getProfileImage().getOriginalUrl()));
    }

    @DisplayName("내 프로필 조회 실패 - 로그인 하지 않은 경우")
    @Test
    void loadMyProfileWithoutLogin() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(get("/setting/profile").with(csrf()));

        //then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"));
    }


    @DisplayName("닉네임 변경시 사용 가능한 새로운 닉네임인지 검증 성공 - 사용 가능한 경우")
    @WithMockMember
    @Test
    void checkIfValidNewNickname() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(get("/setting/nickname/{nickname}", "infli1")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().string("사용 가능한 닉네임"));
    }


    @DisplayName("닉네임 변경시 사용 가능한 새로운 닉네임인지 검증 실패 - 새로운 닉네임값이 없는 경우")
    @WithMockMember
    @Test
    void newNicknameIsEmpty() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(get("/setting/nickname/{nickname}", " ")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("한글, 영어, 숫자를 포함해서 2~8자리 이내로 입력해주세요"));
    }

    @DisplayName("닉네임 변경시 사용 가능한 새로운 닉네임인지 검증 실패 - 로그인 하지 않은 경우")
    @Test
    void checkIsValidNicknameWithoutLogin() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(get("/setting/nickname/{nickname}", "infli12")
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"));
    }

    @DisplayName("닉네임 변경 성공")
    @WithMockMember
    @Test
    void changeNickname() throws Exception {
        //given
        Member member = findContextMember();

        //when
        ResultActions resultActions = mvc.perform(post("/setting/nickname")
                .content("infli12")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk()).andDo(print());
        Member findMember = memberRepository
                .findActiveMemberBy(member.getLoginCredentials().getUsername()).get();
        assertThat(findMember.getBasicCredentials().getNickname()).isEqualTo("infli12");
    }

    @DisplayName("닉네임 변경 실패 - 로그인을 하지 않은 경우")
    @Test
    void changeNicknameWithoutLogin() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/setting/nickname")
                .content("infli12")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("닉네임 변경 실패 - 새로운 닉네임을 입력하지 않은 경우")
    @WithMockMember
    @Test
    void changeNicknameWithEmptyString() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/setting/nickname")
                .content("")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 본문의 형식이 올바르지 않습니다"))
                .andDo(print());
    }

    @DisplayName("닉네임 변경 실패 - 새로운 닉네임을 입력하지 않은 경우2")
    @WithMockMember
    @Test
    void changeNicknameWithEmptyString2() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/setting/nickname"));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 본문의 형식이 올바르지 않습니다"))
                .andDo(print());
    }

    @DisplayName("현재 사용중인 비밀번호 검증 성공")
    @WithMockMember
    @Test
    void verifyCurrentPassword() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/setting/password/verification")
                .content("password"));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("현재 사용중인 비밀번호 검증 실패 - 비밀번호가 틀린 경우")
    @WithMockMember
    @Test
    void provideInvalidPassword() throws Exception {

        //when
        ResultActions resultActions = mvc.perform(post("/setting/password/verification")
                .content("1111"));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
                .andDo(print());
    }

    @DisplayName("현재 사용중인 비밀번호 검증 실패 - 로그인을 하지 않은 경우")
    @Test
    void verifyCurrentPasswordWithoutLogin() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/setting/password/verification")
                .content("password"));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("현재 사용중인 비밀번호 검증 실패 - 비밀번호를 입력하지 않은 경우")
    @WithMockMember
    @Test
    void verifyCurrentPasswordWithoutProvidingPassword() throws Exception {

        //when
        ResultActions resultActions = mvc.perform(post("/setting/password/verification")
                .content(""));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 본문의 형식이 올바르지 않습니다"))
                .andDo(print());
    }

    @DisplayName("현재 사용중인 비밀번호 검증 실패 - 비밀번호를 입력하지 않은 경우2")
    @WithMockMember
    @Test
    void verifyCurrentPasswordWithoutProvidingPassword2() throws Exception {

        //when
        ResultActions resultActions = mvc.perform(post("/setting/password/verification"));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 본문의 형식이 올바르지 않습니다"))
                .andDo(print());
    }

    @DisplayName("비밀번호 변경 성공")
    @WithMockMember
    @Test
    void changePassword() throws Exception {

        //given
        String json = om.writeValueAsString(ModifyPasswordRequest.builder()
                .currentPassword("password")
                .newPassword("newPassword1234!")
                .newPasswordConfirm("newPassword1234!")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/setting/password")
                .content(json)
                .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("비밀번호 변경 완료"))
                .andDo(print());
    }

    @DisplayName("비밀번호 변경 실패 - 로그인을 하지 않은 경우")
    @Test
    void changePasswordWithoutLogin() throws Exception {
        //given
        String json = om.writeValueAsString(ModifyPasswordRequest.builder()
                .currentPassword("password")
                .newPassword("newPassword1234!")
                .newPasswordConfirm("newPassword1234!")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/setting/password")
                .content(json)
                .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("비밀번호 변경 실패 - 기존 비밀번호는 필수다")
    @WithMockMember
    @Test
    void changePasswordWithoutCurrentPassword() throws Exception {
        //given
        String json = om.writeValueAsString(ModifyPasswordRequest.builder()
                .currentPassword(null)
                .newPassword("newPassword1234!")
                .newPasswordConfirm("newPassword1234!")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/setting/password")
                .content(json)
                .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.currentPassword").value("기존 비밀번호를 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("비밀번호 변경 실패 - 기존 비밀번호는 필수다 2")
    @WithMockMember
    @Test
    void changePasswordWithoutCurrentPassword2() throws Exception {
        //given
        String json = om.writeValueAsString(ModifyPasswordRequest.builder()
                .currentPassword("")
                .newPassword("newPassword1234!")
                .newPasswordConfirm("newPassword1234!")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/setting/password")
                .content(json)
                .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.currentPassword").value("기존 비밀번호를 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("비밀번호 변경 실패 - 새비밀번호는 필수다")
    @WithMockMember
    @Test
    void changePasswordWithoutNewPassword() throws Exception {
        //given
        String json = om.writeValueAsString(ModifyPasswordRequest.builder()
                .currentPassword("password")
                .newPassword(null)
                .newPasswordConfirm("newPassword1234!")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/setting/password")
                .content(json)
                .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.newPassword").value("새 비밀번호를 입력해주세요"))
                .andDo(print());
    }


    @DisplayName("비밀번호 변경 실패 - 새비밀번호는 필수다 2")
    @WithMockMember
    @Test
    void changePasswordWithoutNewPassword2() throws Exception {
        //given
        String json = om.writeValueAsString(ModifyPasswordRequest.builder()
                .currentPassword("password")
                .newPassword("")
                .newPasswordConfirm("newPassword1234!")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/setting/password")
                .content(json)
                .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.newPassword").value("비밀번호는 영어, 숫자, 특수문자를 포함해서 8~20자리 이내로 입력해주세요."))
                .andDo(print());
    }

    @DisplayName("비밀번호 변경 실패 - 새비밀번호 확인은 필수다")
    @WithMockMember
    @Test
    void changePasswordWithoutNewPasswordConfirm() throws Exception {
        //given
        String json = om.writeValueAsString(ModifyPasswordRequest.builder()
                .currentPassword("password")
                .newPassword("newPassword1234!")
                .newPasswordConfirm(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/setting/password")
                .content(json)
                .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.newPasswordConfirm").value("새 비밀번호 확인을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("비밀번호 변경 실패 - 새비밀번호 확인은 필수다2")
    @WithMockMember
    @Test
    void changePasswordWithoutNewPasswordConfirm2() throws Exception {
        //given
        String json = om.writeValueAsString(ModifyPasswordRequest.builder()
                .currentPassword("password")
                .newPassword("newPassword1234!")
                .newPasswordConfirm("")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/setting/password")
                .content(json)
                .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.newPasswordConfirm").value("새 비밀번호 확인을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("비밀번호 변경 실패 - 비밀번호 정규표현식 규칙에 맞지 않는 경우")
    @WithMockMember
    @Test
    void changePasswordWithInvalidNewPassword() throws Exception {
        //given
        String json = om.writeValueAsString(ModifyPasswordRequest.builder()
                .currentPassword("password")
                .newPassword("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                .newPasswordConfirm("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/setting/password")
                .content(json)
                .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.newPassword").value("비밀번호는 영어, 숫자, 특수문자를 포함해서 8~20자리 이내로 입력해주세요."))
                .andDo(print());
    }

    @DisplayName("비밀번호 변경 실패 - Request Body값이 없는 경우")
    @WithMockMember
    @Test
    void changePasswordWithoutRequestBody() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/setting/password")
                .content(""));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 본문의 형식이 올바르지 않습니다"))
                .andDo(print());
    }

    @DisplayName("비밀번호 변경 실패 - Request Body값이 없는 경우2")
    @WithMockMember
    @Test
    void changePasswordWithoutRequestBody2() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/setting/password"));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 본문의 형식이 올바르지 않습니다"))
                .andDo(print());
    }

    @DisplayName("탈퇴를 요청한 회원의 정보 조회 성공 - 학생 회원인 경우")
    @WithMockMember(role = STUDENT)
    @Test
    void loadStudentMemberProfileToUnregister() throws Exception {
        //given
        Member member = findContextMember();

        //when
        ResultActions resultActions = mvc.perform(get("/setting/unregister"));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(member.getLoginCredentials().getUsername()))
                .andExpect(jsonPath("$.companyName").isEmpty())
                .andExpect(jsonPath("$.realName")
                        .value(member.getStudentCredentials().getRealName()))
                .andDo(print());
    }

    @DisplayName("탈퇴를 요청한 회원의 정보 조회 성공 - 기업 회원인 경우")
    @WithMockMember(role = COMPANY)
    @Test
    void loadCompanyMemberProfileToUnregister() throws Exception {
        //given
        Member member = memberRepository.findActiveMemberWithCompanyBy(
                findContextMember().getLoginCredentials().getUsername()).get();

        //when
        ResultActions resultActions = mvc.perform(get("/setting/unregister"));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.realName").isEmpty())
                .andExpect(jsonPath("$.username")
                        .value(member.getLoginCredentials().getUsername()))
                .andExpect(jsonPath("$.companyName")
                        .value(member.getCompanyCredentials().getCompany().getName()))
                .andDo(print());
    }

    @DisplayName("탈퇴를 요청한 회원의 정보 조회 실패 - 로그인 하지 않은 경우")
    @Test
    void loadMemberProfileToUnregisterWithoutLogin() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(get("/setting/unregister"));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("회원 탈퇴 성공")
    @WithMockMember
    @Test
    void unregisterMember() throws Exception {
        Member member = findContextMember();

        //when
        ResultActions resultActions = mvc.perform(post("/setting/unregister")
                .content("password")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(unauthenticated())
                .andDo(print());

        assertThat(getContext().getAuthentication()).isNull();
        assertThat(memberRepository.findActiveMemberBy(
                member.getLoginCredentials().getUsername())).isEmpty();
    }

    @DisplayName("회원 탈퇴 실패 - 로그인을 하지 않은 경우")
    @Test
    void unregisterMemberWithoutLogin() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/setting/unregister")
                .content("password")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("회원 탈퇴 실패 - 비밀번호를 입력하지 않은 경우")
    @WithMockMember
    @Test
    void unregisterMemberWithoutProvidingPassword() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/setting/unregister"));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 본문의 형식이 올바르지 않습니다"))
                .andDo(print());
    }

    @DisplayName("회원 탈퇴 실패 - 비밀번호를 입력하지 않은 경우2")
    @WithMockMember
    @Test
    void unregisterMemberWithoutProvidingPassword2() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/setting/unregister")
                .content(""));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 본문의 형식이 올바르지 않습니다"))
                .andDo(print());
    }

    @DisplayName("프로필 사진 변경 성공")
    @WithMockMember
    @Test
    void changeProfileImage() throws Exception {
        Member member = findContextMember();

        //given
        String fileName = "testImage.png";
        Resource resource = resourceLoader.getResource("classpath:/static/images/" + fileName);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testImage.png",
                IMAGE_PNG_VALUE,
                resource.getInputStream()
        );

        //when
        ResultActions resultActions = mvc
                .perform(multipart("/setting/profile/image")
                        .file(file));

        //then
        Member findMember = memberRepository.findActiveMemberBy(member.getId()).get();

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl")
                        .value(findMember.getProfileImage().getOriginalUrl()))

                .andExpect(jsonPath("$.thumbnailUrl")
                        .value(findMember.getProfileImage().getThumbnailUrl()))
                .andDo(print());

    }


    @DisplayName("프로필 사진 변경 실패 - 로그인을 하지 않은 경우")
    @Test
    void changeProfileImageWithoutLogin() throws Exception {

        //given
        String fileName = "testImage.png";
        Resource resource = resourceLoader.getResource("classpath:/static/images/" + fileName);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testImage.png",
                IMAGE_PNG_VALUE,
                resource.getInputStream()
        );

        //when
        ResultActions resultActions = mvc
                .perform(multipart("/setting/profile/image")
                        .file(file));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());

    }

    @DisplayName("프로필 사진 변경 실패 - 업로드할 사진을 첨부하지 않은 경우")
    @WithMockMember
    @Test
    void changeProfileImageWithoutProvidingImage() throws Exception {
        //when
        ResultActions resultActions = mvc
                .perform(multipart("/setting/profile/image"));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("멀티 파트 요청에서 파일이 누락되었습니다"))
                .andDo(print());
    }


    @DisplayName("프로필 사진 변경 실패 - 비어있는 파일을 업로드 한 경우")
    @WithMockMember
    @Test
    void changeProfileImageProvidingEmptyFile() throws Exception {
        //given
        Resource resource = resourceLoader.getResource("");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                IMAGE_PNG_VALUE,
                resource.getInputStream()
        );

        //when
        ResultActions resultActions = mvc
                .perform(multipart("/setting/profile/image")
                        .file(file));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("업로드할 파일이 비어있습니다"))
                .andDo(print());

    }


    private Member findContextMember() {
        String username = getContext().getAuthentication().getName();
        return memberRepository.findActiveMemberBy(username).get();
    }
}
