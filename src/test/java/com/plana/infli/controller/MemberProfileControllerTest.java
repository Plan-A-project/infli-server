package com.plana.infli.controller;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.annotation.MockMvcTest;
import com.plana.infli.annotation.WithMockMember;
import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.Role;
import com.plana.infli.domain.University;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.web.dto.request.profile.MemberWithdrawalRequest;
import com.plana.infli.web.dto.request.profile.NicknameModifyRequest;
import com.plana.infli.web.dto.request.profile.PasswordConfirmRequest;
import com.plana.infli.web.dto.request.profile.PasswordModifyRequest;
import com.plana.infli.web.dto.response.profile.MemberProfileResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@MockMvcTest
@Transactional
class MemberProfileControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UniversityFactory universityFactory;
    @Autowired
    private MemberFactory memberFactory;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ResourceLoader resourceLoader;
    private String accessToken;


    @DisplayName("로그인 상태일 때")
    @Nested
    class Describe_UnderLoginCondition{

        @DisplayName("사용자는 내 프로필을 조회할 수 있다.")
        @WithMockMember
        @Test
        public void getMemberProfile() throws Exception{
            login();
            MemberProfileResponse responseDto = new MemberProfileResponse("youngjin", Role.STUDENT,
                "youngjin@gmail.com");

            ResultActions perform = mockMvc.perform(get("/member/profile")
                .header("Access-Token", accessToken)
                .contentType(MediaType.APPLICATION_JSON));

            String body = perform
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

            assertThat(body).isEqualTo(objectMapper.writeValueAsString(responseDto));

        }

        @DisplayName("사용자는 닉네임을 변경할 수 있다. - validation 통과")
        @WithMockMember
        @Test
        public void nicknameModify() throws Exception{
            login();
            NicknameModifyRequest requestDto = new NicknameModifyRequest("youngjin@gmail.com", "change");

            String content = objectMapper.writeValueAsString(requestDto);

            ResultActions perform = mockMvc.perform(post("/member/profile/nickname/modify")
                .header("Access-Token", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

            perform
                .andExpect(status().isOk());

            Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                    requestDto.getEmail()));

            assertThat(member.getNickname()).isEqualTo("change");
        }

        @DisplayName("사용자는 닉네임을 변경할 수 있다. - validation 조건 불충족")
        @WithMockMember
        @Test
        public void nicknameModify_400Exception() throws Exception{
            login();
            NicknameModifyRequest requestDto = new NicknameModifyRequest("youngjin@gmail.com", "changeNickname");

            String content = objectMapper.writeValueAsString(requestDto);

            ResultActions perform = mockMvc.perform(post("/member/profile/nickname/modify")
                .header("Access-Token", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

            perform
                .andExpect(status().isBadRequest());

            String response = perform.andReturn().getResponse().getContentAsString();
            assertThat(response).isEqualTo("{\"code\":400,\"message\":\"잘못된 요청입니다\",\"validation\":{\"afterNickname\":\"닉네임은 2~8자리여야 합니다. 한글, 영어, 숫자 조합 가능.\"}}");
        }

        @DisplayName("사용자는 비밀번호를 변경하거나 회원 탈퇴 시 비밀번호를 확인받는다. - 성공")
        @WithMockMember
        @Test
        public void passwordConfirm() throws Exception{
            login();
            PasswordConfirmRequest requestDto = new PasswordConfirmRequest("youngjin@gmail.com", "Test1234!");

            String content = objectMapper.writeValueAsString(requestDto);

            ResultActions perform = mockMvc.perform(post("/member/profile/password/confirm")
                .header("Access-Token", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

            perform
                .andExpect(status().isOk());
        }

        @DisplayName("사용자는 비밀번호를 변경하거나 회원 탈퇴 시 비밀번호를 확인받는다. - 실패")
        @WithMockMember
        @Test
        public void passwordConfirm_400Exception() throws Exception{
            login();
            PasswordConfirmRequest requestDto = new PasswordConfirmRequest("youngjin@gmail.com", "notMatch123!");

            String content = objectMapper.writeValueAsString(requestDto);

            ResultActions perform = mockMvc.perform(post("/member/profile/password/confirm")
                .header("Access-Token", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

            perform
                .andExpect(status().isBadRequest());

            String response = perform.andReturn().getResponse().getContentAsString();
            assertThat(response).isEqualTo("{\"code\":400,\"message\":\"비밀번호가 일치하지 않습니다.\",\"validation\":{}}");
        }

        @DisplayName("사용자는 비밀번호를 변경할 수 있다.")
        @WithMockMember
        @Test
        public void passwordModify() throws Exception{
            login();
            PasswordModifyRequest requestDto = new PasswordModifyRequest("youngjin@gmail.com", "newPassword1234!");

            String content = objectMapper.writeValueAsString(requestDto);

            ResultActions perform = mockMvc.perform(post("/member/profile/password/modify")
                .header("Access-Token", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

            perform
                .andExpect(status().isOk());

            Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                    requestDto.getEmail()));
            assertTrue(passwordEncoder.matches("newPassword1234!", member.getPassword()));
        }

        @DisplayName("사용자는 회원 탈퇴할 수 있다.")
        @WithMockMember
        @Test
        public void memberWithdrawal() throws Exception {
            login();

            MemberWithdrawalRequest requestDto = new MemberWithdrawalRequest("youngjin@gmail.com", "testNickname");

            String content = objectMapper.writeValueAsString(requestDto);

            ResultActions perform = mockMvc.perform(post("/member/profile/withdrawal")
                .header("Access-Token", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

            perform
                .andExpect(status().isOk());

            Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                    requestDto.getEmail()));

            assertTrue(member.isDeleted());
        }
    }

    @DisplayName("비로그인 상태일 때")
    @Nested
    class Describe_UnderNonLoginCondition{

        @DisplayName("사용자는 프로필을 조회할 수 없다.")
        @Test
        public void getMemberProfile_401Exception() throws Exception{
            signup();
            MemberProfileResponse responseDto = new MemberProfileResponse("testNickname", Role.STUDENT,
                "youngjin@gmail.com");

            ResultActions perform = mockMvc.perform(get("/member/profile")
                .contentType(MediaType.APPLICATION_JSON));

            perform
                .andExpect(status().isUnauthorized());
        }

        @DisplayName("사용자는 닉네임을 변경할 수 없다.")
        @Test
        public void nicknameModify() throws Exception{
            signup();
            NicknameModifyRequest requestDto = new NicknameModifyRequest("youngjin@gmail.com", "change");

            String content = objectMapper.writeValueAsString(requestDto);

            ResultActions perform = mockMvc.perform(post("/member/profile/nickname/modify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

            perform
                .andExpect(status().isUnauthorized());

            Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                    requestDto.getEmail()));

            assertThat(member.getNickname()).isNotEqualTo("change");
        }

        @DisplayName("사용자는 비밀번호 확인 요청을 할 수 없다.")
        @Test
        public void passwordConfirm() throws Exception{
            signup();

            PasswordConfirmRequest requestDto = new PasswordConfirmRequest("youngjin@gmail.com", "Test1234!");

            String content = objectMapper.writeValueAsString(requestDto);

            ResultActions perform = mockMvc.perform(post("/member/profile/password/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

            perform
                .andExpect(status().isUnauthorized());
        }

        @DisplayName("사용자는 비밀번호를 변경할 수 없다.")
        @Test
        public void passwordModify() throws Exception{
            signup();
            PasswordModifyRequest requestDto = new PasswordModifyRequest("youngjin@gmail.com", "newPassword1234!");

            String content = objectMapper.writeValueAsString(requestDto);

            ResultActions perform = mockMvc.perform(post("/member/profile/password/modify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

            perform
                .andExpect(status().isUnauthorized());

            Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                    requestDto.getEmail()));
            assertFalse(passwordEncoder.matches("newPassword1234!", member.getPassword()));
        }

        @DisplayName("사용자는 프로필 사진을 변경할 수 없다.")
        @Test
        public void profileImageModify() throws Exception{
            signup();
            String fileName = "testImage.png";
            Resource resource = resourceLoader.getResource("classpath:/static/images/" + fileName);

            MockMultipartFile file = new MockMultipartFile(
                "file",
                "testImage.png",
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
            );

            ResultActions perform = mockMvc
                .perform(
                    multipart("/member/profile/image/modify")
                        .file(file));

            perform
                .andDo(print())
                .andExpect(status().isUnauthorized());

            String imageUrl = perform.andReturn().getResponse().getContentAsString();
            Member member = memberRepository.findByEmail("youngjin@gmail.com")
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

            assertThat(member.getProfileImageUrl()).isNotEqualTo(imageUrl);
        }

        @DisplayName("사용자는 회원 탈퇴할 수 없다.")
        @Test
        public void memberWithdrawal() throws Exception {
            signup();
            MemberWithdrawalRequest requestDto = new MemberWithdrawalRequest("youngjin@gmail.com", "testNickname");

            String content = objectMapper.writeValueAsString(requestDto);

            ResultActions perform = mockMvc.perform(post("/member/profile/withdrawal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

            perform
                .andExpect(status().isUnauthorized());

            Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                    requestDto.getEmail()));

            assertFalse(member.isDeleted());
        }


    }
    public void signup() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createStudentMember("youngjin", university);
    }

    public void login() throws Exception{
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("email", "youngjin@gmail.com");
        requestMap.put("password", "Test1234!");

        String content = objectMapper.writeValueAsString(requestMap);

        ResultActions resultActions = mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
            )
            .andDo(print())
            .andExpect(status().isOk());

        accessToken = resultActions.andReturn().getResponse().getHeader("Access-Token");
    }
}