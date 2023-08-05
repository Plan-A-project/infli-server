package com.plana.infli.service;

import static com.plana.infli.domain.BoardType.*;
import static com.plana.infli.domain.PostType.*;
import static com.plana.infli.domain.Role.*;
import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.BoardType;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.Role;
import com.plana.infli.domain.University;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.NotFoundException;
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
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.post.create.normal.CreateNormalPostServiceRequest;
import com.plana.infli.web.dto.request.post.create.recruitment.CreateRecruitmentPostServiceRequest;
import jakarta.persistence.EntityManager;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class PostServiceTest {

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
    private CommentService commentService;

    @Autowired
    private UniversityFactory universityFactory;

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
    private EntityManager em;

    @AfterEach
    void tearDown() {
        commentLikeRepository.deleteAllInBatch();
        commentRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }

    /**
     * 모집글 작성
     */
    public static Stream<Arguments> SUCCESS_provideRoleAndBoardTypeForCheckingWritePermissionOnRecruitment() {

        return Stream.of(
                Arguments.of(ACTIVITY, STUDENT),
                Arguments.of(ACTIVITY, ADMIN),
                Arguments.of(ACTIVITY, COMPANY),

                Arguments.of(EMPLOYMENT, COMPANY),
                Arguments.of(EMPLOYMENT, ADMIN)
        );
    }

    @DisplayName("모집 글 작성 성공 - 회원 유형, 게시판 유형에 따른 케이스 분류")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("SUCCESS_provideRoleAndBoardTypeForCheckingWritePermissionOnRecruitment")
    void SUCCESS_writeRecruitmentPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createBoard(university, boardType);
        Member member = memberFactory.createMember("member", university, role);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .email(member.getEmail())
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(now())
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
        assertThat(post.getPostType()).isEqualTo(RECRUITMENT);
    }


    public static Stream<Arguments> FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnRecruitment() {

        return Stream.of(
                Arguments.of(ACTIVITY, UNCERTIFIED),
                Arguments.of(ACTIVITY, STUDENT_COUNCIL),

                Arguments.of(EMPLOYMENT, UNCERTIFIED),
                Arguments.of(EMPLOYMENT, STUDENT),
                Arguments.of(EMPLOYMENT, STUDENT_COUNCIL)
        );
    }

    @DisplayName("모집 글 작성 실패 - 회원 유형, 게시판 유형에 따른 케이스 분류")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnRecruitment")
    void FAIL_writeRecruitmentPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createBoard(university, boardType);
        Member member = memberFactory.createMember("member", university, role);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .email(member.getEmail())
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


    public static Stream<Arguments> SUCCESS_provideRoleAndBoardTypeForCheckingWritePermissionOnNormal() {

        return Stream.of(
                Arguments.of(ACTIVITY, STUDENT),
                Arguments.of(ACTIVITY, ADMIN),
                Arguments.of(ACTIVITY, COMPANY),

                Arguments.of(EMPLOYMENT, COMPANY),
                Arguments.of(EMPLOYMENT, ADMIN),
                Arguments.of(EMPLOYMENT, STUDENT),

                Arguments.of(ANONYMOUS, STUDENT),
                Arguments.of(EMPLOYMENT, ADMIN),

                Arguments.of(CLUB, STUDENT),
                Arguments.of(CLUB, ADMIN),

                Arguments.of(CAMPUS_LIFE, STUDENT),
                Arguments.of(CAMPUS_LIFE, STUDENT_COUNCIL),
                Arguments.of(CAMPUS_LIFE, ADMIN)
        );
    }

    @DisplayName("일반 글 작성 성공 - 게시판 유형, 회원 유형에 따른 케이스 분류")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("SUCCESS_provideRoleAndBoardTypeForCheckingWritePermissionOnNormal")
    void SUCCESS_writeNormalPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createBoard(university, boardType);
        Member member = memberFactory.createMember("member", university, role);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .email(member.getEmail())
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

    public static Stream<Arguments> FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnNormal() {

        return Stream.of(
                Arguments.of(ANONYMOUS, UNCERTIFIED),
                Arguments.of(ANONYMOUS, STUDENT_COUNCIL),
                Arguments.of(ANONYMOUS, COMPANY),

                Arguments.of(ACTIVITY, UNCERTIFIED),
                Arguments.of(ACTIVITY, STUDENT_COUNCIL),

                Arguments.of(EMPLOYMENT, UNCERTIFIED),
                Arguments.of(EMPLOYMENT, STUDENT_COUNCIL),

                Arguments.of(CLUB, UNCERTIFIED),
                Arguments.of(CLUB, STUDENT_COUNCIL),
                Arguments.of(CLUB, COMPANY),

                Arguments.of(CAMPUS_LIFE, UNCERTIFIED),
                Arguments.of(CAMPUS_LIFE, COMPANY)
        );
    }

    @DisplayName("일반 글 작성 실패 - 회원 유형, 게시판 유형에 따른 케이스 분류")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnNormal")
    void FAIL_writeNormalPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createBoard(university, boardType);
        Member member = memberFactory.createMember("member", university, role);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .email(member.getEmail())
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


    public static Stream<Arguments> SUCCESS_provideRoleAndBoardTypeForCheckingWritePermissionOnAnnouncement() {

        return Stream.of(
                Arguments.of(CAMPUS_LIFE, STUDENT_COUNCIL),
                Arguments.of(CAMPUS_LIFE, ADMIN)
        );
    }

    @DisplayName("공지 글 작성 성공 - 게시판 유형, 회원 유형에 따른 케이스 분류")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("SUCCESS_provideRoleAndBoardTypeForCheckingWritePermissionOnAnnouncement")
    void SUCCESS_writeAnnouncementPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createBoard(university, boardType);
        Member member = memberFactory.createMember("member", university, role);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .email(member.getEmail())
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
        assertThat(post.getBoard().getBoardType()).isEqualTo(boardType);
        assertThat(post.getPostType()).isEqualTo(ANNOUNCEMENT);
    }

    public static Stream<Arguments> FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnAnnouncement() {

        return Stream.of(
                Arguments.of(CAMPUS_LIFE, UNCERTIFIED),
                Arguments.of(CAMPUS_LIFE, STUDENT),
                Arguments.of(CAMPUS_LIFE, COMPANY)
        );
    }

    @DisplayName("공지 글 작성 실패 - 회원 유형, 게시판 유형에 따른 케이스 분류")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnAnnouncement")
    void FAIL_writeAnnouncementPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createBoard(university, boardType);
        Member member = memberFactory.createMember("member", university, role);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .email(member.getEmail())
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


    public static Stream<Arguments> FAIL_providingRecruitmentTypeForWritingNormalPost() {

        return Stream.of(
                Arguments.of(ANONYMOUS),
                Arguments.of(ACTIVITY),
                Arguments.of(EMPLOYMENT),
                Arguments.of(CAMPUS_LIFE),
                Arguments.of(CLUB)
        );
    }

    @DisplayName("일반 글 작성 실패 - 글 작성시 글 종류 요청 값이 일반이 아니라 모집인 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}")
    @MethodSource("FAIL_providingRecruitmentTypeForWritingNormalPost")
    void FAIL_writeNormalPostWithRecruitmentType(BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createBoard(university, boardType);
        Member member = memberFactory.createAdminMember("member", university);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .email(member.getEmail())
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

    public static Stream<Arguments> FAIL_providingInvalidBoardTypeForWritingAnnouncementPost() {

        return Stream.of(
                Arguments.of(ANONYMOUS),
                Arguments.of(ACTIVITY),
                Arguments.of(EMPLOYMENT),
                Arguments.of(CLUB)
        );
    }

    @DisplayName("공지 글 작성 실패 - 공지글 작성시 잘못된 게시판 종류가 입력된 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}")
    @MethodSource("FAIL_providingInvalidBoardTypeForWritingAnnouncementPost")
    void FAIL_writeAnnouncementPostWithInvalidBoardType(BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createBoard(university, boardType);
        Member member = memberFactory.createAdminMember("member", university);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .email(member.getEmail())
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

    public static Stream<Arguments> FAIL_providingInvalidBoardTypeForWritingRecruitmentPost() {

        return Stream.of(
                Arguments.of(ANONYMOUS),
                Arguments.of(CLUB),
                Arguments.of(CAMPUS_LIFE)
        );
    }

    @DisplayName("모집 글 작성 실패 - 모집글 작성시 잘못된 게시판 종류가 입력된 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}")
    @MethodSource("FAIL_providingInvalidBoardTypeForWritingRecruitmentPost")
    void FAIL_writeRecruitmentPostWithInvalidBoardType(BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createBoard(university, boardType);
        Member member = memberFactory.createAdminMember("member", university);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .email(member.getEmail())
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

    @DisplayName("일반글 작성 실패 - 존재하지 않는 게시판에 글을 작성할수 없다")
    @Test
    void writeNormalPostInNotExistingBoard() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createAdminMember("member", university);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .email(member.getEmail())
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
        Member member = memberFactory.createAdminMember("member", university);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .email("aaa")
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

    @DisplayName("공지글 작성 실패 - 존재하지 않는 게시판에 글을 작성할수 없다")
    @Test
    void writeAnnouncementPostInNotExistingBoard() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createAdminMember("member", university);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .email(member.getEmail())
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

    @DisplayName("공지글 작성 실패 - 존재하지 않는 회원의 계정으로 글을 작성할수 없다")
    @Test
    void writeAnnouncementPostByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .email("aaa")
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

    @DisplayName("모집글 작성 실패 - 존재하지 않는 게시판에 글을 작성할수 없다")
    @Test
    void writeRecruitmentPostInNotExistingBoard() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createAdminMember("member", university);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .email(member.getEmail())
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
                .email("aaa")
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
        Member member = memberFactory.createCompanyMember("member", university);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .email(member.getEmail())
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
}
