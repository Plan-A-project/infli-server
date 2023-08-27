package com.plana.infli.service;

import static com.plana.infli.domain.type.BoardType.ACTIVITY;
import static com.plana.infli.domain.type.BoardType.ANONYMOUS;
import static com.plana.infli.domain.type.BoardType.CAMPUS_LIFE;
import static com.plana.infli.domain.type.BoardType.CLUB;
import static com.plana.infli.domain.type.BoardType.EMPLOYMENT;
import static com.plana.infli.domain.type.PostType.ANNOUNCEMENT;
import static com.plana.infli.domain.type.PostType.NORMAL;
import static com.plana.infli.domain.type.PostType.RECRUITMENT;
import static com.plana.infli.domain.type.Role.ADMIN;
import static com.plana.infli.domain.type.Role.COMPANY;
import static com.plana.infli.domain.type.Role.STUDENT;
import static com.plana.infli.domain.type.Role.STUDENT_COUNCIL;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostLike;
import com.plana.infli.domain.University;
import com.plana.infli.domain.type.BoardType;
import com.plana.infli.domain.type.PostType;
import com.plana.infli.domain.type.Role;
import com.plana.infli.infra.exception.custom.AuthorizationFailedException;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.factory.BoardFactory;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.PostFactory;
import com.plana.infli.factory.PostLikeFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.postlike.PostLikeRepository;
import com.plana.infli.repository.university.UniversityRepository;
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
class PostLikeServiceTest {

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private PostService postService;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private BoardFactory boardFactory;

    @Autowired
    private PostFactory postFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private PostLikeFactory postLikeFactory;

    @AfterEach
    void tearDown() {
        postLikeRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }


    public static Stream<Arguments> allowedCombinationToWritePost() {

        return Stream.of(
                Arguments.of(STUDENT, EMPLOYMENT, NORMAL),
                Arguments.of(STUDENT, ACTIVITY, NORMAL),
                Arguments.of(STUDENT, ACTIVITY, RECRUITMENT),
                Arguments.of(STUDENT, CLUB, NORMAL),
                Arguments.of(STUDENT, ANONYMOUS, NORMAL),
                Arguments.of(STUDENT, CAMPUS_LIFE, NORMAL),

                Arguments.of(STUDENT_COUNCIL, CAMPUS_LIFE, NORMAL),
                Arguments.of(STUDENT_COUNCIL, CAMPUS_LIFE, ANNOUNCEMENT),

                Arguments.of(COMPANY, ACTIVITY, NORMAL),
                Arguments.of(COMPANY, ACTIVITY, RECRUITMENT),
                Arguments.of(COMPANY, EMPLOYMENT, NORMAL),
                Arguments.of(COMPANY, EMPLOYMENT, RECRUITMENT),

                Arguments.of(ADMIN, EMPLOYMENT, NORMAL),
                Arguments.of(ADMIN, EMPLOYMENT, RECRUITMENT),
                Arguments.of(ADMIN, ANONYMOUS, NORMAL),
                Arguments.of(ADMIN, ACTIVITY, NORMAL),
                Arguments.of(ADMIN, ACTIVITY, RECRUITMENT),
                Arguments.of(ADMIN, CLUB, NORMAL),
                Arguments.of(ADMIN, CAMPUS_LIFE, NORMAL),
                Arguments.of(ADMIN, CAMPUS_LIFE, ANNOUNCEMENT)
        );
    }

    @DisplayName("학생 회원이 글 좋아요 누르기 성공")
    @ParameterizedTest(name = "{index} 글 작성자 회원 유형 : {0}, 게시판 유형 : {1}, 글 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void pressPostLikeByStudent(Role role, BoardType boardType, PostType postType) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(postMember, board, postType);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        postLikeService.pressPostLike(member.getLoginCredentials().getUsername(), post.getId());

        //then
        assertThat(postLikeRepository.findAllByPostAndMember(post, member)).isNotEmpty();
    }

    @DisplayName("기업 회원이 글 좋아요 누르기 성공")
    @ParameterizedTest(name = "{index} 글 작성자 회원 유형 : {0}, 게시판 유형 : {1}, 글 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void pressPostLikeByCompany(Role role, BoardType boardType, PostType postType) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(postMember, board, postType);

        Member member = memberFactory.createVerifiedCompanyMember(university);

        //when
        postLikeService.pressPostLike(member.getLoginCredentials().getUsername(), post.getId());

        //then
        assertThat(postLikeRepository.findAllByPostAndMember(post, member)).isNotEmpty();
    }


