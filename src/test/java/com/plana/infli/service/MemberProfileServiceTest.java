package com.plana.infli.service;

import static com.plana.infli.exception.custom.NotFoundException.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Role;
import com.plana.infli.domain.University;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.profile.MemberWithdrawalRequest;
import com.plana.infli.web.dto.request.profile.NicknameModifyRequest;
import com.plana.infli.web.dto.request.profile.PasswordConfirmRequest;
import com.plana.infli.web.dto.request.profile.PasswordModifyRequest;
import com.plana.infli.web.dto.response.profile.MemberProfileResponse;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberProfileServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberProfileService memberProfileService;
    @Autowired
    private UniversityRepository universityRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private UniversityFactory universityFactory;

    private String accessToken;

    @DisplayName("회원 정보 조회 테스트")
    @Test
    public void getMemberProfile(){
        Member member = createMember();

        MemberProfileResponse memberProfile = memberProfileService.getMemberProfile(member.getEmail());

        assertThat(memberProfile.getNickname()).isEqualTo(member.getNickname());
        assertThat(memberProfile.getEmail()).isEqualTo(member.getEmail());
        assertThat(memberProfile.getRole()).isEqualTo(member.getRole());
    }

    @DisplayName("회원 닉네임 변경 테스트")
    @Test
    public void modifyNickname(){
        Member member = createMember();

        NicknameModifyRequest nicknameModifyRequest = new NicknameModifyRequest("testEmail@naver.com", "changeNickname");

        boolean result = memberProfileService.modifyNickname(nicknameModifyRequest);

        assertThat(result).isEqualTo(true);
        assertThat(member.getNickname()).isEqualTo("changeNickname");
    }

    @DisplayName("비밀번호 확인 테스트 - 성공")
    @Test
    public void checkPasswordSuccess(){
        Member member = createMember();

        PasswordConfirmRequest passwordConfirmRequest = new PasswordConfirmRequest("testEmail@naver.com", "Test1234!");

        boolean result = memberProfileService.checkPassword(passwordConfirmRequest);

        assertThat(result).isEqualTo(true);
    }

    @DisplayName("비밀번호 확인 테스트 - 실패")
    @Test
    public void checkPasswordFail(){
        Member member = createMember();

        PasswordConfirmRequest passwordConfirmRequest = new PasswordConfirmRequest("testEmail@naver.com", "Test1234");

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> memberProfileService.checkPassword(passwordConfirmRequest));

        assertEquals("비밀번호가 일치하지 않습니다.", badRequestException.getMessage());
    }

    @DisplayName("회원 비밀번호 변경 테스트")
    @Test
    public void modifyPassword(){
        Member member = createMember();

        PasswordModifyRequest passwordModifyRequest = new PasswordModifyRequest("testEmail@naver.com", "changePassword!");

        boolean result = memberProfileService.modifyPassword(passwordModifyRequest);

        assertThat(result).isEqualTo(true);
        assertTrue(passwordEncoder.matches(passwordModifyRequest.getNewPassword(), member.getPassword()));
    }

    @DisplayName("프로필 사진 변경 테스트")
    @Test
    public void modifyProfileImage() throws Exception{
        signup();
        login();

        String fileName = "testImage.png";
        Resource resource = resourceLoader.getResource("classpath:/static/images/" + fileName);

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "testImage.png",
            MediaType.IMAGE_PNG_VALUE,
            resource.getInputStream()
        );

        ResultActions resultActions = mockMvc
            .perform(
                multipart("/member/profile/image/modify")
                    .file(file)
                    .header("Access-Token", accessToken))
            .andDo(print())
            .andExpect(status().isOk());

        String imageUrl = resultActions.andReturn().getResponse().getContentAsString();
        Member member = memberRepository.findByEmail("testEmail@naver.com")
            .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        assertThat(member.getProfileImageUrl()).isEqualTo(imageUrl);
    }

    @DisplayName("회원 탈퇴 테스트")
    @Test
    public void deleteMember(){
        Member member = createMember();

        MemberWithdrawalRequest memberWithdrawalRequest = new MemberWithdrawalRequest("testEmail@naver.com", "Lee");

        boolean result = memberProfileService.deleteMember(memberWithdrawalRequest);

        assertThat(result).isEqualTo(true);
        assertTrue(member.isDeleted());
    }

    public Member createMember(){
        University university = universityFactory.createUniversity("푸단대학교");

        Member member = new Member("testEmail@naver.com", "Test1234!", "LEE", "LSH", Role.UNCERTIFIED, university,
            passwordEncoder);

        return memberRepository.save(member);
    }

    public void signup() throws Exception {
        universityFactory.createUniversity("푸단대학교");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("email", "testEmail@naver.com");
        requestMap.put("name", "LEE");
        requestMap.put("password", "Test1234!");
        requestMap.put("nickname", "LSH");
        requestMap.put("universityId", "1");

        String content = objectMapper.writeValueAsString(requestMap);

        mockMvc
            .perform(
                post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
            )
            .andDo(print())
            .andExpect(status().isOk());
    }

    public void login() throws Exception{
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("email", "testEmail@naver.com");
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