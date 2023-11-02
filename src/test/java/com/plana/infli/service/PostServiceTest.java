package com.plana.infli.service;

import static com.plana.infli.domain.type.BoardType.*;
import static com.plana.infli.domain.type.PostType.*;
import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.web.dto.request.post.view.PostQueryRequest.PostViewOrder.*;
import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.of;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.PostLike;
import com.plana.infli.domain.type.BoardType;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.type.Role;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.type.PostType;
import com.plana.infli.domain.University;
import com.plana.infli.factory.PostLikeFactory;
import com.plana.infli.infra.exception.custom.AuthorizationFailedException;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.factory.BoardFactory;
import com.plana.infli.factory.CommentFactory;
import com.plana.infli.factory.CommentLikeFactory;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.PostFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.commentlike.CommentLikeRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.postlike.PostLikeRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.post.create.normal.CreateNormalPostServiceRequest;
import com.plana.infli.web.dto.request.post.create.recruitment.CreateRecruitmentPostServiceRequest;
import com.plana.infli.web.dto.request.post.edit.normal.EditNormalPostServiceRequest;
import com.plana.infli.web.dto.request.post.edit.recruitment.EditRecruitmentPostServiceRequest;
import com.plana.infli.web.dto.request.post.view.board.LoadPostsByBoardServiceRequest;
import com.plana.infli.web.dto.request.post.view.search.SearchPostsByKeywordServiceRequest;
import com.plana.infli.web.dto.response.post.board.BoardPostsResponse;
import com.plana.infli.web.dto.response.post.my.MyPostsResponse;
import com.plana.infli.web.dto.response.post.search.SearchedPostsResponse;
import com.plana.infli.web.dto.response.post.single.SinglePostResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.parameters.P;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class PostServiceTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private CommentFactory commentFactory;

    @Autowired
    private PostFactory postFactory;

    @Autowired
    private BoardFactory boardFactory;

    @Autowired
    private CommentLikeFactory commentLikeFactory;

    @Autowired
    private PostService postService;

    @Autowired
    private PostLikeFactory postLikeFactory;

    @AfterEach
    void tearDown() {
        commentLikeRepository.deleteAllInBatch();
        commentRepository.deleteAllInBatch();
        postLikeRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }

    /**
     * 모집글 작성
     */
    public static Stream<Arguments> allowedRoleToWriteRecruitmentPost() {

        return Stream.of(
                Arguments.of(STUDENT, ACTIVITY),
                Arguments.of(ADMIN, ACTIVITY),
                Arguments.of(COMPANY, ACTIVITY),

                Arguments.of(COMPANY, EMPLOYMENT),
                Arguments.of(ADMIN, EMPLOYMENT)
        );
    }


    @DisplayName("모집 글 작성 성공 - 회원 유형, 게시판 유형에 따른 케이스 분류")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("allowedRoleToWriteRecruitmentPost")
    void SUCCESS_writeRecruitmentPost(Role role, BoardType boardType) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 8, 2, 0, 0))
                .build();

        //when
        Long postId = postService.createRecruitmentPost(request);

        //then
        assertThat(postRepository.count()).isEqualTo(1);
        Post post = postRepository.findActivePostWithBoardAndMemberBy(postId).get();
        assertThat(post.getMember().getId()).isEqualTo(member.getId());
        assertThat(post.getTitle()).isEqualTo("제목입니다");
        assertThat(post.getContent()).isEqualTo("내용입니다");
        assertThat(post.getBoard().getId()).isEqualTo(board.getId());
        assertThat(post.getBoard().getBoardType()).isEqualTo(boardType);
        assertThat(post.getRecruitment().getCompanyName()).isEqualTo("카카오");
        assertThat(post.getRecruitment().getStartDate()).isEqualTo(of(2023, 8, 1, 0, 0));
        assertThat(post.getRecruitment().getEndDate()).isEqualTo(of(2023, 8, 2, 0, 0));
        assertThat(post.getPostType()).isEqualTo(RECRUITMENT);
    }


    public static Stream<Arguments> notAllowedRoleToWriteRecruitmentPost() {

        return Stream.of(
                Arguments.of(STUDENT_COUNCIL, ACTIVITY),

                Arguments.of(STUDENT, EMPLOYMENT),
                Arguments.of(STUDENT_COUNCIL, EMPLOYMENT)
        );
    }

    @DisplayName("모집 글 작성 실패 - 해당 게시판에 모집글 작성 권한이 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("notAllowedRoleToWriteRecruitmentPost")
    void FAIL_writeRecruitmentPost(Role role, BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(now())
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createRecruitmentPost(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }

    public static Stream<Arguments> invalidBoardTypeToWriteRecruitmentPost() {

        return Stream.of(
                Arguments.of(CLUB, STUDENT),
                Arguments.of(CLUB, COMPANY),
                Arguments.of(CLUB, STUDENT_COUNCIL),
                Arguments.of(CLUB, ADMIN),

                Arguments.of(ANONYMOUS, STUDENT),
                Arguments.of(ANONYMOUS, COMPANY),
                Arguments.of(ANONYMOUS, STUDENT_COUNCIL),
                Arguments.of(ANONYMOUS, ADMIN),

                Arguments.of(CAMPUS_LIFE, STUDENT),
                Arguments.of(CAMPUS_LIFE, COMPANY),
                Arguments.of(CAMPUS_LIFE, STUDENT_COUNCIL),
                Arguments.of(CAMPUS_LIFE, ADMIN)
        );
    }

    @DisplayName("모집 글 작성 실패 - 모집글을 작성할수 없는 게시판인 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("invalidBoardTypeToWriteRecruitmentPost")
    void FAIL_invalidBoardTypeToWriteRecruitmentPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(now())
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createRecruitmentPost(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("채용글을 작성할수 있는 게시판이 아닙니다");
    }


    public static Stream<Arguments> allowedRoleToWriteNormalPost() {

        return Stream.of(
                Arguments.of(STUDENT, ACTIVITY),
                Arguments.of(ADMIN, ACTIVITY),
                Arguments.of(COMPANY, ACTIVITY),

                Arguments.of(STUDENT, EMPLOYMENT),
                Arguments.of(ADMIN, EMPLOYMENT),
                Arguments.of(COMPANY, EMPLOYMENT),

                Arguments.of(STUDENT, ANONYMOUS),
                Arguments.of(ADMIN, ANONYMOUS),

                Arguments.of(STUDENT, CLUB),
                Arguments.of(ADMIN, CLUB),

                Arguments.of(STUDENT, CAMPUS_LIFE),
                Arguments.of(ADMIN, CAMPUS_LIFE),
                Arguments.of(STUDENT_COUNCIL, CAMPUS_LIFE)
        );
    }

    @DisplayName("일반 글 작성 성공 - 게시판 유형, 회원 유형에 따른 케이스 분류")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("allowedRoleToWriteNormalPost")
    void SUCCESS_writeNormalPost(Role role, BoardType boardType) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(NORMAL)
                .build();

        //when
        Long postId = postService.createNormalPost(request);

        //then
        assertThat(postRepository.count()).isEqualTo(1);
        Post post = postRepository.findActivePostWithBoardAndMemberBy(postId).get();
        assertThat(post.getMember().getId()).isEqualTo(member.getId());
        assertThat(post.getTitle()).isEqualTo("제목입니다");
        assertThat(post.getContent()).isEqualTo("내용입니다");
        assertThat(post.getBoard().getId()).isEqualTo(board.getId());
        assertThat(post.getBoard().getBoardType()).isEqualTo(boardType);
        assertThat(post.getPostType()).isEqualTo(NORMAL);
    }

    public static Stream<Arguments> notAllowedRoleToWriteNormalPost() {

        return Stream.of(
                Arguments.of(STUDENT_COUNCIL, ANONYMOUS),
                Arguments.of(COMPANY, ANONYMOUS),

                Arguments.of(STUDENT_COUNCIL, ACTIVITY),

                Arguments.of(STUDENT_COUNCIL, EMPLOYMENT),

                Arguments.of(STUDENT_COUNCIL, CLUB),
                Arguments.of(COMPANY, CLUB),

                Arguments.of(COMPANY, CAMPUS_LIFE)
        );
    }

    @DisplayName("일반 글 작성 실패 - 해당 게시판에 일반글 작성 권한이 없는경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("notAllowedRoleToWriteNormalPost")
    void FAIL_writeNormalPost(Role role, BoardType boardType) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(NORMAL)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createNormalPost(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }


    public static Stream<Arguments> allowedRoleToWriteAnnouncementPost() {

        return Stream.of(
                Arguments.of(STUDENT_COUNCIL),
                Arguments.of(ADMIN)
        );
    }

    @DisplayName("공지 글 작성 성공 - 게시판 유형, 회원 유형에 따른 케이스 분류")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("allowedRoleToWriteAnnouncementPost")
    void SUCCESS_writeAnnouncementPost(Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, CAMPUS_LIFE);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(ANNOUNCEMENT)
                .build();

        //when
        Long postId = postService.createNormalPost(request);

        //then
        assertThat(postRepository.count()).isEqualTo(1);
        Post post = postRepository.findActivePostWithBoardAndMemberBy(postId).get();
        assertThat(post.getMember().getId()).isEqualTo(member.getId());
        assertThat(post.getTitle()).isEqualTo("제목입니다");
        assertThat(post.getContent()).isEqualTo("내용입니다");
        assertThat(post.getBoard().getId()).isEqualTo(board.getId());
        assertThat(post.getBoard().getBoardType()).isEqualTo(CAMPUS_LIFE);
        assertThat(post.getPostType()).isEqualTo(ANNOUNCEMENT);
    }

    public static Stream<Arguments> notAllowedRoleToWriteAnnouncementPost() {

        return Stream.of(
                Arguments.of(STUDENT),
                Arguments.of(COMPANY)
        );
    }

    @DisplayName("일반 공지글 작성 실패 - 공지글 작성 권한이 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("notAllowedRoleToWriteAnnouncementPost")
    void FAIL_writeAnnouncementPost(Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, CAMPUS_LIFE);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(ANNOUNCEMENT)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createNormalPost(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }

    public static Stream<Arguments> invalidBoardTypeToWriteAnnouncementPost() {

        return Stream.of(
                Arguments.of(EMPLOYMENT, STUDENT),
                Arguments.of(EMPLOYMENT, COMPANY),
                Arguments.of(EMPLOYMENT, ADMIN),
                Arguments.of(EMPLOYMENT, STUDENT_COUNCIL),

                Arguments.of(ACTIVITY, STUDENT),
                Arguments.of(ACTIVITY, COMPANY),
                Arguments.of(ACTIVITY, ADMIN),
                Arguments.of(ACTIVITY, STUDENT_COUNCIL),

                Arguments.of(CLUB, STUDENT),
                Arguments.of(CLUB, COMPANY),
                Arguments.of(CLUB, ADMIN),
                Arguments.of(CLUB, STUDENT_COUNCIL),

                Arguments.of(ANONYMOUS, STUDENT),
                Arguments.of(ANONYMOUS, COMPANY),
                Arguments.of(ANONYMOUS, ADMIN),
                Arguments.of(ANONYMOUS, STUDENT_COUNCIL)
        );
    }

    @DisplayName("일반 공지글 작성 실패 - 공지글을 작성할수 있는 게시판이 아닌 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("invalidBoardTypeToWriteAnnouncementPost")
    void FAIL_invalidBoardTypeToWriteAnnouncementPost(BoardType boardType, Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(ANNOUNCEMENT)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createNormalPost(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("게시판 정보가 옳바르지 않습니다");
    }


    @DisplayName("일반 글 작성 실패 - 일반글을 작성하는 API에 모집글 작성을 요청한 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}")
    @MethodSource("allowedRoleToWriteNormalPost")
    void FAIL_writeNormalPostWithRecruitmentType(Role role, BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(RECRUITMENT)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createNormalPost(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("해당 글 종류는 허용되지 않습니다");
    }


    @DisplayName("일반글 작성 실패 - 존재하지 않는 게시판에 글을 작성할수 없다")
    @Test
    void writeNormalPostInNotExistingBoard() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createAdminMember(university);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(999L)
                .title("제목입니다")
                .content("내용입니다")
                .postType(NORMAL)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createNormalPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시판을 찾을수 없습니다");
    }


    @DisplayName("일반글 작성 실패 - 존재하지 않는 회원의 계정으로 글을 작성할수 없다")
    @Test
    void writeNormalPostByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createAdminMember(university);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username("aaa")
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(NORMAL)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createNormalPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("일반 공지글 작성 실패 - 존재하지 않는 게시판에 글을 작성할수 없다")
    @Test
    void writeAnnouncementPostInNotExistingBoard() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createAdminMember(university);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(999L)
                .title("제목입니다")
                .content("내용입니다")
                .postType(ANNOUNCEMENT)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createNormalPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시판을 찾을수 없습니다");
    }

    @DisplayName("일반글 작성 실패 - 글 작성 규정을 동의하지 않고 글을 작성할수 없다")
    @Test
    void writeNormalPostWithoutAgreeingOnWritePolicy() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createAdminMember(university);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(999L)
                .title("제목입니다")
                .content("내용입니다")
                .postType(ANNOUNCEMENT)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createNormalPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시판을 찾을수 없습니다");
    }

    @DisplayName("일반 공지글 작성 실패 - 존재하지 않는 회원의 계정으로 글을 작성할수 없다")
    @Test
    void writeAnnouncementPostByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username("aaa")
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(NORMAL)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createNormalPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("일반글 작성 실패 - 학생 인증을 받지 못한 학생 회원은 글을 작성할수 없다")
    @Test
    void writePostWithoutStudentVerification() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member unverifiedMember = memberFactory.createUnverifiedStudentMember("nickname", university);
        Board board = boardFactory.createCampusLifeBoard(university);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(unverifiedMember.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(NORMAL)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createNormalPost(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }

    @DisplayName("모집글 작성 실패 - 기업 인증을 받지 못한 기업 회원은 글을 작성할수 없다")
    @Test
    void writeRecruitmentPostWithoutCompanyVerification() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member unverifiedCompanyMember = memberFactory.createUnverifiedCompanyMember(university);
        Board board = boardFactory.createEmploymentBoard(university);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .username(unverifiedCompanyMember.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(now())
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createRecruitmentPost(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }


    @DisplayName("모집글 작성 실패 - 존재하지 않는 게시판에 글을 작성할수 없다")
    @Test
    void writeRecruitmentPostInNotExistingBoard() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createAdminMember(university);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(999L)
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(now())
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createRecruitmentPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시판을 찾을수 없습니다");
    }

    @DisplayName("모집글 작성 실패 - 존재하지 않는 회원의 계정으로 글을 작성할수 없다")
    @Test
    void writeRecruitmentPostByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createEmploymentBoard(university);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .username("aaa")
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(now())
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createRecruitmentPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("모집글 작성 실패 - 모집 종료일이 모집 시작일보다 빠를수 없다")
    @Test
    void writeRecruitmentPostByInvalidRecruitmentDate() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = memberFactory.createVerifiedCompanyMember(university);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2022, 8, 1, 0, 0))
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createRecruitmentPost(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("모집 종료일이 시작일보다 빠를수 없습니다");
    }

    @DisplayName("모집글 작성 실패 - 글 작성 규정 동의를 하지 않은 상태로 글 작성을 할수 없다")
    @Test
    void createRecruitmentPostWithoutAgreeingOnWritePolicy() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = memberFactory.createPolicyNotAcceptedMemberWithRole(
                university, COMPANY);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build();

        //when //then
        assertThatThrownBy(() -> postService.createRecruitmentPost(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("글 작성 규칙 동의를 먼저 진행해주세요");
    }

    public static Stream<Arguments> allowedCombinationToWritePost() {

        return Stream.of(
                Arguments.of(EMPLOYMENT, NORMAL, STUDENT),
                Arguments.of(EMPLOYMENT, NORMAL, COMPANY),
                Arguments.of(EMPLOYMENT, NORMAL, ADMIN),
                Arguments.of(EMPLOYMENT, RECRUITMENT, COMPANY),
                Arguments.of(EMPLOYMENT, RECRUITMENT, ADMIN),

                Arguments.of(ACTIVITY, NORMAL, STUDENT),
                Arguments.of(ACTIVITY, NORMAL, COMPANY),
                Arguments.of(ACTIVITY, NORMAL, ADMIN),
                Arguments.of(ACTIVITY, RECRUITMENT, STUDENT),
                Arguments.of(ACTIVITY, RECRUITMENT, COMPANY),
                Arguments.of(ACTIVITY, RECRUITMENT, ADMIN),

                Arguments.of(CLUB, NORMAL, STUDENT),
                Arguments.of(CLUB, NORMAL, ADMIN),
                Arguments.of(CLUB, NORMAL, STUDENT),

                Arguments.of(ANONYMOUS, NORMAL, STUDENT),
                Arguments.of(ANONYMOUS, NORMAL, ADMIN),

                Arguments.of(CAMPUS_LIFE, NORMAL, STUDENT),
                Arguments.of(CAMPUS_LIFE, NORMAL, ADMIN),
                Arguments.of(CAMPUS_LIFE, ANNOUNCEMENT, STUDENT_COUNCIL),
                Arguments.of(CAMPUS_LIFE, ANNOUNCEMENT, ADMIN)
        );
    }

    @DisplayName("해당 게시판에 글 작성 권한 있는지 여부 확인 - 권한 있는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형: {1}, 회원 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void SUCCESS_checkMemberHasWritePolicy(BoardType boardType, PostType postType, Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        //when //then
        assertThat(postService.checkMemberHasWritePermission(board.getId(),
                member.getLoginCredentials().getUsername(), postType)).isTrue();
    }


    public static Stream<Arguments> notAllowedRoleToWritePost() {

        return Stream.of(
                Arguments.of(EMPLOYMENT, NORMAL, STUDENT_COUNCIL),
                Arguments.of(EMPLOYMENT, RECRUITMENT, STUDENT),
                Arguments.of(EMPLOYMENT, RECRUITMENT, STUDENT_COUNCIL),

                Arguments.of(ACTIVITY, NORMAL, STUDENT_COUNCIL),
                Arguments.of(ACTIVITY, RECRUITMENT, STUDENT_COUNCIL),

                Arguments.of(CLUB, NORMAL, STUDENT_COUNCIL),
                Arguments.of(CLUB, NORMAL, COMPANY),

                Arguments.of(ANONYMOUS, NORMAL, STUDENT_COUNCIL),
                Arguments.of(ANONYMOUS, NORMAL, COMPANY),

                Arguments.of(CAMPUS_LIFE, NORMAL, COMPANY),
                Arguments.of(CAMPUS_LIFE, ANNOUNCEMENT, STUDENT),
                Arguments.of(CAMPUS_LIFE, ANNOUNCEMENT, COMPANY)
        );
    }

    @DisplayName("해당 게시판에 글 작성 권한 있는지 여부 확인 - 권한 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("notAllowedRoleToWritePost")
    void Fail_checkMemberHasWritePolicy(BoardType boardType, PostType postType, Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        //when //then
        assertThatThrownBy(() -> postService.checkMemberHasWritePermission(board.getId(),
                member.getLoginCredentials().getUsername(), postType))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }


    /**
     * 글 수정
     */

    public static Stream<Arguments> possibleCombinationToWriteNonRecruitmentPost() {

        return Stream.of(
                Arguments.of(ACTIVITY, STUDENT),
                Arguments.of(ACTIVITY, ADMIN),
                Arguments.of(ACTIVITY, COMPANY),

                Arguments.of(EMPLOYMENT, COMPANY),
                Arguments.of(EMPLOYMENT, ADMIN),
                Arguments.of(EMPLOYMENT, STUDENT),

                Arguments.of(ANONYMOUS, STUDENT),
                Arguments.of(ANONYMOUS, ADMIN),

                Arguments.of(CLUB, STUDENT),
                Arguments.of(CLUB, ADMIN),

                Arguments.of(CAMPUS_LIFE, STUDENT),
                Arguments.of(CAMPUS_LIFE, STUDENT_COUNCIL),
                Arguments.of(CAMPUS_LIFE, ADMIN)
        );
    }

    @DisplayName("일반글 수정 성공")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("possibleCombinationToWriteNonRecruitmentPost")
    void Success_EditNormalPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        Post post = postFactory.createNormalPost(member, board);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .build();

        //when
        postService.editNormalPost(request);

        //then
        Post findPost = postRepository.findActivePostBy(post.getId()).get();
        assertThat(findPost.getTitle()).isEqualTo("수정된 제목입니다");
        assertThat(findPost.getContent()).isEqualTo("수정된 내용입니다");
        assertThat(findPost.getThumbnailUrl()).isEqualTo("aaa.com");
    }

    @DisplayName("일반글 수정 실패 - 수정을 요청한 회원을 찾을수 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("possibleCombinationToWriteNonRecruitmentPost")
    void Fail_EditNormalPostByNotExistingMember(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createNormalPost(member, board);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username("aaa")
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .build();

        //when //then
        assertThatThrownBy(() -> postService.editNormalPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("일반글 수정 실패 - 글 작성자가 회원 탈퇴를 한 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("possibleCombinationToWriteNonRecruitmentPost")
    void Fail_EditNormalPostByDeletedMember(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createNormalPost(member, board);

        memberRepository.delete(member);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .build();

        //when //then
        assertThatThrownBy(() -> postService.editNormalPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("일반글 수정 실패 - 수정할 글이 존재하지 않는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("possibleCombinationToWriteNonRecruitmentPost")
    void Fail_EditNotExistingNormalPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .postId(999L)
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .build();

        //when //then
        assertThatThrownBy(() -> postService.editNormalPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("일반글 수정 실패 - 수정할 글이 이미 삭제된 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("possibleCombinationToWriteNonRecruitmentPost")
    void Fail_EditDeletedNormalPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createNormalPost(member, board);

        postService.deletePost(post.getId(), member.getLoginCredentials().getUsername());

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .build();

        //when //then
        assertThatThrownBy(() -> postService.editNormalPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("일반글 수정 실패 - 일반글 수정이 아니라 모집글 수정을 요청한 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("allowedRoleToWriteRecruitmentPost")
    void Fail_InvalidRequestToEditRecruitmentPost(Role role, BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createRecruitmentPost(member, board);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .build();

        //when //then
        assertThatThrownBy(() -> postService.editNormalPost(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("해당 글 종류는 허용되지 않습니다");
    }

    @DisplayName("일반글 수정 실패 - 수정할 글이 내가 작성한 글이 아닌 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("possibleCombinationToWriteNonRecruitmentPost")
    void Fail_EditNormalPostWroteByOtherMember(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .build();

        //when //then
        assertThatThrownBy(() -> postService.editNormalPost(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }


    @DisplayName("모집글 수정 성공")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("allowedRoleToWriteRecruitmentPost")
    void Success_EditRecruitPost(Role role, BoardType boardType) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        Post post = postFactory.createRecruitmentPost(member, board);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 8, 5, 0, 0))
                .build();

        //when
        postService.editRecruitmentPost(request);

        //then
        Post findPost = postRepository.findActivePostBy(post.getId()).get();
        assertThat(findPost.getTitle()).isEqualTo("수정된 제목입니다");
        assertThat(findPost.getContent()).isEqualTo("수정된 내용입니다");
        assertThat(findPost.getThumbnailUrl()).isEqualTo("aaa.com");
        assertThat(findPost.getRecruitment().getCompanyName()).isEqualTo("삼성전자");
        assertThat(findPost.getRecruitment().getStartDate()).isEqualTo(of(2023, 8, 1, 0, 0));
        assertThat(findPost.getRecruitment().getEndDate()).isEqualTo(of(2023, 8, 5, 0, 0));
    }

    @DisplayName("모집글 수정 실패 - 수정을 요청한 회원을 찾을수 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("allowedRoleToWriteRecruitmentPost")
    void Fail_EditRecruitmentPostByNotExistingMember(Role role, BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createRecruitmentPost(member, board);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .username("unknown")
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 8, 5, 0, 0))
                .build();

        //when //then
        assertThatThrownBy(() -> postService.editRecruitmentPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("모집글 수정 실패 - 글 작성자가 회원 탈퇴를 한 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("allowedRoleToWriteRecruitmentPost")
    void Fail_EditRecruitmentPostByDeletedMember(Role role, BoardType boardType) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createRecruitmentPost(member, board);

        memberRepository.delete(member);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .username("unknown")
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 8, 5, 0, 0))
                .build();

        //when //then
        assertThatThrownBy(() -> postService.editRecruitmentPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("모집글 수정 실패 - 수정할 글이 존재하지 않는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("allowedRoleToWriteRecruitmentPost")
    void Fail_EditNotExistingRecruitmentPost(Role role, BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .postId(-11L)
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 8, 5, 0, 0))
                .build();

        //when //then
        assertThatThrownBy(() -> postService.editRecruitmentPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("모집글 수정 실패 - 수정할 글이 이미 삭제된 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("allowedRoleToWriteRecruitmentPost")
    void Fail_EditDeletedRecruitmentPost(Role role, BoardType boardType) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createRecruitmentPost(member, board);

        postService.deletePost(post.getId(), member.getLoginCredentials().getUsername());

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 8, 5, 0, 0))
                .build();

        //when //then
        assertThatThrownBy(() -> postService.editRecruitmentPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("모집글 수정 실패 - 모집글 수정이 아니라 일반글 수정을 요청한 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("possibleCombinationToWriteNonRecruitmentPost")
    void Fail_InvalidRequestToEditNormalPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createNormalPost(member, board);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 8, 5, 0, 0))
                .build();

        //when //then
        assertThatThrownBy(() -> postService.editRecruitmentPost(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("해당 글 종류는 허용되지 않습니다");
    }

    @DisplayName("모집글 수정 실패 - 수정할 글이 내가 작성한 글이 아닌 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("allowedRoleToWriteRecruitmentPost")
    void Fail_EditRecruitmentPostWroteByOtherMember(Role role, BoardType boardType) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createRecruitmentPost(postMember, board);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("aaa.com")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 8, 5, 0, 0))
                .build();

        //when //then
        assertThatThrownBy(() -> postService.editRecruitmentPost(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }

    @DisplayName("글 삭제 성공")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void Success_DeletePost(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(member, board, postType);

        //when
        postService.deletePost(post.getId(), member.getLoginCredentials().getUsername());

        //then
        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(findPost.isDeleted()).isTrue();
    }

    @DisplayName("글 삭제 성공 - 관리자는 타인이 작성한 글도 삭제할수 있다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void Success_DeletePostByAdmin(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(member, board, postType);

        Member admin = memberFactory.createAdminMember(university);

        //when
        postService.deletePost(post.getId(), admin.getLoginCredentials().getUsername());

        //then
        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(findPost.isDeleted()).isTrue();
    }

    @DisplayName("글 삭제 실패 - 글 작성자를 찾을수 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void Fail_DeletePostByNotExistingMember(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(member, board, postType);

        //when //then
        assertThatThrownBy(() -> postService.deletePost(post.getId(), "aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("글 삭제 실패 - 글 작성자가 회원 탈퇴를 한 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void Fail_DeletePostByDeletedMember(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(member, board, postType);

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> postService.deletePost(post.getId(),
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }


    public static Stream<Arguments> existingMemberRole() {

        return Stream.of(
                Arguments.of(STUDENT),
                Arguments.of(STUDENT_COUNCIL),
                Arguments.of(ADMIN),
                Arguments.of(COMPANY)
        );
    }


    @DisplayName("글 삭제 실패 - 존재하지 않는글을 삭제할수 없다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("existingMemberRole")
    void Fail_DeleteNotExistingPost(Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        //when //then
        assertThatThrownBy(
                () -> postService.deletePost(-10L, member.getLoginCredentials().getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("글 삭제 실패 - 이미 삭제된 글을 삭제 시도 할수 없다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void Fail_DeletePostThatIsAlreadyDeleted(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(member, board, postType);

        postService.deletePost(post.getId(), member.getLoginCredentials().getUsername());

        //when //then
        assertThatThrownBy(() -> postService.deletePost(post.getId(),
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("글 삭제 실패 - 내가 작성하지 않은글을 삭제할수 없다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void Fail_DeletePostThatIsNotMine(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(postMember, board, postType);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when //then
        assertThatThrownBy(() -> postService.deletePost(post.getId(),
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }

    @DisplayName("모집 글 단건 조회")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 작성 회원 유형 : {1}")
    @MethodSource("allowedRoleToWriteRecruitmentPost")
    void viewSingleRecruitmentPost(Role role, BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        Post post = postFactory.createPost(postMember, board, RECRUITMENT);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response).extracting("postId", "title", "commentCount",
                        "likeCount", "viewCount", "thumbnailUrl", "boardName", "boardId",
                        "postType", "content")
                .containsExactly(post.getId(), post.getTitle(), post.getCommentMemberCount(), 0, 1,
                        post.getThumbnailUrl(), board.getBoardName(), board.getId(),
                        post.getPostType().toString(), post.getContent());

        assertThat(response.getRecruitment()).extracting("companyName", "startDate", "endDate")
                .containsExactly(post.getRecruitment().getCompanyName(),
                        post.getRecruitment().getStartDate(), post.getRecruitment().getEndDate());
    }

    public static Stream<Arguments> boardThatCompanyMemberCanWriteRecruitmentPost() {
        return Stream.of(
                Arguments.of(ACTIVITY),
                Arguments.of(EMPLOYMENT)
        );
    }


    @DisplayName("모집 글 단건 조회 - 기업 회원이 모집글을 작성한 경우, 글 작성자 이름의 값은 해당 기업 회원의 회사 이름 이다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}")
    @MethodSource("boardThatCompanyMemberCanWriteRecruitmentPost")
    void viewRecruitmentPostWriterColumByCompanyMember(BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member postMember = memberFactory.createVerifiedCompanyMember(university);

        Post post = postFactory.createPost(postMember, board, RECRUITMENT);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response.getNickname()).isEqualTo(
                postMember.getBasicCredentials().getNickname());
    }

    public static Stream<Arguments> possibleCombinationToWriteRecruitmentPostExceptCompanyMember() {

        return Stream.of(
                Arguments.of(ACTIVITY, STUDENT),
                Arguments.of(ACTIVITY, ADMIN),

                Arguments.of(EMPLOYMENT, ADMIN)
        );
    }


    @DisplayName("모집 글 단건 조회 - 학생 또는 관리자 회원이 모집글을 작성한 경우, 글 작성자 이름의 값은 해당 회원의 nickname 이다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 작성 회원 유형 : {1}")
    @MethodSource("possibleCombinationToWriteRecruitmentPostExceptCompanyMember")
    void viewRecruitmentPostWriterColumByNonCompanyMember(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        Post post = postFactory.createPost(postMember, board, RECRUITMENT);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response.getNickname()).isEqualTo(
                postMember.getBasicCredentials().getNickname());
    }


    public static Stream<Arguments> possibleRoleToWriteAnonymousPost() {
        return Stream.of(
                Arguments.of(STUDENT),
                Arguments.of(ADMIN)
        );
    }

    @DisplayName("익명 글 단건 조회")
    @ParameterizedTest(name = "{index} 글 작성 회원 유형 : {0}")
    @MethodSource("possibleRoleToWriteAnonymousPost")
    void viewSingleNormalPost(Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);

        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        Post post = postFactory.createPost(postMember, board, NORMAL);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response).extracting("postId", "title", "commentCount",
                        "likeCount", "viewCount", "thumbnailUrl", "boardName", "boardId",
                        "postType", "content")
                .containsExactly(post.getId(), post.getTitle(), post.getCommentMemberCount(), 0, 1,
                        post.getThumbnailUrl(), board.getBoardName(), board.getId(),
                        post.getPostType().toString(), post.getContent());
    }

    @DisplayName("익명 글 단건 조회 - 익명글 조회시 글 작성자 이름의 값은 null 이다")
    @ParameterizedTest(name = "{index} 글 작성 회원 유형 : {0}")
    @MethodSource("possibleRoleToWriteAnonymousPost")
    void writerColumOnAnonymousPostIsNull(Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);

        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        Post post = postFactory.createPost(postMember, board, NORMAL);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response.getNickname()).isNull();
    }

    public static Stream<Arguments> possibleNormalPostExceptAnonymousBoard() {

        return Stream.of(
                Arguments.of(ACTIVITY, STUDENT),
                Arguments.of(ACTIVITY, ADMIN),

                Arguments.of(EMPLOYMENT, ADMIN),
                Arguments.of(EMPLOYMENT, STUDENT),

                Arguments.of(CLUB, STUDENT),
                Arguments.of(CLUB, ADMIN),

                Arguments.of(CAMPUS_LIFE, STUDENT),
                Arguments.of(CAMPUS_LIFE, STUDENT_COUNCIL),
                Arguments.of(CAMPUS_LIFE, ADMIN)
        );
    }

    @DisplayName("익명이 아닌 일반 글 단건 조회")
    @ParameterizedTest(name = "{index} 글 작성 회원 유형 : {0}")
    @MethodSource("possibleNormalPostExceptAnonymousBoard")
    void viewNonAnonymousNormalPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        Post post = postFactory.createPost(postMember, board, NORMAL);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response).extracting("postId", "title", "commentCount",
                        "likeCount", "viewCount", "thumbnailUrl", "boardName", "boardId",
                        "postType", "nickname", "content")
                .containsExactly(post.getId(), post.getTitle(), post.getCommentMemberCount(), 0, 1,
                        post.getThumbnailUrl(), board.getBoardName(), board.getId(),
                        post.getPostType().toString(),
                        postMember.getBasicCredentials().getNickname(),
                        post.getContent());
    }

    @DisplayName("글 단건 조회 실패 - 존재하지 않는 회원이 조회를 요청한 경우")
    @Test
    void loadSinglePostByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("member", university);
        Post post = postFactory.createNormalPost(member, board);

        //when //then
        assertThatThrownBy(() -> postService.loadSinglePost(post.getId(), "AAAAA"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("글 단건 조회 실패 - 조회를 요청한 회원이 탈퇴한 회원인 경우")
    @Test
    void loadSinglePostByDeletedMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("member", university);
        Post post = postFactory.createNormalPost(member, board);

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(
                () -> postService.loadSinglePost(
                        post.getId(), member.getLoginCredentials().getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("글 단건 조회 실패 - 글이 존재하지 않는 경우")
    @Test
    void loadNotExistingSingle() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when //then
        assertThatThrownBy(
                () -> postService.loadSinglePost(-1L, member.getLoginCredentials().getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("글 단건 조회 실패 - 글이 삭제된 경우")
    @Test
    void loadDeletedSinglePost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("member", university);
        Post post = postFactory.createNormalPost(member, board);

        postService.deletePost(post.getId(), member.getLoginCredentials().getUsername());

        //when //then
        assertThatThrownBy(
                () -> postService.loadSinglePost(-1L, member.getLoginCredentials().getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("글 단건 조회 실패 - 해당 글이 내가 소속되지 않은 다른 대학교 게시판에 작성된 글이 경우")
    @Test
    void loadSinglePostThatIsNotFromMyUniversity() {
        //given
        University university1 = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university1);
        Member member = memberFactory.createVerifiedStudentMember("member", university1);
        Post post = postFactory.createNormalPost(member, board);

        University university2 = universityFactory.createUniversity("서울대학교");
        Member anotherMember = memberFactory.createVerifiedStudentMember("nickanme", university2);

        //when //then
        assertThatThrownBy(
                () -> postService.loadSinglePost(post.getId(),
                        anotherMember.getLoginCredentials().getUsername()))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }

    @DisplayName("글 단건 조회시 조회수 증가 동시성 테스트")
    @Test
    void viewCountConcurrencyTest() throws InterruptedException {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {

            int finalI = i;
            executorService.submit(() -> {

                try {
                    Member member = memberFactory.createVerifiedStudentMember("" + finalI,
                            university);

                    postService.loadSinglePost(post.getId(),
                            member.getLoginCredentials().getUsername());

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(findPost.getViewCount()).isEqualTo(100);
    }

    @DisplayName("글 단건 조회 성공 - 글 작성자 본인이 해당 글을 조회하는 경우 확인이 가능하다")
    @Test
    void loadSinglePost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("member", university);
        Post post = postFactory.createNormalPost(member, board);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response.isMyPost()).isTrue();
    }

    @DisplayName("글 단건 조회 성공 - 해당 글이 익명 글인 경우 글 작성자 컬럼의 값은 null 이다")
    @Test
    void loadAnonymousSinglePost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("member", university);
        Post post = postFactory.createNormalPost(member, board);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response.getNickname()).isNull();
    }

    @DisplayName("글 단건 조회시 내가 해당 글에 좋아요를 눌렀는지 확인 - 좋아요 누른 경우")
    @Test
    void loadSinglePostThatIPressedLike() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createVerifiedStudentMember("member", university);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createUnverifiedStudentMember("nickanme", university);
        PostLike postLike = postLikeFactory.createPostLike(member, post);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response.isPressedLike()).isTrue();
    }

    @DisplayName("글 단건 조회시 내가 해당 글에 좋아요를 눌렀는지 확인 - 좋아요 누르지 않은 경우")
    @Test
    void loadSinglePostThatIDidNotPressedLike() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createVerifiedStudentMember("member", university);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createUnverifiedStudentMember("nickanme", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response.isPressedLike()).isFalse();
    }

    @DisplayName("내가 작성한 글 목록 조회 - 해당 글이 일반글인 경우")
    @ParameterizedTest(name = "{index} 글 작성 회원 유형 : {0}, 게시판 유형 : {1}")
    @MethodSource("allowedRoleToWriteNormalPost")
    void loadMyNormalPost(Role role, BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Board board = boardFactory.createByBoardType(university, boardType);
        Post post = postFactory.createNormalPost(member, board);

        //when
        MyPostsResponse response = postService.loadMyPosts(
                member.getLoginCredentials().getUsername(), 1);

        //then
        assertThat(response).extracting("sizeRequest", "currentPage", "actualSize")
                .containsExactly(20, 1, 1);

        assertThat(response.getPosts()).extracting("boardName", "postId",
                        "title", "commentCount", "pressedLike", "likeCount", "viewCount", "thumbnailUrl")
                .containsExactly(
                        tuple(board.getBoardName(), post.getId(),
                                post.getTitle(), 0, false, 0, 0, null));
    }

    @DisplayName("내가 작성한 글 목록 조회 - 해당 글이 채용글인 경우")
    @ParameterizedTest(name = "{index} 글 작성 회원 유형 : {0}, 게시판 유형 : {1}")
    @MethodSource("allowedRoleToWriteRecruitmentPost")
    void loadMyRecruitmentPost(Role role, BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Board board = boardFactory.createByBoardType(university, boardType);
        Post post = postFactory.createRecruitmentPost(member, board);

        //when
        MyPostsResponse response = postService.loadMyPosts(
                member.getLoginCredentials().getUsername(), 1);

        //then
        assertThat(response).extracting("sizeRequest", "currentPage", "actualSize")
                .containsExactly(20, 1, 1);

        assertThat(response.getPosts()).extracting("boardName", "postId", "companyName",
                        "recruitmentStartDate", "recruitmentEndDate",
                        "title", "commentCount", "pressedLike", "likeCount", "viewCount", "thumbnailUrl")
                .containsExactly(
                        tuple(board.getBoardName(), post.getId(),
                                post.getRecruitment().getCompanyName(),
                                post.getRecruitment().getStartDate(),
                                post.getRecruitment().getEndDate(),
                                post.getTitle(), 0, false, 0, 0, null));
    }

    @DisplayName("내가 작성한 글 목록 조회 - 내가 해당 글에 좋아요를 누른 경우")
    @Test
    void loadMyPostThatIPressedLike() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(member, board);

        postLikeFactory.createPostLike(member, post);

        //when
        MyPostsResponse response = postService.loadMyPosts(
                member.getLoginCredentials().getUsername(), 1);

        //then
        assertThat(response.getPosts().get(0).isPressedLike()).isTrue();
    }

    @DisplayName("내가 작성한 글 목록 조회 - 내가 해당 글에 좋아요를 누르지 않은 경우")
    @Test
    void loadMyPostThatIDidNotPressedLike() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(member, board);

        //when
        MyPostsResponse response = postService.loadMyPosts(
                member.getLoginCredentials().getUsername(), 1);

        //then
        assertThat(response.getPosts().get(0).isPressedLike()).isFalse();
    }


    @DisplayName("내가 작성한 글 목록 조회 - 한 페이지당 글이 20개씩 조회된다")
    @Test
    void loadMyPost_DefaultPageSizeIs20() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Board board = boardFactory.createAnonymousBoard(university);

        IntStream.rangeClosed(1, 21).forEach(i -> postFactory.createNormalPost(member, board));

        //when
        MyPostsResponse response = postService.loadMyPosts(
                member.getLoginCredentials().getUsername(), 1);

        //then
        assertThat(response.getPosts()).size().isEqualTo(20);
    }


    @DisplayName("내가 작성한 글 목록 조회 - 내가 글을 작성한 적이 없는 경우")
    @Test
    void loadMyPostWhenIDidNotWriteAnyPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        //when
        MyPostsResponse response = postService.loadMyPosts(
                member.getLoginCredentials().getUsername(), 1);

        //then
        assertThat(response.getPosts()).isEmpty();
    }


    @DisplayName("내가 작성한 글 목록 조회 실패 - 회원이 존재하지 않는 경우")
    @Test
    void loadNotExistingMemberPost() {

        //when //then
        assertThatThrownBy(() -> postService.loadMyPosts("aaa", 1))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("특정 게시판에 작성된 글 목록 조회 성공 - 일반 글인 경우")
    @Test
    void loadPostsByBoard() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        LoadPostsByBoardServiceRequest request = LoadPostsByBoardServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .type(NORMAL)
                .page(1)
                .order(recent)
                .size(20)
                .build();

        //when
        BoardPostsResponse response = postService.loadPostsByBoard(request);

        //then
        assertThat(response).extracting("boardId", "boardName")
                .containsExactly(board.getId(), board.getBoardName());
        assertThat(response.getPosts()).extracting("postId", "title", "commentCount", "pressedLike",
                        "likeCount", "viewCount", "thumbnailUrl")
                .containsExactly(tuple(post.getId(), post.getTitle(), 0, false,
                        0, 0, null));
    }

    @DisplayName("특정 게시판에 작성된 글 목록 조회 성공 - 채용 글인 경우")
    @Test
    void loadRecruitmentPostsByBoard() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = memberFactory.createVerifiedCompanyMember(university);
        Post post = postFactory.createRecruitmentPost(member, board);

        LoadPostsByBoardServiceRequest request = LoadPostsByBoardServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .type(RECRUITMENT)
                .page(1)
                .order(recent)
                .size(20)
                .build();

        //when
        BoardPostsResponse response = postService.loadPostsByBoard(request);

        //then
        assertThat(response).extracting("boardId", "boardName")
                .containsExactly(board.getId(), board.getBoardName());
        assertThat(response.getPosts()).extracting("postId", "title", "commentCount", "pressedLike",
                        "likeCount", "viewCount", "thumbnailUrl", "memberRole", "companyName",
                        "recruitmentStartDate", "recruitmentEndDate")
                .containsExactly(tuple(post.getId(), post.getTitle(), 0, false,
                        0, 0, null, member.getRole().name(), post.getRecruitment().getCompanyName(),
                        post.getRecruitment().getStartDate(), post.getRecruitment().getEndDate()));
    }

    @DisplayName("특정 게시판에 작성된 글 목록 조회 실패 - 존재하지 않는 게시판 Id인 경우")
    @Test
    void loadPostsByNotExistingBoardId() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedCompanyMember(university);

        LoadPostsByBoardServiceRequest request = LoadPostsByBoardServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(-1L)
                .type(RECRUITMENT)
                .page(1)
                .order(recent)
                .size(20)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.loadPostsByBoard(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시판을 찾을수 없습니다");
    }


    @DisplayName("특정 게시판에 작성된 글 목록 조회 실패 - 해당 게시판이 내가 속한 대학교의 게시판이 아닌 경우")
    @Test
    void loadPostsByBoardThatIsNotInMyUniversity() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);

        University otherUniversity = universityFactory.createUniversity("서울대학교");
        Member member = memberFactory.createUnverifiedStudentMember("nickname", otherUniversity);

        LoadPostsByBoardServiceRequest request = LoadPostsByBoardServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .type(NORMAL)
                .page(1)
                .order(recent)
                .size(20)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.loadPostsByBoard(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }

    public static Stream<Arguments> invalidBoardTypeAndPostTypeCombination() {

        return Stream.of(
                Arguments.of(EMPLOYMENT, ANNOUNCEMENT),

                Arguments.of(ACTIVITY, ANNOUNCEMENT),

                Arguments.of(CLUB, ANNOUNCEMENT),
                Arguments.of(CLUB, RECRUITMENT),

                Arguments.of(ANONYMOUS, ANNOUNCEMENT),
                Arguments.of(ANONYMOUS, RECRUITMENT),

                Arguments.of(CAMPUS_LIFE, RECRUITMENT)
        );
    }

    @DisplayName("특정 게시판에 작성된 글 목록 조회 성공 - 게시판 유형과 글 유형이 옳바르지 않은 경우")
    @ParameterizedTest(name = "{index} 게시판 유형 : {0}, 글 유형 : {1}")
    @MethodSource("invalidBoardTypeAndPostTypeCombination")
    void loadPostsByBoard_ValidBoardTypeAndPostType(BoardType boardType, PostType postType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createUnverifiedStudentMember("nickname", university);

        LoadPostsByBoardServiceRequest request = LoadPostsByBoardServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardId(board.getId())
                .type(postType)
                .page(1)
                .order(recent)
                .size(20)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.loadPostsByBoard(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("게시판 정보가 옳바르지 않습니다");
    }

    @DisplayName("특정글에 사진 업로드 실패 - 업로드 하려는 사진이 10장이 넘을 경우")
    @Test
    void uploadImagesToPost_MaxImageSizeExceeded() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        List<MultipartFile> files = new ArrayList<>();
        IntStream.rangeClosed(1, 11).forEach(i -> {
            MockMultipartFile multipartFile = new MockMultipartFile("file " + i, new byte[]{1, 2, 3, 4});
            files.add(multipartFile);
        });

        //when //then
        assertThatThrownBy(() -> postService.uploadPostImages(post.getId(), files,
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("최대 10개까지 업로드 할 수 있습니다");
    }

    @DisplayName("특정글에 사진 업로드 실패 - 업로드할 파일 List가 null 인 경우")
    @Test
    void uploadImagesToPost_filesListIsNull() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        List<MultipartFile> files = null;

        //when //then
        assertThatThrownBy(() -> postService.uploadPostImages(post.getId(), files,
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("업로드할 사진이 선택되지 않았습니다");
    }

    @DisplayName("특정글에 사진 업로드 실패 - 업로드할 파일 List가 비어있는 경우")
    @Test
    void uploadImagesToPost_filesListIsEmpty() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        List<MultipartFile> files = new ArrayList<>();

        //when //then
        assertThatThrownBy(() -> postService.uploadPostImages(post.getId(), files,
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("업로드할 사진이 선택되지 않았습니다");
    }

    @DisplayName("특정글에 사진 업로드 실패 - 업로드할 파일이 nul 인 경우")
    @Test
    void uploadImagesToPost_fileIsNull() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        List<MultipartFile> files = new ArrayList<>();
        files.add(null);
        files.add(null);
        files.add(null);

        //when //then
        assertThatThrownBy(() -> postService.uploadPostImages(post.getId(), files,
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("업로드할 사진이 선택되지 않았습니다");
    }

    @DisplayName("특정글에 사진 업로드 실패 - 업로드할 파일이 비어있는 경우")
    @Test
    void uploadImagesToPost_fileIsEmpty() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        List<MultipartFile> files = new ArrayList<>();
        MockMultipartFile multipartFile = new MockMultipartFile("emptyFile", new byte[]{});
        files.add(multipartFile);

        //when //then
        assertThatThrownBy(() -> postService.uploadPostImages(post.getId(), files,
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("업로드할 사진이 선택되지 않았습니다");
    }

    @DisplayName("특정글에 사진 업로드 실패 - 업로드를 요청한 회원이 존재하지 않는 경우")
    @Test
    void uploadImagesToPostByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        List<MultipartFile> files = new ArrayList<>();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "emptyFile", new byte[]{1, 2, 3, 4, 5, 6});
        files.add(multipartFile);

        //when //then
        assertThatThrownBy(() -> postService.uploadPostImages(post.getId(), files, "aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("특정글에 사진 업로드 실패 - 업로드를 요청한 회원이 탈퇴한 회원인 경우")
    @Test
    void uploadImagesToPostByDeletedMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        List<MultipartFile> files = new ArrayList<>();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "emptyFile", new byte[]{1, 2, 3, 4, 5, 6});
        files.add(multipartFile);

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> postService.uploadPostImages(post.getId(), files,
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("특정글에 사진 업로드 실패 - 사진을 업로드할 글이 존재하지 않는 경우")
    @Test
    void uploadImagesToNotExistingPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        List<MultipartFile> files = new ArrayList<>();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "emptyFile", new byte[]{1, 2, 3, 4, 5, 6});
        files.add(multipartFile);

        //when //then
        assertThatThrownBy(() -> postService.uploadPostImages(-1L, files,
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("특정글에 사진 업로드 실패 - 사진을 업로드할 글이 삭제된 경우")
    @Test
    void uploadImagesToDeletedPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        List<MultipartFile> files = new ArrayList<>();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "emptyFile", new byte[]{1, 2, 3, 4, 5, 6});
        files.add(multipartFile);

        postService.deletePost(post.getId(), member.getLoginCredentials().getUsername());

        //when //then
        assertThatThrownBy(() -> postService.uploadPostImages(post.getId(), files,
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("특정글에 사진 업로드 실패 - 글 작성자와 사진 업로드를 요청한 회원이 서로 다른 경우")
    @Test
    void uploadImagesToPostByNonPostWriterMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        List<MultipartFile> files = new ArrayList<>();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "emptyFile", new byte[]{1, 2, 3, 4, 5, 6});
        files.add(multipartFile);

        Member anotherMember = memberFactory.createVerifiedCompanyMember(university);

        //when //then
        assertThatThrownBy(() -> postService.uploadPostImages(post.getId(), files,
                anotherMember.getLoginCredentials().getUsername()))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }


    @DisplayName("키워드로 글 검색 성공 - 글 제목으로 검색")
    @ParameterizedTest(name = "{index} 게시판 유형 : {0}, 글 유형 : {1}")
    @MethodSource("allowedCombinationToWritePost")
    void searchPostsByTitle(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(member, board, postType);

        SearchPostsByKeywordServiceRequest request = SearchPostsByKeywordServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .keyword("제목")
                .page(1)
                .build();

        //when
        SearchedPostsResponse response = postService.searchPostsByKeyword(request);

        //then
        assertThat(response).extracting("sizeRequest", "actualSize", "currentPage")
                .containsExactly(20, 1, 1);
        assertThat(response.getPosts()).extracting(
                        "content", "boardName", "boardId", "postId",
                        "title", "likeCount", "pressedLike", "viewCount", "thumbnailUrl")
                .containsExactly(tuple("내용입니다", board.getBoardName(), board.getId(), post.getId(),
                        post.getTitle(), 0, false, 0, null));

    }

    @DisplayName("키워드로 글 검색 성공 - 글 내용으로 검색")
    @ParameterizedTest(name = "{index} 게시판 유형 : {0}, 글 유형 : {1}")
    @MethodSource("allowedCombinationToWritePost")
    void searchPostsByContent(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(member, board, postType);

        SearchPostsByKeywordServiceRequest request = SearchPostsByKeywordServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .keyword("내용")
                .page(1)
                .build();

        //when
        SearchedPostsResponse response = postService.searchPostsByKeyword(request);

        //then
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getPosts()).size().isEqualTo(1);
        assertThat(response.getPosts()).extracting(
                        "content", "boardName", "boardId", "postId",
                        "title", "likeCount", "pressedLike", "viewCount", "thumbnailUrl")
                .containsExactly(tuple("내용입니다", board.getBoardName(), board.getId(), post.getId(),
                        post.getTitle(), 0, false, 0, null));

    }

    @DisplayName("키워드로 글 검색 실패 - 조건을 만족하는 글이 없는 경우")
    @Test
    void searchPostsByKeyword_Fail_NoMatchingPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("member", university);

        SearchPostsByKeywordServiceRequest request = SearchPostsByKeywordServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .keyword("내용")
                .page(1)
                .build();

        //when
        SearchedPostsResponse response = postService.searchPostsByKeyword(request);

        //then
        assertThat(response.getPosts()).isEmpty();
    }

    @DisplayName("키워드로 글 검색 실패 - 조회를 요청한 회원이 존재하지 않는 회원인 경우")
    @Test
    void searchPostsByKeywordByNotExistingMember() {
        //given
        SearchPostsByKeywordServiceRequest request = SearchPostsByKeywordServiceRequest.builder()
                .username("aaaa")
                .keyword("내용")
                .page(1)
                .build();

        //when //then
        assertThatThrownBy(() -> postService.searchPostsByKeyword(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }
}