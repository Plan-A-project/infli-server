package com.plana.infli.controller;

import static java.lang.String.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.core.context.SecurityContextHolder.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.annotation.MockMvcTest;
import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.University;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.infra.security.filter.CustomLoginProcessingFilter.Login;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.request.member.signup.company.CreateCompanyMemberRequest;
import com.plana.infli.web.dto.request.member.signup.company.CreateCompanyMemberServiceRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberServiceRequest;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@MockMvcTest
class AuthControllerTest {

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
    private MemberService memberService;


    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }

    @DisplayName("학생 회원 회원 가입")
    @Test
    void studentMemberSignup() throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("infli")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        Member member = memberRepository.findAll().get(0);
        resultActions
                .andExpect(status().isCreated())
                .andExpect(content().string(valueOf(member.getId())))
                .andDo(print());

        assertThat(member.getStudentCredentials().getRealName()).isEqualTo(request.getRealName());
        assertThat(member.getBasicCredentials().getNickname()).isEqualTo(request.getNickname());
        assertThat(member.getLoginCredentials().getUsername()).isEqualTo(request.getUsername());
        assertThat(member.getUniversity().getId()).isEqualTo(university.getId());
    }

    @DisplayName("기업 회원 회원 가입")
    @Test
    void companyMemberSignup() throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("infli")
                .nickname("jin8743")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        Member member = memberRepository.findAll().get(0);

        resultActions
                .andExpect(status().isCreated())
                .andExpect(content().string(valueOf(member.getId())))
                .andDo(print());

        assertThat(member.getBasicCredentials().getNickname()).isEqualTo(request.getNickname());
        assertThat(member.getLoginCredentials().getUsername()).isEqualTo(request.getUsername());
        assertThat(member.getUniversity().getId()).isEqualTo(university.getId());
    }

    public static Stream<Arguments> provideValidUsernames() {

        return Stream.of(
                Arguments.of("a".repeat(5)),
                Arguments.of("a".repeat(20)),
                Arguments.of("1".repeat(5)),
                Arguments.of("1".repeat(20)),
                Arguments.of("-".repeat(5)),
                Arguments.of("-".repeat(20)),
                Arguments.of("_".repeat(5)),
                Arguments.of("_".repeat(20)),
                Arguments.of("a1".repeat(5)),
                Arguments.of("a-".repeat(5)),
                Arguments.of("a_".repeat(5)),
                Arguments.of("-_".repeat(5)),
                Arguments.of("1-".repeat(5)),
                Arguments.of("1_".repeat(5))
        );
    }

    @DisplayName("학생 회원 회원가입 성공 - Username 정규표현식 옳바른 유형")
    @ParameterizedTest(name = "{index} Username 유형: {0}")
    @MethodSource("provideValidUsernames")
    void SUCCESS_ValidStudentUsernames(String username) throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username(username)
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isCreated());
    }

    @DisplayName("기업 회원 회원가입 성공 - Username 정규표현식 옳바른 유형")
    @ParameterizedTest(name = "{index} Username 유형: {0}")
    @MethodSource("provideValidUsernames")
    void SUCCESS_ValidCompanyUsernames(String username) throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username(username)
                .nickname("jin8743")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isCreated());
    }

    public static Stream<Arguments> provideInvalidUsernames() {

        return Stream.of(
                Arguments.of("A"),
                Arguments.of("A".repeat(21)),
                Arguments.of("ㄱ"),
                Arguments.of("ㄱ".repeat(21)),
                Arguments.of("3"),
                Arguments.of("a"),
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of(" ".repeat(21)),
                Arguments.of("!"),
                Arguments.of("!".repeat(21)),
                Arguments.of("a".repeat(21)),
                Arguments.of("1".repeat(21)),
                Arguments.of("-".repeat(21)),
                Arguments.of("_".repeat(21))
        );
    }

    @DisplayName("학생 회원 회원가입 실패 - Username 정규표현식 잘못된 유형")
    @ParameterizedTest(name = "{index} Username 유형: {0}")
    @MethodSource("provideInvalidUsernames")
    void FAIL_InvalidStudentUsernames(String username) throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username(username)
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.username").value(
                        "영어, 숫자, 특수문자 -, _ 를 포함해서 5~20자리 이내로 입력해주세요"));
    }

    @DisplayName("기업 회원 회원가입 실패 - Username 정규표현식 잘못된 유형")
    @ParameterizedTest(name = "{index} Username 유형: {0}")
    @MethodSource("provideInvalidUsernames")
    void FAIL_InvalidCompanyUsernames(String username) throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username(username)
                .nickname("jin8743")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.username").value(
                        "영어, 숫자, 특수문자 -, _ 를 포함해서 5~20자리 이내로 입력해주세요"));
    }


    public static Stream<Arguments> provideValidRealNames() {

        return Stream.of(
                Arguments.of("이영진"),
                Arguments.of("가".repeat(2)),
                Arguments.of("가".repeat(10)),
                Arguments.of("힣".repeat(2)),
                Arguments.of("힣".repeat(10))
        );
    }

    @DisplayName("학생 회원 회원가입 성공 - 실명 정규표현식 옳바른 유형")
    @ParameterizedTest(name = "{index} 실명 : {0}")
    @MethodSource("provideValidRealNames")
    void SUCCESS_ValidRealNames(String realName) throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName(realName)
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isCreated());
    }


    public static Stream<Arguments> provideInvalidRealNames() {

        return Stream.of(
                Arguments.of("가".repeat(1)),
                Arguments.of("가".repeat(11)),
                Arguments.of("힣".repeat(1)),
                Arguments.of("힣".repeat(11)),

                Arguments.of("a".repeat(1)),
                Arguments.of("a".repeat(2)),
                Arguments.of("a".repeat(10)),
                Arguments.of("a".repeat(11)),

                Arguments.of("A".repeat(1)),
                Arguments.of("A".repeat(2)),
                Arguments.of("A".repeat(10)),
                Arguments.of("A".repeat(11)),

                Arguments.of("1".repeat(1)),
                Arguments.of("1".repeat(2)),
                Arguments.of("1".repeat(10)),
                Arguments.of("1".repeat(11)),

                Arguments.of("ㄱ".repeat(1)),
                Arguments.of("ㄱ".repeat(2)),
                Arguments.of("ㄱ".repeat(10)),
                Arguments.of("ㄱ".repeat(11)),

                Arguments.of("ㅏ".repeat(1)),
                Arguments.of("ㅏ".repeat(2)),
                Arguments.of("ㅏ".repeat(10)),
                Arguments.of("ㅏ".repeat(11)),

                Arguments.of(""),
                Arguments.of(" ".repeat(1)),
                Arguments.of(" ".repeat(2)),
                Arguments.of(" ".repeat(10)),
                Arguments.of(" ".repeat(11)),

                Arguments.of("!".repeat(1)),
                Arguments.of("!".repeat(2)),
                Arguments.of("!".repeat(10)),
                Arguments.of("!".repeat(11))
        );
    }

    @DisplayName("회원가입 실패 - 실명 정규표현식 잘못된 유형")
    @ParameterizedTest(name = "{index} 실명 : {0}")
    @MethodSource("provideInvalidRealNames")
    void Fail_InvalidRealNames(String realName) throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName(realName)
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.realName").value("이름은 한글로 2~10자리 이내로 입력해주세요"));
    }


    public static Stream<Arguments> provideValidPasswords() {

        return Stream.of(
                Arguments.of("AbcDefg1!"),
                Arguments.of("eFg456@Hij789#K"),
                Arguments.of("LmN012$oPQ345%R"),
                Arguments.of("StuV678^WxY901&Z"),
                Arguments.of("aBc567(Def8)Gh"),
                Arguments.of("Ijkl9KmOpq0!@#L"),
                Arguments.of("!@#MnoPqr1@%Rst"),
                Arguments.of("Uvw2Xyz3^&*(ABC"),
                Arguments.of("DEF4Ghij567%Hij"),
                Arguments.of("789IJklMno0!@#P"),
                Arguments.of("QRStu2Vwx3^&*xY"),
                Arguments.of("Yzabc4Def5(6Gh"),
                Arguments.of("7IJklmN8901@%O"),
                Arguments.of("pqRStu2vwx3^&*Yz"),
                Arguments.of("ABc4DEf5(6ghI"),
                Arguments.of("JK7LMN8OP9#QRS"),
                Arguments.of("TUVW2XYZ3^&*zab"),
                Arguments.of("cdefGHI4jk5(6L"),
                Arguments.of("MNOP7QrSt8@%uv"),
                Arguments.of("wxyzAbcD9(0EFG"),
                Arguments.of("HIJklMN9o0!@#PQR"),
                Arguments.of("STU2VWX3^&*YZa"),
                Arguments.of("BCDef4Ghij5(6kl"),
                Arguments.of("mnNOPQR7st8@%UV"),
                Arguments.of("WxYzabc9Def0GHI!"),
                Arguments.of("JklMNO2PQR3^&*RS"),
                Arguments.of("TuV4WXY5^&*zab"),
                Arguments.of("cdeFGHI6jk7(8LM"),
                Arguments.of("NOPQR9st8@%uVW"),
                Arguments.of("WxYZa1bc2Def3EF@"));
    }

    @DisplayName("학생 회원 회원가입 성공 - 비밀번호 정규표현식 옳바른 유형")
    @ParameterizedTest(name = "{index} 비밀번호 : {0}")
    @MethodSource("provideValidPasswords")
    void SUCCESS_ValidStudentPassword(String password) throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName("이영진")
                .password(password)
                .passwordConfirm(password)
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isCreated());
    }

    @DisplayName("기업 회원 회원가입 성공 - 비밀번호 정규표현식 옳바른 유형")
    @ParameterizedTest(name = "{index} Username 유형: {0}")
    @MethodSource("provideValidPasswords")
    void SUCCESS_ValidCompanyPassword(String password) throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("infli12")
                .nickname("jin8743")
                .password(password)
                .passwordConfirm(password)
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isCreated());
    }


    public static Stream<Arguments> provideInvalidPasswords() {

        return Stream.of(
                Arguments.of("1234567890!"),
                Arguments.of("!@#Abcdefghi"),
                Arguments.of(""),
                Arguments.of("!@#23456789"),
                Arguments.of("AbcdeFgh1"),
                Arguments.of("12345678901!@#"),
                Arguments.of("!@#AbcdEF"),
                Arguments.of("123!@#"),
                Arguments.of("Abcdefghi"),
                Arguments.of("!@#"),
                Arguments.of("12345678901234567890"),
                Arguments.of("ABCDEFGHI"),
                Arguments.of("abcdefgh"),
                Arguments.of("12345678"),
                Arguments.of("Ab1"),
                Arguments.of("12345678A"),
                Arguments.of("1!@#2"),
                Arguments.of("!@#23456789012345"),
                Arguments.of("aBcDeF"),
                Arguments.of("TuV4WXY5^&*zab-"),
                Arguments.of("567890ABCDEF_-"),
                Arguments.of("_ABc4DEf5(6ghI"),
                Arguments.of("JK7LMN8-OP9#QRS"),
                Arguments.of("TuV4WXY5_^&*zab"),
                Arguments.of("cdefGHI6j k7(8LM"),
                Arguments.of("567890ABCDEF_-"),
                Arguments.of("NOPQRst@%uVW"),
                Arguments.of("567890ABCDEF_-"),
                Arguments.of("NOPQRst-@%uVW"),
                Arguments.of("WxYZa1bc2Def3EF"));
    }

    @DisplayName("학생 회원 회원가입 실패 - 비밀번호 정규표현식 잘못된 유형")
    @ParameterizedTest(name = "{index} 비밀번호 : {0}")
    @MethodSource("provideInvalidPasswords")
    void FAIL_InvalidStudentPassword(String password) throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName("이영진")
                .password(password)
                .passwordConfirm(password)
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.password").value(
                        "비밀번호는 영어, 숫자, 특수문자를 포함해서 8~20자리 이내로 입력해주세요."));
    }

    @DisplayName("기업 회원 회원가입 실패 - 비밀번호 정규표현식 잘못된 유형")
    @ParameterizedTest(name = "{index} 비밀번호 유형: {0}")
    @MethodSource("provideInvalidPasswords")
    void FAIL_InvalidCompanyPassword(String password) throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("username")
                .nickname("jin8743")
                .password(password)
                .passwordConfirm(password)
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.password").value(
                        "비밀번호는 영어, 숫자, 특수문자를 포함해서 8~20자리 이내로 입력해주세요."));
    }


    public static Stream<Arguments> provideValidNicknames() {

        return Stream.of(
                Arguments.of("가나다"),
                Arguments.of("00"),
                Arguments.of("99"),
                Arguments.of("aa"),
                Arguments.of("zz"),
                Arguments.of("AA"),
                Arguments.of("ZZ"),
                Arguments.of("Aㄱ"),
                Arguments.of("Zㅎ"),
                Arguments.of("ZZ"),
                Arguments.of("가가"),
                Arguments.of("힣힣"),
                Arguments.of("ㅏㅏ"),
                Arguments.of("ㅣㅣ"),
                Arguments.of("ㄱㄱ"),
                Arguments.of("ㅎㅎ"),
                Arguments.of("0ㄱ"),
                Arguments.of("9ㅎ"),
                Arguments.of("0가"),
                Arguments.of("9힣"),
                Arguments.of("12345678"),
                Arguments.of("a".repeat(8)),
                Arguments.of("A".repeat(8)),
                Arguments.of("ㄱ".repeat(8)),
                Arguments.of("가".repeat(8)));
    }

    @DisplayName("학생 회원 회원가입 성공 - 닉네임 정규표현식 옳바른 유형")
    @ParameterizedTest(name = "{index} 닉네임 : {0}")
    @MethodSource("provideValidNicknames")
    void SUCCESS_ValidStudentNickname(String nickname) throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname(nickname)
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isCreated());
    }

    @DisplayName("기업 회원 회원가입 성공 - 닉네임 정규표현식 옳바른 유형")
    @ParameterizedTest(name = "{index} 닉네임 유형: {0}")
    @MethodSource("provideValidNicknames")
    void SUCCESS_ValidCompanyNickname(String nickname) throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("infli12")
                .nickname(nickname)
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions
                .andExpect(status().isCreated());
    }


    public static Stream<Arguments> provideInvalidNicknames() {

        return Stream.of(
                Arguments.of("1"),
                Arguments.of("가"),
                Arguments.of("가나다라마바사아자"),
                Arguments.of("abcdefghijklmnopqrstuvwxyz"),
                Arguments.of("테스트!"),
                Arguments.of("123456789abcdefg"),
                Arguments.of("테스트@123"),
                Arguments.of("test!@#$%^"),
                Arguments.of("A"),
                Arguments.of("@#$%^&*()"),
                Arguments.of("123456789abcdefghijklmnopqrstuvwxy"),
                Arguments.of("ㅏ"),
                Arguments.of("ㄱ"),
                Arguments.of("ㅎ"),
                Arguments.of("ㅣ"));
    }

    @DisplayName("학생 회원가입 실패 - 닉네임 정규표현식 잘못된 유형")
    @ParameterizedTest(name = "{index} 닉네임 : {0}")
    @MethodSource("provideInvalidNicknames")
    void FAIL_InvalidStudentNickname(String nickname) throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname(nickname)
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.nickname").value(
                        "한글, 영어, 숫자를 포함해서 2~8자리 이내로 입력해주세요"));
    }

    @DisplayName("기업 회원 회원가입 실패 - 닉네임 정규표현식 잘못된 유형")
    @ParameterizedTest(name = "{index} 닉네임 유형: {0}")
    @MethodSource("provideInvalidNicknames")
    void FAIL_InvalidCompanyNickname(String nickname) throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("username")
                .nickname(nickname)
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.nickname").value(
                        "한글, 영어, 숫자를 포함해서 2~8자리 이내로 입력해주세요"));
    }


    @DisplayName("학생 회원 회원가입 실패 - username은  필수다")
    @Test
    void usernameNotProvidedInStudentSignup() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username(null)
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.username").value("아이디를 입력해주세요"));
    }

    @DisplayName("학생 회원 회원가입 실패 - username은  필수다2")
    @Test
    void usernameNotProvidedInStudentSignup2() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.username").value(
                        "영어, 숫자, 특수문자 -, _ 를 포함해서 5~20자리 이내로 입력해주세요"));
    }


    @DisplayName("기업 회원 회원가입 실패 - username은 필수다")
    @Test
    void usernameNotProvidedInCompanySignup() throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username(null)
                .nickname("jin8743")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.username").value("아이디를 입력해주세요"));
    }

    @DisplayName("기업 회원 회원가입 실패 - username은 필수다2")
    @Test
    void usernameNotProvidedInCompanySignup2() throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("")
                .nickname("jin8743")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.username").value(
                        "영어, 숫자, 특수문자 -, _ 를 포함해서 5~20자리 이내로 입력해주세요"));
    }


    @DisplayName("학생 회원 회원가입 실패 - 이름은  필수다")
    @Test
    void realNameNotProvidedInStudentSignup() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName(null)
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.realName").value("이름을 입력해주세요"));
    }

    @DisplayName("학생 회원 회원가입 실패 - 이름은  필수다2")
    @Test
    void realNameNotProvidedInStudentSignup2() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName("")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.realName").value("이름은 한글로 2~10자리 이내로 입력해주세요"));
    }


    @DisplayName("학생 회원 회원가입 실패 - 비밀번호는 필수다")
    @Test
    void passwordNotProvidedInStudentSignup() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName("이영진")
                .password(null)
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.password").value("비밀번호를 입력해주세요"));
    }

    @DisplayName("학생 회원 회원가입 실패 - 비밀번호는 필수다2")
    @Test
    void passwordNotProvidedInStudentSignup2() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName("이영진")
                .password("")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.password").value(
                        "비밀번호는 영어, 숫자, 특수문자를 포함해서 8~20자리 이내로 입력해주세요."));
    }


    @DisplayName("기업 회원 회원가입 실패 - 비밀번호는 필수다")
    @Test
    void passwordNotProvidedInCompanySignup() throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("username")
                .nickname("jin8743")
                .password(null)
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.password").value("비밀번호를 입력해주세요"));
    }


    @DisplayName("기업 회원 회원가입 실패 - 비밀번호는 필수다2")
    @Test
    void passwordNotProvidedInCompanySignup2() throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("username")
                .nickname("jin8743")
                .password("")
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.password").value(
                        "비밀번호는 영어, 숫자, 특수문자를 포함해서 8~20자리 이내로 입력해주세요."));
    }


    @DisplayName("학생 회원 회원가입 실패 - 비밀번호 확인은 필수다")
    @Test
    void passwordConfirmIsNotProvided() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm(null)
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.passwordConfirm").value("비밀번호 확인을 입력해주세요"));
    }

    @DisplayName("학생 회원 회원가입 실패 - 비밀번호 확인은 필수다 2")
    @Test
    void passwordConfirmIsNotProvided2() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("")
                .nickname("jin8743")
                .universityId(university.getId())
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.passwordConfirm").value("비밀번호 확인을 입력해주세요"));
    }


    @DisplayName("기업 회원 회원가입 실패 - 비밀번호 확인은 필수다")
    @Test
    void passwordConfirmIsMandatory() throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("username")
                .nickname("jin8743")
                .password("password1234!")
                .passwordConfirm(null)
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.passwordConfirm").value("비밀번호 확인을 입력해주세요"));
    }

    @DisplayName("기업 회원 회원가입 실패 - 비밀번호 확인은 필수다2")
    @Test
    void passwordConfirmIsMandatory2() throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("username")
                .nickname("jin8743")
                .password("password1234!")
                .passwordConfirm("")
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.passwordConfirm").value("비밀번호 확인을 입력해주세요"));
    }


    @DisplayName("학생 회원 회원가입 실패 - 대학교 ID 번호는 필수다")
    @Test
    void universityIdIsNotProvided() throws Exception {

        //given
        CreateStudentMemberRequest request = CreateStudentMemberRequest.builder()
                .username("username")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(null)
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/student")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.universityId").value("대학교 번호는 필수입니다."));
    }


    @DisplayName("기업 회원 회원가입 실패 - 대학교 ID 번호는 필수다2")
    @Test
    void universityIdIsNotProvidedInCompanySignup() throws Exception {

        //given
        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("username")
                .nickname("jin8743")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(null)
                .companyName("카카오")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.universityId").value("대학교 번호는 필수입니다."));
    }


    @DisplayName("기업 회원 회원가입 실패 - 회사명은  필수다")
    @Test
    void companyNameIsNotProvidedInCompanySignup() throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("username")
                .nickname("jin8743")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName(null)
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.companyName").value("회사 이름을 입력해주세요"));
    }

    @DisplayName("기업 회원 회원가입 실패 - 회사명은  필수다2")
    @Test
    void companyNameIsNotProvidedInCompanySignup2() throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");

        CreateCompanyMemberRequest request = CreateCompanyMemberRequest.builder()
                .username("username")
                .nickname("jin8743")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName("")
                .build();

        String json = om.writeValueAsString(request);

        //when
        ResultActions resultActions = mvc.perform(post("/signup/company")
                .content(json)
                .contentType(APPLICATION_JSON)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
                .andExpect(jsonPath("$.validation.companyName").value("회사 이름을 입력해주세요"));
    }


    @DisplayName("아이디 중복 확인 - 중복되지 않는 경우")
    @Test
    void checkUsernameDuplicate_NotDuplicated() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(
                get("/signup/username/{username}", "infli1234")
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("사용 가능한 아이디 입니다"));
    }


    @DisplayName("아이디 중복 확인 - 중복되는 경우")
    @Test
    void checkUsernameDuplicate_Duplicated() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        memberService.signupAsStudentMember(CreateStudentMemberServiceRequest.builder()
                .username("infli")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                get("/signup/username/{username}", "infli")
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용중인 ID 입니다"));
    }


    @DisplayName("닉네임 중복 확인 - 중복되지 않는 경우")
    @Test
    void checkNicknameDuplicate_NotDuplicated() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(
                get("/signup/nickname/{nickname}", "infli123")
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("사용 가능한 닉네임 입니다"));
    }

    @DisplayName("닉네임 중복 확인 - 중복되는 경우")
    @Test
    void checkNicknameDuplicate_Duplicated() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        memberService.signupAsStudentMember(CreateStudentMemberServiceRequest.builder()
                .username("infli")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                get("/signup/nickname/{nickname}", "jin8743")
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다."));
    }


    @DisplayName("로그인 성공 - 학생 회원 로그인")
    @Test
    void login() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        memberService.signupAsStudentMember(CreateStudentMemberServiceRequest.builder()
                .username("infli")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build());

        String json = om.writeValueAsString(Login.builder()
                .username("infli")
                .password("password1234!")
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                post("/login")
                        .content(json)
                        .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(authenticated().withUsername("infli"));
    }

    @DisplayName("로그인 성공 - 기업 회원 로그인")
    @Test
    void CompanyMemberLoginSuccess() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        memberService.signupAsCompanyMember(CreateCompanyMemberServiceRequest.builder()
                .username("infli")
                .nickname("jin8743")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName("카카오")
                .build());

        String json = om.writeValueAsString(Login.builder()
                .username("infli")
                .password("password1234!")
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                post("/login")
                        .content(json)
                        .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(authenticated().withUsername("infli"));
    }

    @DisplayName("로그인 실패 - 잘못된 아이디")
    @Test
    void invalidUsername() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        memberService.signupAsStudentMember(CreateStudentMemberServiceRequest.builder()
                .username("infli")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build());

        String json = om.writeValueAsString(Login.builder()
                .username("aaa")
                .password("password1234!")
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                post("/login")
                        .content(json)
                        .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("아이디 또는 비밀번호를 잘못 입력했습니다."))
                .andExpect(unauthenticated());
    }

    @DisplayName("로그인 실패 - 잘못된 아이디2")
    @Test
    void invalidUsername2() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        memberService.signupAsStudentMember(CreateStudentMemberServiceRequest.builder()
                .username("infli")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build());

        String json = om.writeValueAsString(Login.builder()
                .username(null)
                .password("password1234!")
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                post("/login")
                        .content(json)
                        .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("아이디와 비밀번호는 공백일수 없습니다."))
                .andExpect(unauthenticated());
    }

    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    @Test
    void invalidPassword() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        memberService.signupAsStudentMember(CreateStudentMemberServiceRequest.builder()
                .username("infli")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build());

        String json = om.writeValueAsString(Login.builder()
                .username("infli")
                .password("123456")
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                post("/login")
                        .content(json)
                        .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("아이디 또는 비밀번호를 잘못 입력했습니다."))
                .andExpect(unauthenticated());
    }

    @DisplayName("로그인 실패 - 잘못된 비밀번호2")
    @Test
    void invalidPassword2() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        memberService.signupAsStudentMember(CreateStudentMemberServiceRequest.builder()
                .username("infli")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build());

        String json = om.writeValueAsString(Login.builder()
                .username("infli")
                .password(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                post("/login")
                        .content(json)
                        .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("아이디와 비밀번호는 공백일수 없습니다."))
                .andExpect(unauthenticated());
    }

    @DisplayName("로그인 실패 - 탈퇴한 회원의 계정으로 로그인 할수 없다")
    @Test
    void loginByUnregisteredMember() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Long memberId = memberService.signupAsStudentMember(
                CreateStudentMemberServiceRequest.builder()
                        .username("infli")
                        .realName("이영진")
                        .password("password1234!")
                        .passwordConfirm("password1234!")
                        .nickname("jin8743")
                        .universityId(university.getId())
                        .build());

        memberRepository.deleteById(memberId);

        String json = om.writeValueAsString(Login.builder()
                .username("infli")
                .password("password1234!")
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                post("/login")
                        .content(json)
                        .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("아이디 또는 비밀번호를 잘못 입력했습니다."))
                .andExpect(unauthenticated());
    }

    @DisplayName("로그인 실패 - HTTP GET 으로 요청한 경우")
    @Test
    void invalidLoginGETRequest() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        memberService.signupAsStudentMember(CreateStudentMemberServiceRequest.builder()
                .username("infli")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build());

        String json = om.writeValueAsString(Login.builder()
                .username("infli")
                .password(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                get("/login")
                        .content(json)
                        .contentType(APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("지원되지 않는 로그인 방식입니다."))
                .andExpect(unauthenticated());
    }

    @DisplayName("로그아웃 - 성공")
    @TestFactory
    Collection<DynamicTest> successfulLogout() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        memberService.signupAsStudentMember(CreateStudentMemberServiceRequest.builder()
                .username("infli")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build());

        return List.of(
                dynamicTest("로그인 성공",
                        () -> {
                            //given
                            String json = om.writeValueAsString(Login.builder()
                                    .username("infli")
                                    .password("password1234!")
                                    .build());
                            //when
                            ResultActions resultActions = mvc.perform(
                                    post("/login")
                                            .content(json)
                                            .contentType(APPLICATION_JSON));

                            //then
                            resultActions.andExpect(status().isOk())
                                    .andExpect(authenticated().withUsername("infli"));
                        }),

                dynamicTest("로그아웃 요청",
                        () -> {
                            //when
                            ResultActions resultActions = mvc.perform(
                                    post("/logout"));

                            //then
                            resultActions.andExpect(status().isOk())
                                    .andExpect(unauthenticated());

                            assertThat(getContext().getAuthentication()).isNull();
                        })
        );
    }

    @DisplayName("로그아웃 실패 - HTTP GET 방식으로 로그아웃을 할수 없다")
    @TestFactory
    Collection<DynamicTest> invalidGETLogoutRequest() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        memberService.signupAsStudentMember(CreateStudentMemberServiceRequest.builder()
                .username("infli")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin8743")
                .universityId(university.getId())
                .build());

        return List.of(
                dynamicTest("로그인 성공",
                        () -> {
                            //given
                            String json = om.writeValueAsString(Login.builder()
                                    .username("infli")
                                    .password("password1234!")
                                    .build());
                            //when
                            ResultActions resultActions = mvc.perform(
                                    post("/login")
                                            .content(json)
                                            .contentType(APPLICATION_JSON));

                            //then
                            resultActions.andExpect(status().isOk())
                                    .andExpect(authenticated().withUsername("infli"));
                        }),

                dynamicTest("로그아웃 요청",
                        () -> {
                            //when
                            ResultActions resultActions = mvc.perform(
                                    get("/logout"));

                            //then
                            resultActions.andExpect(status().isUnauthorized())
                                    .andDo(print());
                        })
        );
    }

}
