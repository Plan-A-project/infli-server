package com.plana.infli.service.mock;

import static com.plana.infli.domain.embedded.member.BasicCredentials.ofDefaultWithNickname;
import static com.plana.infli.domain.embedded.member.ProfileImage.ofDefaultProfileImage;
import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.domain.type.VerificationStatus.*;
import static java.util.Optional.*;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.LoginCredentials;
import com.plana.infli.domain.embedded.member.StudentCredentials;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.SettingService;
import com.plana.infli.service.S3Uploader;
import com.plana.infli.web.dto.response.profile.image.ChangeProfileImageResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SettingServiceMockTest {

    @Spy
    private MemberRepository memberRepository;

    @Spy
    private UniversityRepository universityRepository;

    @Spy
    private PasswordEncoder encoder;

    @Mock
    private S3Uploader s3Uploader;

    @InjectMocks
    private SettingService settingService;

    @DisplayName("회원 프로필 사진 변경 성공")
    @Test
    void changeMemberProfile() {
        //given
        University university = universityRepository.save(University.create("푸단대학교"));
        Member member = createStudentMember(university);

        given(memberRepository.findActiveMemberBy(anyString())).willReturn(ofNullable(member));

        given(s3Uploader.uploadAsOriginalImage(any(MultipartFile.class), anyString()))
                .willReturn("aaa.com");

        given(s3Uploader.uploadAsThumbnailImage(any(MultipartFile.class), anyString()))
                .willReturn("bbb.com");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testImage.png",
                IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3, 4}
        );

        //when
        ChangeProfileImageResponse response = settingService.changeProfileImage("username", file);

        //then
        assertThat(response.getThumbnailUrl()).isEqualTo("bbb.com");
        assertThat(response.getOriginalUrl()).isEqualTo("aaa.com");
    }

    Member createStudentMember(University university) {

        return Member.builder()
                .university(university)
                .role(STUDENT)
                .verificationStatus(SUCCESS)
                .loginCredentials(LoginCredentials.of(randomUUID().toString(),
                        encoder.encode("password")))
                .profileImage(ofDefaultProfileImage())
                .basicCredentials(ofDefaultWithNickname(UUID.randomUUID().toString()))
                .companyCredentials(null)
                .studentCredentials(StudentCredentials.ofDefault("이영진"))
                .build();
    }
}
