package com.plana.infli.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Comment;
import com.plana.infli.domain.CommentLike;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.University;
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
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class CommentLikeServiceTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

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
    private CommentLikeService commentLikeService;

    @AfterEach
    void tearDown() {
        commentLikeRepository.deleteAllInBatch();
        commentRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }

    @DisplayName("댓글 좋아요 누르기")
    @Test
    void commentLike() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        commentLikeService.createCommentLike(
                member.getLoginCredentials().getUsername(), comment.getId());

        //then
        assertThat(commentLikeRepository.existsByMemberAndComment(member, comment)).isTrue();
    }


    @DisplayName("존재하지 않는 회원의 명의로 댓글 좋아요를 누를수 없다")
    @Test
    void commentLikeByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        //when //then
        assertThatThrownBy(() -> commentLikeService.createCommentLike("aaa", comment.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("탈퇴한 회원의 명의로 댓글 좋아요를 누를수 없다")
    @Test
    void commentLikeByDeletedMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        memberRepository.delete(member);


        //when //then
        assertThatThrownBy(() -> commentLikeService.createCommentLike(
                member.getLoginCredentials().getUsername(), comment.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("존재하지 않는 댓글에 좋아요를 누를수 없다")
    @Test
    void pressLikeOnNotExistingComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when //then
        assertThatThrownBy(() -> commentLikeService.createCommentLike(
                member.getLoginCredentials().getUsername(), -1L))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }


    @DisplayName("삭제된 댓글에 좋아요를 누를수 없다")
    @Test
    void pressLikeOnDeletedComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        commentRepository.delete(comment);

        //when //then
        assertThatThrownBy(() -> commentLikeService.createCommentLike(
                member.getLoginCredentials().getUsername(),
                comment.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("댓글이 작성된 글이 삭제된 경우, 해당 댓글에 좋아요를 누를수 없다")
    @Test
    void commentLikeOnDeletedPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Member postMember = memberFactory.createVerifiedStudentMember("postMember", university);
        Post post = postFactory.createNormalPost(postMember, board);

        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        postService.deletePost(post.getId(), postMember.getLoginCredentials().getUsername());

        //when //then
        assertThatThrownBy(() -> commentLikeService.createCommentLike(
                member.getLoginCredentials().getUsername(), comment.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");

    }

    /**
     * 좋아요 취소
     */

    @DisplayName("댓글 좋아요 취소")
    @Test
    void cancelCommentLike() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        CommentLike commentLike = commentLikeFactory.createCommentLike(member, comment);

        //when
        commentLikeService.cancelCommentLike(member.getLoginCredentials().getUsername(),
                comment.getId());

        //then
        assertThat(commentLikeRepository.count()).isZero();
    }

    @DisplayName("좋아요를 누르지 않은 댓글에 좋아요 취소를 누를수 없다")
    @Test
    void cancelCommentLikeThatIDidNotPressLike() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike(
                member.getLoginCredentials().getUsername(), comment.getId()))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("해당 댓글에 좋아요를 누르지 않았습니다");

    }

    @DisplayName("이미 취소한 좋아요를 다시한번 취소할수 없다")
    @TestFactory
    Collection<DynamicTest> cancelCommentLikeThatIAlreadyCanceled() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        commentLikeFactory.createCommentLike(member, comment);

        return List.of(
                dynamicTest("좋아요 취소 실행",
                        () -> {
                            commentLikeService.cancelCommentLike(
                                    member.getLoginCredentials().getUsername(), comment.getId());
                        }),

                dynamicTest("다시한번 좋아요 취소할 경우 예외 발생",
                        () -> {
                            //when //then
                            assertThatThrownBy(() -> commentLikeService.cancelCommentLike(
                                    member.getLoginCredentials().getUsername(), comment.getId()))
                                    .isInstanceOf(BadRequestException.class)
                                    .message().isEqualTo("해당 댓글에 좋아요를 누르지 않았습니다");

                        })
        );
    }

    @DisplayName("타인이 누른 댓글좋아요를 내가 취소할수 없다")
    @Test
    void cancelCommentLikeByOtherMember() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        commentLikeFactory.createCommentLike(
                memberFactory.createVerifiedStudentMember("likeMember", university), comment);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike(
                member.getLoginCredentials().getUsername(), comment.getId()))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("해당 댓글에 좋아요를 누르지 않았습니다");

    }

    @DisplayName("이미 삭제된 댓글에 눌린 좋아요를 취소할수 없다")
    @Test
    void cancelCommentLikeWhenCommentIsDeleted() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        commentLikeFactory.createCommentLike(member, comment);

        commentRepository.delete(comment);

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike(
                member.getLoginCredentials().getUsername(), comment.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("댓글 좋아요 취소 - 댓글이 작성된 글이 삭제된 경우 댓글 좋아요 취소를 할수 없다")
    @Test
    void cancelCommentLikeWhenPostIsDeleted() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Member postMember = memberFactory.createVerifiedStudentMember("postMember", university);
        Post post = postFactory.createNormalPost(postMember, board);

        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        commentLikeFactory.createCommentLike(member, comment);

        postService.deletePost(post.getId(), postMember.getLoginCredentials().getUsername());

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike(
                member.getLoginCredentials().getUsername(), comment.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");

    }


    @DisplayName("존재하지 않는 회원은 댓글 좋아요 취소를 할수 있다")
    @Test
    void cancelCommentLikeByNotExistingMember() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        commentLikeFactory.createCommentLike(member, comment);

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike("aaa", comment.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("존재하지 않는 댓글의 좋아요 취소를 할수 없다")
    @Test
    void commentIdIsMandatory() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike(
                member.getLoginCredentials().getUsername(), -1L))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }
}
