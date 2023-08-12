package com.plana.infli.service;

import static com.plana.infli.domain.type.BoardType.*;
import static com.plana.infli.domain.type.PostType.*;
import static com.plana.infli.domain.type.MemberRole.*;
import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.linesOf;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.type.BoardType;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.type.MemberRole;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.type.PostType;
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
import com.plana.infli.web.dto.response.post.single.SinglePostResponse;
import jakarta.persistence.EntityManager;
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
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
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
    void SUCCESS_writeRecruitmentPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .username(member.getUsername())
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
                Arguments.of(ACTIVITY, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(ACTIVITY, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(ACTIVITY, STUDENT_COUNCIL),

                Arguments.of(EMPLOYMENT, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(EMPLOYMENT, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(EMPLOYMENT, STUDENT),
                Arguments.of(EMPLOYMENT, STUDENT_COUNCIL)
        );
    }

    @DisplayName("모집 글 작성 실패 - 해당 게시판에 모집글 작성 권한이 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnRecruitment")
    void FAIL_writeRecruitmentPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .username(member.getUsername())
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
    void SUCCESS_writeNormalPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getUsername())
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
                Arguments.of(ANONYMOUS, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(ANONYMOUS, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(ANONYMOUS, STUDENT_COUNCIL),
                Arguments.of(ANONYMOUS, COMPANY),

                Arguments.of(ACTIVITY, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(ACTIVITY, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(ACTIVITY, STUDENT_COUNCIL),

                Arguments.of(EMPLOYMENT, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(EMPLOYMENT, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(EMPLOYMENT, STUDENT_COUNCIL),

                Arguments.of(CLUB, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(CLUB, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(CLUB, STUDENT_COUNCIL),
                Arguments.of(CLUB, COMPANY),

                Arguments.of(CAMPUS_LIFE, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(CAMPUS_LIFE, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(CAMPUS_LIFE, COMPANY)
        );
    }

    @DisplayName("일반 글 작성 실패 - 해당 게시판에 일반글 작성 권한이 없는경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnNormal")
    void FAIL_writeNormalPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getUsername())
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
    void SUCCESS_writeAnnouncementPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getUsername())
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
                Arguments.of(CAMPUS_LIFE, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(CAMPUS_LIFE, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(CAMPUS_LIFE, STUDENT),
                Arguments.of(CAMPUS_LIFE, COMPANY)
        );
    }

    @DisplayName("일반 공지글 작성 실패 - 공지글 작성 권한이 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("FAIL_provideRoleAndBoardTypeForCheckingWritePermissionOnAnnouncement")
    void FAIL_writeAnnouncementPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        CreateNormalPostServiceRequest request = CreateNormalPostServiceRequest.builder()
                .username(member.getUsername())
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
                .username(member.getUsername())
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
                .username(member.getUsername())
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
                .username(member.getUsername())
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
                .username(member.getUsername())
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
                .username(member.getUsername())
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

    @DisplayName("모집글 작성 실패 - 존재하지 않는 게시판에 글을 작성할수 없다")
    @Test
    void writeRecruitmentPostInNotExistingBoard() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createAdminMember(university);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .username(member.getUsername())
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
        Member member = memberFactory.createCompanyMember("카카오", university);

        CreateRecruitmentPostServiceRequest request = CreateRecruitmentPostServiceRequest.builder()
                .username(member.getUsername())
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
                Arguments.of(EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(STUDENT),
                Arguments.of(COMPANY),
                Arguments.of(STUDENT_COUNCIL),
                Arguments.of(ADMIN)
        );
    }

    @DisplayName("글 작성 규정 동의 여부 확인 - 동의 안한 경우")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("providingRoleForCheckingMemberAcceptedWritePolicy")
    void False_checkMemberAcceptedWritePolicy(MemberRole memberRole) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyNotAccepted(university, memberRole);

        //when
        boolean agreedOnWritePolicy = postService.checkMemberAcceptedWritePolicy(member.getUsername());

        //then
        assertThat(agreedOnWritePolicy).isFalse();
    }

    @DisplayName("글 작성 규정 동의 여부 확인 - 동의한 경우")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("providingRoleForCheckingMemberAcceptedWritePolicy")
    void True_checkMemberAcceptedWritePolicy(MemberRole memberRole) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        //when
        boolean agreedOnWritePolicy = postService.checkMemberAcceptedWritePolicy(member.getUsername());

        //then
        assertThat(agreedOnWritePolicy).isTrue();
    }

    public static Stream<Arguments> providingRoleForMemberAcceptingWritePolicy() {
        return Stream.of(
                Arguments.of(EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(STUDENT),
                Arguments.of(COMPANY),
                Arguments.of(STUDENT_COUNCIL),
                Arguments.of(ADMIN)
        );
    }

    @DisplayName("글 작성 규정 동의함 요청 성공")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("providingRoleForMemberAcceptingWritePolicy")
    void Success_AcceptingWritePolicy(MemberRole memberRole) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyNotAccepted(university, memberRole);

        //when
        postService.acceptWritePolicy(member.getUsername());

        //then
        Member findMember = memberRepository.findByUsername(member.getUsername()).get();
        assertThat(findMember.getStatus().isPolicyAccepted()).isTrue();
    }

    @DisplayName("글 작성 규정 동의함 요청 실패 - 해당 회원이 탈퇴 회원인 경우")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("providingRoleForMemberAcceptingWritePolicy")
    void Fail_AcceptingWritePolicyByDeletedMember(MemberRole memberRole) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyNotAccepted(university, memberRole);
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
    void SUCCESS_checkMemberHasWritePolicy(BoardType boardType, PostType postType, MemberRole memberRole) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        //when //then
        assertThat(postService.checkMemberHasWritePermission(board.getId(),
                member.getUsername(), postType)).isTrue();
    }

    public static Stream<Arguments> Fail_provideRoleAndBoardTypeAndPostTypeForCheckingMemberHasWritePolicy() {

        return Stream.of(
                Arguments.of(EMPLOYMENT, NORMAL, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(EMPLOYMENT, NORMAL, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(EMPLOYMENT, NORMAL, STUDENT_COUNCIL),
                Arguments.of(EMPLOYMENT, RECRUITMENT, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(EMPLOYMENT, RECRUITMENT, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(EMPLOYMENT, RECRUITMENT, STUDENT),
                Arguments.of(EMPLOYMENT, RECRUITMENT, STUDENT_COUNCIL),

                Arguments.of(ACTIVITY, NORMAL, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(ACTIVITY, NORMAL, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(ACTIVITY, NORMAL, STUDENT_COUNCIL),
                Arguments.of(ACTIVITY, RECRUITMENT, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(ACTIVITY, RECRUITMENT, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(ACTIVITY, RECRUITMENT, STUDENT_COUNCIL),

                Arguments.of(CLUB, NORMAL, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(CLUB, NORMAL, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(CLUB, NORMAL, STUDENT_COUNCIL),
                Arguments.of(CLUB, NORMAL, COMPANY),

                Arguments.of(ANONYMOUS, NORMAL, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(ANONYMOUS, NORMAL, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(ANONYMOUS, NORMAL, STUDENT_COUNCIL),
                Arguments.of(ANONYMOUS, NORMAL, COMPANY),

                Arguments.of(CAMPUS_LIFE, NORMAL, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(CAMPUS_LIFE, NORMAL, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(CAMPUS_LIFE, NORMAL, COMPANY),
                Arguments.of(CAMPUS_LIFE, ANNOUNCEMENT, EMAIL_UNCERTIFIED_STUDENT),
                Arguments.of(CAMPUS_LIFE, ANNOUNCEMENT, EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(CAMPUS_LIFE, ANNOUNCEMENT, STUDENT),
                Arguments.of(CAMPUS_LIFE, ANNOUNCEMENT, COMPANY)
        );
    }

    @DisplayName("해당 게시판에 글 작성 권한 있는지 여부 확인 - 권한 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 회원 유형 : {1}")
    @MethodSource("Fail_provideRoleAndBoardTypeAndPostTypeForCheckingMemberHasWritePolicy")
    void Fail_checkMemberHasWritePolicy(BoardType boardType, PostType postType, MemberRole memberRole) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        //when //then
        assertThatThrownBy(() -> postService.checkMemberHasWritePermission(board.getId(),
                member.getUsername(), postType))
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
    void Success_EditNormalPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        Post post = postFactory.createNormalPost(member, board);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username(member.getUsername())
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
    void Fail_EditNormalPostByNotExistingMember(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
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
    void Fail_EditNormalPostByDeletedMember(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createNormalPost(member, board);

        memberRepository.delete(member);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username(member.getUsername())
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
    void Fail_EditNotExistingNormalPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username(member.getUsername())
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
    void Fail_EditDeletedNormalPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createNormalPost(member, board);

        postService.deletePost(post.getId(), member.getUsername());

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username(member.getUsername())
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
    void Fail_InvalidRequestToEditRecruitmentPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createRecruitmentPost(member, board);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username(member.getUsername())
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
    void Fail_EditNormalPostWroteByOtherMember(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createStudentMember("member", university);

        EditNormalPostServiceRequest request = EditNormalPostServiceRequest.builder()
                .username(member.getUsername())
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
    void Success_EditRecruitPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        Post post = postFactory.createRecruitmentPost(member, board);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .username(member.getUsername())
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
    void Fail_EditRecruitmentPostByNotExistingMember(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
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
    @MethodSource("possibleCombinationToWriteRecruitmentPost")
    void Fail_EditRecruitmentPostByDeletedMember(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
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
    @MethodSource("possibleCombinationToWriteRecruitmentPost")
    void Fail_EditNotExistingRecruitmentPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .username(member.getUsername())
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
    void Fail_EditDeletedRecruitmentPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createRecruitmentPost(member, board);

        postService.deletePost(post.getId(), member.getUsername());

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .username(member.getUsername())
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
    void Fail_InvalidRequestToEditNormalPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createNormalPost(member, board);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .username(member.getUsername())
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
    void Fail_EditRecruitmentPostWroteByOtherMember(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createRecruitmentPost(postMember, board);

        Member member = memberFactory.createStudentMember("member", university);

        EditRecruitmentPostServiceRequest request = EditRecruitmentPostServiceRequest.builder()
                .username(member.getUsername())
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
    void Success_DeletePost(BoardType boardType, PostType postType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createPost(member, board, postType);

        //when
        postService.deletePost(post.getId(), member.getUsername());

        //then
        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(findPost.isDeleted()).isTrue();
    }

    @DisplayName("글 삭제 성공 - 관리자는 타인이 작성한 글도 삭제할수 있다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("possibleCombinationToWritePost")
    void Success_DeletePostByAdmin(BoardType boardType, PostType postType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createPost(member, board, postType);

        Member admin = memberFactory.createAdminMember(university);

        //when
        postService.deletePost(post.getId(), admin.getUsername());

        //then
        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(findPost.isDeleted()).isTrue();
    }

    @DisplayName("글 삭제 실패 - 글 작성자를 찾을수 없는 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("possibleCombinationToWritePost")
    void Fail_DeletePostByNotExistingMember(BoardType boardType, PostType postType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createPost(member, board, postType);

        //when //then
        assertThatThrownBy(() -> postService.deletePost(post.getId(), "aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("글 삭제 실패 - 글 작성자가 회원 탈퇴를 한 경우")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("possibleCombinationToWritePost")
    void Fail_DeletePostByDeletedMember(BoardType boardType, PostType postType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createPost(member, board, postType);

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> postService.deletePost(post.getId(), member.getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }


    public static Stream<Arguments> existingMemberRole() {

        return Stream.of(
                Arguments.of(STUDENT),
                Arguments.of(STUDENT_COUNCIL),
                Arguments.of(ADMIN),
                Arguments.of(COMPANY),
                Arguments.of(EMAIL_UNCERTIFIED_COMPANY),
                Arguments.of(EMAIL_UNCERTIFIED_STUDENT)
        );
    }


    @DisplayName("글 삭제 실패 - 존재하지 않는글을 삭제할수 없다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("existingMemberRole")
    void Fail_DeleteNotExistingPost(MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        //when //then
        assertThatThrownBy(() -> postService.deletePost(-10L, member.getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("글 삭제 실패 - 이미 삭제된 글을 삭제 시도 할수 없다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("possibleCombinationToWritePost")
    void Fail_DeletePostThatIsAlreadyDeleted(BoardType boardType, PostType postType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member member = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createPost(member, board, postType);

        postService.deletePost(post.getId(), member.getUsername());

        //when //then
        assertThatThrownBy(() -> postService.deletePost(post.getId(), member.getUsername()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("글 삭제 실패 - 내가 작성하지 않은글을 삭제할수 없다")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 유형 : {1}, 회원 유형 : {2}")
    @MethodSource("possibleCombinationToWritePost")
    void Fail_DeletePostThatIsNotMine(BoardType boardType, PostType postType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member postMember = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);
        Post post = postFactory.createPost(postMember, board, postType);

        Member member = memberFactory.createStudentMember("member", university);

        //when //then
        assertThatThrownBy(() -> postService.deletePost(post.getId(), member.getUsername()))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }

    @DisplayName("모집 글 단건 조회")
    @ParameterizedTest(name = "{index} 게시판 유형: {0}, 글 작성 회원 유형 : {1}")
    @MethodSource("possibleCombinationToWriteRecruitmentPost")
    void viewSingleRecruitmentPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member postMember = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        Post post = postFactory.createPost(postMember, board, RECRUITMENT);

        Member member = memberFactory.createStudentMember("member", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getUsername());

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

        Member postMember = memberFactory.createCompanyMember("구글", university);

        Post post = postFactory.createPost(postMember, board, RECRUITMENT);

        Member member = memberFactory.createStudentMember("member", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getUsername());

        //then
        assertThat(response.getWriter()).isEqualTo(postMember.getCompany().getName());
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
    void viewRecruitmentPostWriterColumByNonCompanyMember(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member postMember = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        Post post = postFactory.createPost(postMember, board, RECRUITMENT);

        Member member = memberFactory.createStudentMember("member", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getUsername());

        //then
        assertThat(response.getWriter()).isEqualTo(postMember.getName().getNickname());
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
    void viewSingleNormalPost(MemberRole memberRole) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);

        Member postMember = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        Post post = postFactory.createPost(postMember, board, NORMAL);

        Member member = memberFactory.createStudentMember("member", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getUsername());

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
    void writerColumOnAnonymousPostIsNull(MemberRole memberRole) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);

        Member postMember = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        Post post = postFactory.createPost(postMember, board, NORMAL);

        Member member = memberFactory.createStudentMember("member", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getUsername());

        //then
        assertThat(response.getWriter()).isNull();
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
    void viewNonAnonymousNormalPost(BoardType boardType, MemberRole memberRole) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);

        Member postMember = memberFactory.parameterizedTest_PolicyAccepted(university, memberRole);

        Post post = postFactory.createPost(postMember, board, NORMAL);

        Member member = memberFactory.createStudentMember("member", university);

        //when
        SinglePostResponse response = postService.loadSinglePost(post.getId(),
                member.getUsername());

        //then
        assertThat(response).extracting("postId", "title", "commentCount",
                        "likeCount", "viewCount", "thumbnailUrl", "boardName", "boardId",
                        "postType", "writer", "content")
                .containsExactly(post.getId(), post.getTitle(), post.getCommentMemberCount(), 0, 1,
                        post.getThumbnailUrl(), board.getBoardName(), board.getId(),
                        post.getPostType().toString(), postMember.getName().getNickname(),
                        post.getContent());
    }
}
