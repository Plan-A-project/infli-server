package com.plana.infli.service.mock;

import static com.plana.infli.domain.embedded.member.BasicCredentials.ofDefaultWithNickname;
import static com.plana.infli.domain.embedded.member.ProfileImage.ofDefaultProfileImage;
import static com.plana.infli.domain.type.BoardType.*;
import static com.plana.infli.domain.type.PostType.*;
import static com.plana.infli.domain.type.Role.STUDENT;
import static com.plana.infli.domain.type.VerificationStatus.SUCCESS;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.LoginCredentials;
import com.plana.infli.domain.embedded.member.StudentCredentials;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.service.PostService;
import com.plana.infli.service.S3Uploader;
import com.plana.infli.web.dto.response.post.image.PostImageUploadResponse;
import java.util.List;
import java.util.Optional;
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
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class PostServiceMockTest {

    @Mock
    S3Uploader s3Uploader;

    @InjectMocks
    PostService postService;

    @Spy
    PasswordEncoder encoder;

    @Mock
    MemberRepository memberRepository;

    @Mock
    PostRepository postRepository;

    @DisplayName("특정 글에 사진 업로드 성공")
    @Test
    void test() {
        //given
        University university = createUniversity();
        Member member = createStudentMember(university);
        Board board = createBoard(university);
        Post post = createPost(board, member);

        MockMultipartFile file = new MockMultipartFile("file", new byte[]{1, 2, 3, 4, 5});

        given(memberRepository.findActiveMemberBy(anyString())).willReturn(Optional.of(member));

        given(postRepository.findActivePostWithMemberBy(any())).willReturn(Optional.of(post));

        given(s3Uploader.uploadAsOriginalImage(any(MultipartFile.class), anyString()))
                .willReturn("aaa.com");

        given(s3Uploader.uploadAsThumbnailImage(any(MultipartFile.class), anyString()))
                .willReturn("bbb.com");

        //when
        PostImageUploadResponse response = postService.uploadPostImages(post.getId(),
                List.of(file),
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response.getThumbnailImageUrl()).isEqualTo("bbb.com");
        assertThat(response.getOriginalImageUrls()).size().isEqualTo(1);
        assertThat(response.getOriginalImageUrls().get(0)).isEqualTo("aaa.com");
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

    University createUniversity() {
        return University.create("푸단대학교");
    }

    Post createPost(Board board, Member member) {
        return Post.builder()
                .board(board)
                .postType(NORMAL)
                .title("제목입니다")
                .content("내용입니다")
                .member(member)
                .recruitment(null)
                .build();
    }

    Board createBoard(University university) {
        return Board.create(ANONYMOUS, university);
    }
}