    @DisplayName("관리자 회원이 글 좋아요 누르기 성공")
    @ParameterizedTest(name = "{index} 글 작성자 회원 유형 : {0}, 게시판 유형 : {1}, 글 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void pressPostLikeByAdmin(Role role, BoardType boardType, PostType postType) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(postMember, board, postType);

        Member member = memberFactory.createAdminMember(university);

        //when
        postLikeService.pressPostLike(member.getLoginCredentials().getUsername(), post.getId());

        //then
        assertThat(postLikeRepository.findAllByPostAndMember(post, member)).isNotEmpty();
    }

    @DisplayName("학생회 회원이 글 좋아요 누르기 성공")
    @ParameterizedTest(name = "{index} 글 작성자 회원 유형 : {0}, 게시판 유형 : {1}, 글 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void pressPostLikeByStudentCouncil(Role role, BoardType boardType, PostType postType) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(postMember, board, postType);

        Member member = memberFactory.createStudentCouncilMember(university);

        //when
        postLikeService.pressPostLike(member.getLoginCredentials().getUsername(), post.getId());

        //then
        assertThat(postLikeRepository.findAllByPostAndMember(post, member)).isNotEmpty();
    }

    @DisplayName("글 좋아요 누르기 실패 - 좋아요를 누를 회원이 존재하지 않은 경우")
    @Test
    void pressPostLikeByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentCouncilMember(university);
        Post post = postFactory.createNormalPost(postMember, board);

