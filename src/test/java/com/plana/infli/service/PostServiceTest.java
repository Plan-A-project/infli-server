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
import com.plana.infli.domain.PostType;
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
import com.plana.infli.web.dto.request.post.edit.normal.EditNormalPostServiceRequest;
import com.plana.infli.web.dto.request.post.edit.recruitment.EditRecruitmentPostServiceRequest;
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
    public static Stream<Arguments> possibleCombinationToWriteRecruitmentPost() {

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
    @MethodSource("possibleCombinationToWriteRecruitmentPost")
    void SUCCESS_writeRecruitmentPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .email(member.getEmail())
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


    public static Stream<Arguments> FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnRecruitment() {

        return Stream.of(
                Arguments.of(ACTIVITY, UNCERTIFIED_STUDENT),
                Arguments.of(ACTIVITY, UNCERTIFIED_COMPANY),
                Arguments.of(ACTIVITY, STUDENT_COUNCIL),

                Arguments.of(EMPLOYMENT, UNCERTIFIED_STUDENT),
                Arguments.of(EMPLOYMENT, UNCERTIFIED_COMPANY),
                Arguments.of(EMPLOYMENT, STUDENT),
                Arguments.of(EMPLOYMENT, STUDENT_COUNCIL)
        );
    }

    @DisplayName("모집 글 작성 실패 - 해당 게시판에 모집글 작성 권한이 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnRecruitment")
    void FAIL_writeRecruitmentPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

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


    public static Stream<Arguments> possibleCombinationsToWriteNormalPost() {

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

    @DisplayName("일반 글 작성 성공 - 게시판 유형, 회원 유형에 따른 케이스 분류")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("possibleCombinationsToWriteNormalPost")
    void SUCCESS_writeNormalPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

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
                Arguments.of(ANONYMOUS, UNCERTIFIED_STUDENT),
                Arguments.of(ANONYMOUS, UNCERTIFIED_COMPANY),
                Arguments.of(ANONYMOUS, STUDENT_COUNCIL),
                Arguments.of(ANONYMOUS, COMPANY),

                Arguments.of(ACTIVITY, UNCERTIFIED_STUDENT),
                Arguments.of(ACTIVITY, UNCERTIFIED_COMPANY),
                Arguments.of(ACTIVITY, STUDENT_COUNCIL),

                Arguments.of(EMPLOYMENT, UNCERTIFIED_STUDENT),
                Arguments.of(EMPLOYMENT, UNCERTIFIED_COMPANY),
                Arguments.of(EMPLOYMENT, STUDENT_COUNCIL),

                Arguments.of(CLUB, UNCERTIFIED_STUDENT),
                Arguments.of(CLUB, UNCERTIFIED_COMPANY),
                Arguments.of(CLUB, STUDENT_COUNCIL),
                Arguments.of(CLUB, COMPANY),

                Arguments.of(CAMPUS_LIFE, UNCERTIFIED_STUDENT),
                Arguments.of(CAMPUS_LIFE, UNCERTIFIED_COMPANY),
                Arguments.of(CAMPUS_LIFE, COMPANY)
        );
    }

    @DisplayName("일반 글 작성 실패 - 해당 게시판에 일반글 작성 권한이 없는경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnNormal")
    void FAIL_writeNormalPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

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


    public static Stream<Arguments> possibleCombinationsToWriteAnnouncementPost() {

        return Stream.of(
                Arguments.of(CAMPUS_LIFE, STUDENT_COUNCIL),
                Arguments.of(CAMPUS_LIFE, ADMIN)
        );
    }

    @DisplayName("일반 공지 글 작성 성공 - 게시판 유형, 회원 유형에 따른 케이스 분류")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("possibleCombinationsToWriteAnnouncementPost")
    void SUCCESS_writeAnnouncementPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

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
                Arguments.of(CAMPUS_LIFE, UNCERTIFIED_STUDENT),
                Arguments.of(CAMPUS_LIFE, UNCERTIFIED_COMPANY),
                Arguments.of(CAMPUS_LIFE, STUDENT),
                Arguments.of(CAMPUS_LIFE, COMPANY)
        );
    }

    @DisplayName("일반 공지글 작성 실패 - 공지글 작성 권한이 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnAnnouncement")
    void FAIL_writeAnnouncementPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

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

    @DisplayName("일반 글 작성 실패 - 일반글을 작성하는 곳에 모집글을 작성하려고 하는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}")
    @MethodSource("FAIL_providingRecruitmentTypeForWritingNormalPost")
    void FAIL_writeNormalPostWithRecruitmentType(BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createAdminMember(university);

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

    @DisplayName("일반 공지 글 작성 실패 - 공지글 작성이 허용되지 않는 게시판에 공지글 작성 시도")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}")
    @MethodSource("FAIL_providingInvalidBoardTypeForWritingAnnouncementPost")
    void FAIL_writeAnnouncementPostWithInvalidBoardType(BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createAdminMember(university);

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

    @DisplayName("모집 글 작성 실패 - 모집글 작성이 허용되지 않는 게시판에 모집글 작성 시도")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}")
    @MethodSource("FAIL_providingInvalidBoardTypeForWritingRecruitmentPost")
    void FAIL_writeRecruitmentPostWithInvalidBoardType(BoardType boardType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.createAdminMember(university);

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
        Member member = memberFactory.createAdminMember(university);

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
        Member member = memberFactory.createAdminMember(university);

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

    @DisplayName("일반 공지글 작성 실패 - 존재하지 않는 게시판에 글을 작성할수 없다")
    @Test
    void writeAnnouncementPostInNotExistingBoard() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createAdminMember(university);

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

    @DisplayName("일반 공지글 작성 실패 - 존재하지 않는 회원의 계정으로 글을 작성할수 없다")
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
        Member member = memberFactory.createAdminMember(university);

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
        Member member = memberFactory.createCompanyMember("카카오", university);

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


    public static Stream<Arguments> providingRoleForCheckingMemberAcceptedWritePolicy() {
        return Stream.of(
                Arguments.of(UNCERTIFIED_STUDENT),
                Arguments.of(UNCERTIFIED_COMPANY),
                Arguments.of(STUDENT),
                Arguments.of(COMPANY),
                Arguments.of(STUDENT_COUNCIL),
                Arguments.of(ADMIN)
        );
    }

    @DisplayName("글 작성 규정 동의 여부 확인 - 동의 안한 경우")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("providingRoleForCheckingMemberAcceptedWritePolicy")
    void False_checkMemberAcceptedWritePolicy(Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyNotAccepted(university, role);

        //when
        boolean agreedOnWritePolicy = postService.checkMemberAcceptedWritePolicy(member.getEmail());

        //then
        assertThat(agreedOnWritePolicy).isFalse();
    }

    @DisplayName("글 작성 규정 동의 여부 확인 - 동의한 경우")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("providingRoleForCheckingMemberAcceptedWritePolicy")
    void True_checkMemberAcceptedWritePolicy(Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

        //when
        boolean agreedOnWritePolicy = postService.checkMemberAcceptedWritePolicy(member.getEmail());

        //then
        assertThat(agreedOnWritePolicy).isTrue();
    }

    public static Stream<Arguments> providingRoleForMemberAcceptingWritePolicy() {
        return Stream.of(
                Arguments.of(UNCERTIFIED_STUDENT),
                Arguments.of(UNCERTIFIED_COMPANY),
                Arguments.of(STUDENT),
                Arguments.of(COMPANY),
                Arguments.of(STUDENT_COUNCIL),
                Arguments.of(ADMIN)
        );
    }

    @DisplayName("글 작성 규정 동의함 요청 성공")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("providingRoleForMemberAcceptingWritePolicy")
    void Success_AcceptingWritePolicy(Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyNotAccepted(university, role);

        //when
        postService.acceptWritePolicy(member.getEmail());

        //then
        Member findMember = memberRepository.findByEmail(member.getEmail()).get();
        assertThat(findMember.getStatus().isPolicyAccepted()).isTrue();
    }

    @DisplayName("글 작성 규정 동의함 요청 실패 - 해당 회원이 탈퇴 회원인 경우")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("providingRoleForMemberAcceptingWritePolicy")
    void Fail_AcceptingWritePolicyByDeletedMember(Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyNotAccepted(university, role);
        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> postService.acceptWritePolicy("aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("글 작성 규정 동의함 요청 실패 - 회원이 존재하지 않는 경우")
    @Test
    void Fail_AcceptingWritePolicyByNotExistingMember() {
        //when //then
        assertThatThrownBy(() -> postService.acceptWritePolicy("aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    public static Stream<Arguments> possibleCombinationToWritePost() {

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
    @MethodSource("possibleCombinationToWritePost")
    void SUCCESS_checkMemberHasWritePolicy(BoardType boardType, PostType postType, Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

        //when //then
        assertThat(postService.checkMemberHasWritePermission(board.getId(),
                member.getEmail(), postType)).isTrue();
    }

    public static Stream<Arguments> Fail_provideRoleAndBoardTypeAndPostTypeForCheckingMemberHasWritePolicy() {

        return Stream.of(
                Arguments.of(EMPLOYMENT, NORMAL, UNCERTIFIED_STUDENT),
                Arguments.of(EMPLOYMENT, NORMAL, UNCERTIFIED_COMPANY),
                Arguments.of(EMPLOYMENT, NORMAL, STUDENT_COUNCIL),
                Arguments.of(EMPLOYMENT, RECRUITMENT, UNCERTIFIED_STUDENT),
                Arguments.of(EMPLOYMENT, RECRUITMENT, UNCERTIFIED_COMPANY),
                Arguments.of(EMPLOYMENT, RECRUITMENT, STUDENT),
                Arguments.of(EMPLOYMENT, RECRUITMENT, STUDENT_COUNCIL),

                Arguments.of(ACTIVITY, NORMAL, UNCERTIFIED_STUDENT),
                Arguments.of(ACTIVITY, NORMAL, UNCERTIFIED_COMPANY),
                Arguments.of(ACTIVITY, NORMAL, STUDENT_COUNCIL),
                Arguments.of(ACTIVITY, RECRUITMENT, UNCERTIFIED_STUDENT),
                Arguments.of(ACTIVITY, RECRUITMENT, UNCERTIFIED_COMPANY),
                Arguments.of(ACTIVITY, RECRUITMENT, STUDENT_COUNCIL),

                Arguments.of(CLUB, NORMAL, UNCERTIFIED_STUDENT),
                Arguments.of(CLUB, NORMAL, UNCERTIFIED_COMPANY),
                Arguments.of(CLUB, NORMAL, STUDENT_COUNCIL),
                Arguments.of(CLUB, NORMAL, COMPANY),

                Arguments.of(ANONYMOUS, NORMAL, UNCERTIFIED_STUDENT),
                Arguments.of(ANONYMOUS, NORMAL, UNCERTIFIED_COMPANY),
                Arguments.of(ANONYMOUS, NORMAL, STUDENT_COUNCIL),
                Arguments.of(ANONYMOUS, NORMAL, COMPANY),

                Arguments.of(CAMPUS_LIFE, NORMAL, UNCERTIFIED_STUDENT),
                Arguments.of(CAMPUS_LIFE, NORMAL, UNCERTIFIED_COMPANY),
                Arguments.of(CAMPUS_LIFE, NORMAL, COMPANY),
                Arguments.of(CAMPUS_LIFE, ANNOUNCEMENT, UNCERTIFIED_STUDENT),
                Arguments.of(CAMPUS_LIFE, ANNOUNCEMENT, UNCERTIFIED_COMPANY),
                Arguments.of(CAMPUS_LIFE, ANNOUNCEMENT, STUDENT),
                Arguments.of(CAMPUS_LIFE, ANNOUNCEMENT, COMPANY)
        );
    }

    @DisplayName("해당 게시판에 글 작성 권한 있는지 여부 확인 - 권한 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("Fail_provideRoleAndBoardTypeAndPostTypeForCheckingMemberHasWritePolicy")
    void Fail_checkMemberHasWritePolicy(BoardType boardType, PostType postType, Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

        //when //then
        assertThatThrownBy(() -> postService.checkMemberHasWritePermission(board.getId(),
                member.getEmail(), postType))
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
                Arguments.of(CAMPUS_LIFE, ADMIN),
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
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

        Post post = postFactory.createNormalPost(member, board);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .email(member.getEmail())
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
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createNormalPost(member, board);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .email("aaa")
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
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createNormalPost(member, board);

        memberRepository.delete(member);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .email(member.getEmail())
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
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .email(member.getEmail())
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
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createNormalPost(member, board);

        postService.deletePost(post.getId(), member.getEmail());

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .email(member.getEmail())
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
    @MethodSource("possibleCombinationToWriteRecruitmentPost")
    void Fail_InvalidRequestToEditRecruitmentPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createRecruitmentPost(member, board);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .email(member.getEmail())
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
        Member postMember = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createStudentMember("member", university);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .email(member.getEmail())
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
    @MethodSource("possibleCombinationToWriteRecruitmentPost")
    void Success_EditRecruitPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

        Post post = postFactory.createRecruitmentPost(member, board);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .email(member.getEmail())
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
    @MethodSource("possibleCombinationToWriteRecruitmentPost")
    void Fail_EditRecruitmentPostByNotExistingMember(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createRecruitmentPost(member, board);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .email("unknown")
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
    @MethodSource("possibleCombinationToWriteRecruitmentPost")
    void Fail_EditRecruitmentPostByDeletedMember(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createRecruitmentPost(member, board);

        memberRepository.delete(member);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .email("unknown")
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
    @MethodSource("possibleCombinationToWriteRecruitmentPost")
    void Fail_EditNotExistingRecruitmentPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .email(member.getEmail())
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
    @MethodSource("possibleCombinationToWriteRecruitmentPost")
    void Fail_EditDeletedRecruitmentPost(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createRecruitmentPost(member, board);

        postService.deletePost(post.getId(), member.getEmail());

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .email(member.getEmail())
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
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createNormalPost(member, board);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .email(member.getEmail())
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
    @MethodSource("possibleCombinationToWriteRecruitmentPost")
    void Fail_EditRecruitmentPostWroteByOtherMember(BoardType boardType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createRecruitmentPost(postMember, board);

        Member member = memberFactory.createStudentMember("member", university);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .email(member.getEmail())
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
    @MethodSource("possibleCombinationToWritePost")
    void Success_DeletePost(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createPost(member, board, postType);

        //when
        postService.deletePost(post.getId(), member.getEmail());

        //then
        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(findPost.isDeleted()).isTrue();
    }

    @DisplayName("글 삭제 성공 - 관리자는 타인이 작성한 글도 삭제할수 있다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("possibleCombinationToWritePost")
    void Success_DeletePostByAdmin(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createPost(member, board, postType);

        Member admin = memberFactory.createAdminMember(university);

        //when
        postService.deletePost(post.getId(), admin.getEmail());

        //then
        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(findPost.isDeleted()).isTrue();
    }

    @DisplayName("글 삭제 실패 - 글 작성자를 찾을수 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("possibleCombinationToWritePost")
    void Fail_DeletePostByNotExistingMember(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createPost(member, board, postType);

        //when //then
        assertThatThrownBy(() -> postService.deletePost(post.getId(), "aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("글 삭제 실패 - 글 작성자가 회원 탈퇴를 한 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("possibleCombinationToWritePost")
    void Fail_DeletePostByDeletedMember(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createPost(member, board, postType);

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> postService.deletePost(post.getId(), member.getEmail()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }


    public static Stream<Arguments> existingMemberRole() {

        return Stream.of(
                Arguments.of(STUDENT),
                Arguments.of(STUDENT_COUNCIL),
                Arguments.of(ADMIN),
                Arguments.of(COMPANY),
                Arguments.of(UNCERTIFIED_COMPANY),
                Arguments.of(UNCERTIFIED_STUDENT)
        );
    }


    @DisplayName("글 삭제 실패 - 존재하지 않는글을 삭제할수 없다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("existingMemberRole")
    void Fail_DeleteNotExistingPost(Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);

        //when //then
        assertThatThrownBy(() -> postService.deletePost(-10L, member.getEmail()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("글 삭제 실패 - 이미 삭제된 글을 삭제 시도 할수 없다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("possibleCombinationToWritePost")
    void Fail_DeletePostThatIsAlreadyDeleted(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createPost(member, board, postType);

        postService.deletePost(post.getId(), member.getEmail());

        //when //then
        assertThatThrownBy(() -> postService.deletePost(post.getId(), member.getEmail()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("글 삭제 실패 - 내가 작성하지 않은글을 삭제할수 없다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("possibleCombinationToWritePost")
    void Fail_DeletePostThatIsNotMine(BoardType boardType, PostType postType, Role role) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member postMember = memberFactory.parameterizedTest_PolicyAccepted(university, role);
        Post post = postFactory.createPost(postMember, board, postType);

        Member member = memberFactory.createStudentMember("member", university);

        //when //then
        assertThatThrownBy(() -> postService.deletePost(post.getId(), member.getEmail()))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }

}
