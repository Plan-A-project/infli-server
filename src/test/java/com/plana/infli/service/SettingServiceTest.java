package com.plana.infli.service;

import static com.plana.infli.domain.type.Role.ADMIN;
import static com.plana.infli.domain.type.Role.COMPANY;
import static com.plana.infli.domain.type.Role.STUDENT;
import static com.plana.infli.domain.type.Role.STUDENT_COUNCIL;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_NICKNAME;
import static java.io.InputStream.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.type.Role;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.setting.modify.nickname.ModifyNicknameServiceRequest;
import com.plana.infli.web.dto.request.setting.modify.password.ModifyPasswordServiceRequest;
import com.plana.infli.web.dto.request.setting.verify.password.VerifyPasswordServiceRequest;
import com.plana.infli.web.dto.response.profile.MyProfileResponse;
import com.plana.infli.web.dto.response.profile.MyProfileToUnregisterResponse;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@SpringBootTest
class SettingServiceTest {

    @Autowired
    private SettingService settingService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ResourceLoader resourceLoader;


    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }

    public static Stream<Arguments> memberRoles() {
        return Stream.of(
                Arguments.of(STUDENT),
                Arguments.of(COMPANY),
                Arguments.of(ADMIN),
                Arguments.of(STUDENT_COUNCIL)
        );
    }

    @DisplayName("내 프로필 조회 성공")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("memberRoles")
    void loadMyProfile(Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        //when
        MyProfileResponse response =
                settingService.loadMyProfile(member.getLoginCredentials().getUsername());

        //then
        assertThat(response)
                .extracting("nickname", "username", "role", "thumbnailUrl", "originalUrl")
                .containsExactly(
                        member.getBasicCredentials().getNickname(),
                        member.getLoginCredentials().getUsername(),
                        member.getRole(),
                        member.getProfileImage().getThumbnailUrl(),
                        member.getProfileImage().getOriginalUrl());
    }


    @DisplayName("내 프로필 조회 실패 - 존재하지 않는 회원인 경우")
    @Test
    void loadNotExistingMemberProfile() {
        //when //then
        assertThatThrownBy(
                () -> settingService.loadMyProfile("aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("내 프로필 조회 실패 - 탈퇴한 회원인 경우")
    @Test
    void loadDeletedMemberProfile() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("member", university);

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(
                () -> settingService.loadMyProfile(member.getLoginCredentials().getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("사용 가능한 새로운 닉네임인지 여부 확인 - 사용가능한 경우")
    @Test
    void checkIsValidNickname() {
        //when
        String response = settingService.checkIsAvailableNewNickname("infli12");

        //then
        assertThat(response).isEqualTo("사용 가능한 닉네임");
    }

    @DisplayName("사용 가능한 새로운 닉네임인지 여부 확인 실패- 이미 사용중인 닉네임인 경우")
    @Test
    void checkIsValidNicknameFail_Duplicated() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        //when
        assertThatThrownBy(() -> settingService.checkIsAvailableNewNickname(
                member.getBasicCredentials().getNickname()))
                .isInstanceOf(ConflictException.class)
                .message().isEqualTo("이미 존재하는 닉네임입니다.");
    }

    @DisplayName("사용 가능한 새로운 닉네임인지 여부 확인 실패 - 닉네임 규칙에 맞지 않는 경우")
    @Test
    void checkIsValidNicknameFail_InvalidRegex() {
        //when
        assertThatThrownBy(() -> settingService.checkIsAvailableNewNickname("!!!!!!!!!!!!!!"))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo(INVALID_NICKNAME);
    }

    @DisplayName("닉네임 변경 성공")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("memberRoles")
    void changeMemberNickname(Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        ModifyNicknameServiceRequest request = ModifyNicknameServiceRequest.builder()
                .nickname("newName")
                .username(member.getLoginCredentials().getUsername())
                .build();

        //when
        settingService.changeNickname(request);

        //then
        Member findMember = memberRepository.findActiveMemberBy(member.getId()).get();
        assertThat(findMember.getBasicCredentials().getNickname()).isEqualTo("newName");
    }

    @DisplayName("닉네임 변경 실패 - 회원이 존재하지 않을 경우")
    @Test
    void changeNotExistingMemberNickname() {
        //given
        ModifyNicknameServiceRequest request = ModifyNicknameServiceRequest.builder()
                .nickname("newName")
                .username("aaa")
                .build();

        //when //then
        assertThatThrownBy(() -> settingService.changeNickname(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("닉네임 변경 실패 - 해당 회원이 탈퇴한 회원인 경우")
    @Test
    void changeDeletedMemberNickname() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        ModifyNicknameServiceRequest request = ModifyNicknameServiceRequest.builder()
                .nickname("newName")
                .username(member.getLoginCredentials().getUsername())
                .build();

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> settingService.changeNickname(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("닉네임 변경 실패 - 이미 사용중인 닉네임인 경우")
    @Test
    void changeNicknameToAlreadyExistingNickname() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        Member member2 = memberFactory.createVerifiedStudentMember("newName", university);

        ModifyNicknameServiceRequest request = ModifyNicknameServiceRequest.builder()
                .nickname(member2.getBasicCredentials().getNickname())
                .username(member.getLoginCredentials().getUsername())
                .build();


        //when //then
        assertThatThrownBy(() -> settingService.changeNickname(request))
                .isInstanceOf(ConflictException.class)
                .message().isEqualTo("이미 존재하는 닉네임입니다.");
    }

    @DisplayName("닉네임 변경 실패 - 닉네임 규칙에 맞지 않는 경우")
    @Test
    void changeNicknameToInvalidRegexNickname() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        ModifyNicknameServiceRequest request = ModifyNicknameServiceRequest.builder()
                .nickname("11111111111111111111111111111111111")
                .username(member.getLoginCredentials().getUsername())
                .build();

        //when //then
        assertThatThrownBy(() -> settingService.changeNickname(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo(INVALID_NICKNAME);
    }

    @DisplayName("기존 비밀번호 검증 성공")
    @Test
    void verifyCurrentPassword() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        VerifyPasswordServiceRequest request = VerifyPasswordServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .password("password")
                .build();

        //when
        String response = settingService.verifyCurrentPassword(request);

        //then
        assertThat(response).isEqualTo("비밀번호 일치");
    }

    @DisplayName("기존 비밀번호 검증 실패 - 비밀번호가 일치하지 않는 경우")
    @Test
    void verifyCurrentPasswordFail() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        VerifyPasswordServiceRequest request = VerifyPasswordServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .password("111111")
                .build();

        //when //then
        assertThatThrownBy(() -> settingService.verifyCurrentPassword(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("비밀번호가 일치하지 않습니다.");
    }

    @DisplayName("기존 비밀번호 검증 실패 - 회원이 존재하지 않을 경우")
    @Test
    void verifyNotExistingMemberPassword() {
        //given
        VerifyPasswordServiceRequest request = VerifyPasswordServiceRequest.builder()
                .username("aaa")
                .password("password")
                .build();

        //when //then
        assertThatThrownBy(() -> settingService.verifyCurrentPassword(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("기존 비밀번호 검증 실패 - 탈퇴한 회원인 경우")
    @Test
    void verifyDeletedMemberPassword() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        VerifyPasswordServiceRequest request = VerifyPasswordServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .password("password")
                .build();

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> settingService.verifyCurrentPassword(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("비밀번호 변경 성공")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("memberRoles")
    void changePasswordSuccess(Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        ModifyPasswordServiceRequest request = ModifyPasswordServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .currentPassword("password")
                .newPassword("newPassword1234!")
                .newPasswordConfirm("newPassword1234!")
                .build();

        //when
        settingService.changePassword(request);

        //then
        Member findMember = memberRepository.findActiveMemberBy(member.getId()).get();
        assertThat(encoder.matches(request.getNewPassword(),
                findMember.getLoginCredentials().getPassword())).isTrue();
    }

    @DisplayName("비밀번호 변경 실패 - 회원이 존재하지 않을 경우")
    @Test
    void changePasswordOfNotExistingMember() {
        //given
        ModifyPasswordServiceRequest request = ModifyPasswordServiceRequest.builder()
                .username("aaa")
                .currentPassword("password")
                .newPassword("newPassword1234!")
                .newPasswordConfirm("newPassword1234!")
                .build();

        //when //then
        assertThatThrownBy(() -> settingService.changePassword(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("비밀번호 변경 실패 - 해당 회원이 탈퇴한 회원인 경우")
    @Test
    void changePasswordOfDeletedMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        ModifyPasswordServiceRequest request = ModifyPasswordServiceRequest.builder()
                .username("aaa")
                .currentPassword("password")
                .newPassword("newPassword1234!")
                .newPasswordConfirm("newPassword1234!")
                .build();

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> settingService.changePassword(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("비밀번호 변경 실패 - 입력한 기존 비밀번호가 틀린 경우")
    @Test
    void changePasswordOfProvidingInvalidCurrentPassword() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        ModifyPasswordServiceRequest request = ModifyPasswordServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .currentPassword("!!!!!!!!!!!!")
                .newPassword("newPassword1234!")
                .newPasswordConfirm("newPassword1234!")
                .build();

        //when //then
        assertThatThrownBy(() -> settingService.changePassword(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("비밀번호가 일치하지 않습니다.");
    }

    @DisplayName("비밀번호 변경 실패 - 새 비밀번호와 새 비밀번호 확인이 서로 일치하지 않을 경우")
    @Test
    void newPasswordAndNewPasswordConfirmNotMatch() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        ModifyPasswordServiceRequest request = ModifyPasswordServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .currentPassword("password")
                .newPassword("newPassword1234!")
                .newPasswordConfirm("1234566")
                .build();

        //when //then
        assertThatThrownBy(() -> settingService.changePassword(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.");
    }

    @DisplayName("탈퇴를 요청한 회원의 프로필 정보 조회 - 학생 회원인 경우")
    @Test
    void loadStudentMemberProfileToUnregister() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        //when
        MyProfileToUnregisterResponse response = settingService.loadProfileToUnregister(
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response).extracting("username", "realName", "companyName")
                .containsExactly(member.getLoginCredentials().getUsername(),
                        member.getStudentCredentials().getRealName(), null);
    }

    @DisplayName("탈퇴를 요청한 회원의 프로필 정보 조회 - 기업 회원인 경우")
    @Test
    void loadCompanyMemberProfileToUnregister() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedCompanyMember(university);

        //when
        MyProfileToUnregisterResponse response = settingService.loadProfileToUnregister(
                member.getLoginCredentials().getUsername());

        //then
        Member companyMember = memberRepository.findActiveMemberWithCompanyBy(
                member.getLoginCredentials().getUsername()).get();

        assertThat(response).extracting("username", "realName", "companyName")
                .containsExactly(companyMember.getLoginCredentials().getUsername(),
                        null, companyMember.getCompanyCredentials().getCompany().getName());
    }

    public static Stream<Arguments> notAllowedRolesToUnregister() {
        return Stream.of(
                Arguments.of(ADMIN),
                Arguments.of(STUDENT_COUNCIL)
        );
    }

    @DisplayName("탈퇴를 요청한 회원의 프로필 정보 조회 실패 - 관리자, 학생회 회원은 탈퇴할수 없다")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("notAllowedRolesToUnregister")
    void loadMemberProfileThatIsNotAllowedToUnregister(Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        //when //then
        assertThatThrownBy(() -> settingService.loadProfileToUnregister(
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("회원 탈퇴를 할수 없는 계정입니다");
    }

    @DisplayName("탈퇴를 요청한 회원의 프로필 정보 실패 - 회원이 존재하지 않을 경우")
    @Test
    void loadNotExistingMemberProfileToUnregister() {

        //when //then
        assertThatThrownBy(() -> settingService.loadProfileToUnregister("aaaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("탈퇴를 요청한 회원의 프로필 정보 실패 - 해당 회원이 이미 탈퇴를 한 경우")
    @Test
    void loadDeletedMemberProfileToUnregister() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> settingService.loadProfileToUnregister(
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("회원 탈퇴 성공 - 학생 회원")
    @Test
    void unregisterStudentMember() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        //when
        settingService.unregisterMember(member.getLoginCredentials().getUsername(), "password");

        //then
        Member deletedMember = memberRepository.findDeletedMemberBy(member.getId()).get();
        assertThat(deletedMember.getBasicCredentials().isDeleted()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @DisplayName("회원 탈퇴 성공 - 기업 회원")
    @Test
    void unregisterCompanyMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedCompanyMember(university);

        //when
        settingService.unregisterMember(member.getLoginCredentials().getUsername(), "password");

        //then
        Member deletedMember = memberRepository.findDeletedMemberBy(member.getId()).get();
        assertThat(deletedMember.getBasicCredentials().isDeleted()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @DisplayName("회원 탈퇴 실패 - 회원이 존재하지 않는 경우")
    @Test
    void unregisterNotExistingMember() {
        //when //then
        assertThatThrownBy(() -> settingService.unregisterMember("aaa", "password"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("회원 탈퇴 실패 - 해당 회원이 이미 탈퇴한 경우")
    @Test
    void unregisterDeletedMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> settingService.unregisterMember("aaa", "password"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("회원 탈퇴 실패 - 관리자 회원과 학생회 회원은 탈퇴를 할수 없다")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("notAllowedRolesToUnregister")
    void unregisterNotAllowedRoleTest(Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        //when //then
        assertThatThrownBy(
                () -> settingService.unregisterMember(member.getLoginCredentials().getUsername(),
                        "password"))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("회원 탈퇴를 할수 없는 계정입니다");
    }


    @DisplayName("프로필 사진 변경 실패 - 회원이 존재하지 않을 경우")
    @Test
    void changeNotExistingMemberProfileImage() {
        //given
        String fileName = "testImage.png";
        Resource resource = resourceLoader.getResource("classpath:/static/images/" + fileName);
        MockMultipartFile file = null;
        try {
            file = new MockMultipartFile(
                    "file",
                    "testImage.png",
                    IMAGE_PNG_VALUE,
                    resource.getInputStream()
            );
        } catch (IOException ignored) {}
        MockMultipartFile finalFile = file;

        //when //then
        assertThatThrownBy(() -> settingService.changeProfileImage("aaa", finalFile))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("프로필 사진 변경 실패 - 해당 회원이 탈퇴한 회원인 경우")
    @Test
    void changeDeletedMemberProfileImage() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        Resource resource = resourceLoader.getResource("classpath:/static/images/testImage.png");
        MockMultipartFile file = null;
        try {
            file = new MockMultipartFile(
                    "file",
                    "testImage.png",
                    IMAGE_PNG_VALUE,
                    resource.getInputStream()
            );
        } catch (IOException ignored) {}
        MockMultipartFile finalFile = file;

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> settingService.changeProfileImage("aaa", finalFile))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("프로필 사진 변경 실패 - 업로드한 파일이 비어있는 경우")
    @Test
    void changeProfileImageToEmptyFile() throws Exception{
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        MockMultipartFile file = new MockMultipartFile(
                "file", "testImage.png",
                IMAGE_PNG_VALUE, nullInputStream());

        //when //then
        assertThatThrownBy(() ->
                settingService.changeProfileImage(member.getLoginCredentials().getUsername(), file))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("업로드할 파일이 비어있습니다");
    }

    @DisplayName("프로필 사진 변경 실패 - 업로드한 파일의 이름이 없는경우")
    @Test
    void changeProfileImageToFileThatHasNoName() throws Exception{
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        String fileName = "testImage.png";
        Resource resource = resourceLoader.getResource("classpath:/static/images/" + fileName);
        MockMultipartFile file = new MockMultipartFile("file", null,
                IMAGE_PNG_VALUE, resource.getInputStream()
        );

        //when //then
        assertThatThrownBy(() ->
                settingService.changeProfileImage(member.getLoginCredentials().getUsername(), file))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("업로드할 파일이 비어있습니다");
    }
}