        //when //then
        assertThatThrownBy(() -> postLikeService.pressPostLike("aaa", post.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("글 좋아요 누르기 실패 - 글을 누를 회원이 탈퇴한 회원인 경우")
    @Test
    void pressPostLikeByNotDeletedMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentCouncilMember(university);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(
                () -> postLikeService.pressPostLike(
                        member.getLoginCredentials().getUsername(), post.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("글 좋아요 누르기 실패 - 존재하지 않는 글을 좋아요 누르는 경우")
    @Test
    void pressLikeOnNotExistingPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when //then
        assertThatThrownBy(
                () -> postLikeService.pressPostLike(member.getLoginCredentials().getUsername(), 0L))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("글 좋아요 누르기 실패 - 좋아요를 누를 글이 삭제된 경우")
    @Test
    void pressLikeOnDeletedPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentCouncilMember(university);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        postService.deletePost(post.getId(), postMember.getLoginCredentials().getUsername());

        //when //then
        assertThatThrownBy(
                () -> postLikeService.pressPostLike(
                        member.getLoginCredentials().getUsername(), post.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("글 좋아요 누르기 실패 - 해당 글이 내가 소속된 대학교가 아닌 다른 대학에서 작성된 경우")
    @Test
    void pressLikeOnOtherUniversityPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentCouncilMember(university);
        Post post = postFactory.createNormalPost(postMember, board);

        University otherUniversity = universityFactory.createUniversity("서울대학교");
        Member member = memberFactory.createVerifiedStudentMember("member", otherUniversity);

        //when //then
        assertThatThrownBy(
                () -> postLikeService.pressPostLike(
                        member.getLoginCredentials().getUsername(), post.getId()))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }

    @DisplayName("글 좋아요 누르기 실패 - 특정글에 좋아요는 한번만 누를수 있다")
    @Test
    void pressLikeOnSinglePostTwiceIsNotAllowed() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentCouncilMember(university);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        postLikeService.pressPostLike(member.getLoginCredentials().getUsername(), post.getId());

        //when //then
        assertThatThrownBy(
                () -> postLikeService.pressPostLike(
                        member.getLoginCredentials().getUsername(), post.getId()))
                .isInstanceOf(ConflictException.class)
                .message().isEqualTo("이미 해당 글에 좋아요를 눌렀습니다");
    }

    @DisplayName("학생 회원이 자신이 좋아요 누른글 좋아요 취소")
    @ParameterizedTest(name = "{index} 글 작성자 회원 유형 : {0}, 게시판 유형 : {1}, 글 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void cancelPostLikeByStudent(Role role, BoardType boardType, PostType postType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(postMember, board, postType);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        PostLike postLike = postLikeFactory.createPostLike(member, post);

        //when
        postLikeService.cancelPostLike(member.getLoginCredentials().getUsername(), post.getId());

        //then
        assertThat(postLikeRepository.findAllByPostAndMember(post, member)).isEmpty();
    }

    @DisplayName("기업 회원이 자신이 좋아요 누른글 좋아요 취소")
    @ParameterizedTest(name = "{index} 글 작성자 회원 유형 : {0}, 게시판 유형 : {1}, 글 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void cancelPostLikeByCompany(Role role, BoardType boardType, PostType postType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(postMember, board, postType);

        Member member = memberFactory.createVerifiedCompanyMember(university);
        PostLike postLike = postLikeFactory.createPostLike(member, post);

        //when
        postLikeService.cancelPostLike(member.getLoginCredentials().getUsername(), post.getId());

        //then
        assertThat(postLikeRepository.findAllByPostAndMember(post, member)).isEmpty();
    }

    @DisplayName("학생회 회원이 자신이 좋아요 누른글 좋아요 취소")
    @ParameterizedTest(name = "{index} 글 작성자 회원 유형 : {0}, 게시판 유형 : {1}, 글 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void cancelPostLikeByStudentCouncil(Role role, BoardType boardType, PostType postType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(postMember, board, postType);

        Member member = memberFactory.createStudentCouncilMember(university);
        PostLike postLike = postLikeFactory.createPostLike(member, post);

        //when
        postLikeService.cancelPostLike(member.getLoginCredentials().getUsername(), post.getId());

        //then
        assertThat(postLikeRepository.findAllByPostAndMember(post, member)).isEmpty();
    }

    @DisplayName("관리자 회원이 자신이 좋아요 누른글 좋아요 취소")
    @ParameterizedTest(name = "{index} 글 작성자 회원 유형 : {0}, 게시판 유형 : {1}, 글 유형 : {2}")
    @MethodSource("allowedCombinationToWritePost")
    void cancelPostLikeByAdmin(Role role, BoardType boardType, PostType postType) {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createByBoardType(university, boardType);
        Member postMember = memberFactory.createPolicyAcceptedMemberWithRole(university, role);
        Post post = postFactory.createPost(postMember, board, postType);

        Member member = memberFactory.createAdminMember(university);
        PostLike postLike = postLikeFactory.createPostLike(member, post);

        //when
        postLikeService.cancelPostLike(member.getLoginCredentials().getUsername(), post.getId());

        //then
        assertThat(postLikeRepository.findAllByPostAndMember(post, member)).isEmpty();
    }

    @DisplayName("글 좋아요 취소 실패 - 해당글에 좋아요를 누른적이 없는 경우")
    @Test
    void cancelLikeOnPostThatIDidNotPressedLike() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentCouncilMember(university);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when //then
        assertThatThrownBy(
                () -> postLikeService.cancelPostLike(
                        member.getLoginCredentials().getUsername(), post.getId()))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("해당 글에 좋아요를 누르지 않았습니다");
    }

    @DisplayName("글 좋아요 취소 실패 - 좋아요 취소를 요청한 회원이 존재하지 않는 경우")
    @Test
    void cancelPostLikeByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentCouncilMember(university);
        Post post = postFactory.createNormalPost(postMember, board);

        //when //then
        assertThatThrownBy(
                () -> postLikeService.cancelPostLike("aaa", post.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("글 좋아요 취소 실패 - 좋아요 취소를 요청한 회원이 탈퇴한 회원인 경우")
    @Test
    void cancelPostLikeByDeletedMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentCouncilMember(university);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(
                () -> postLikeService.cancelPostLike(
                        member.getLoginCredentials().getUsername(), post.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("글 좋아요 취소 실패 - 좋아요 취소를 할 글이 존재하지 않는 경우")
    @Test
    void cancelLikeOnNotExistingPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when //then
        assertThatThrownBy(
                () -> postLikeService.cancelPostLike(
                        member.getLoginCredentials().getUsername(), -1L))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("글 좋아요 취소 실패 - 좋아요 취소를 할 글이 이미 삭제된 경우")
    @Test
    void cancelLikeOnDeletedPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentCouncilMember(university);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        postService.deletePost(post.getId(), postMember.getLoginCredentials().getUsername());

        //when //then
        assertThatThrownBy(
                () -> postLikeService.cancelPostLike(
                        member.getLoginCredentials().getUsername(), post.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }
}
