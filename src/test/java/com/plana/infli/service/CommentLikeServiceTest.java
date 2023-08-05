package com.plana.infli.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Comment;
import com.plana.infli.domain.CommentLike;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.University;
import com.plana.infli.exception.custom.AuthenticationFailedException;
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
import com.plana.infli.web.dto.request.commentlike.cancel.CancelCommentLikeServiceRequest;
import com.plana.infli.web.dto.request.commentlike.create.CreateCommentLikeServiceRequest;
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
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("member", university);
        CreateCommentLikeServiceRequest request = CreateCommentLikeServiceRequest.builder()
                .email(member.getEmail())
                .commentId(comment.getId())
                .postId(post.getId())
                .build();

        //when
        Long commentLikeId = commentLikeService.createCommentLike(request);

        //then
        CommentLike like = commentLikeRepository.findWithCommentAndMemberById(commentLikeId).get();
        assertThat(like.getComment().getId()).isEqualTo(comment.getId());
        assertThat(like.getMember().getId()).isEqualTo(member.getId());
    }


    @DisplayName("존재하지 않는 회원의 명의로 댓글 좋아요를 누를수 없다")
    @Test
    void commentLikeByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        CreateCommentLikeServiceRequest request = CreateCommentLikeServiceRequest.builder()
                .email("aaaa")
                .commentId(comment.getId())
                .postId(post.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentLikeService.createCommentLike(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("탈퇴한 회원의 명의로 댓글 좋아요를 누를수 없다")
    @Test
    void commentLikeByDeletedMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("member", university);
        memberRepository.delete(member);

        CreateCommentLikeServiceRequest request = CreateCommentLikeServiceRequest.builder()
                .email(member.getEmail())
                .commentId(comment.getId())
                .postId(post.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentLikeService.createCommentLike(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("존재하지 않는 댓글에 좋아요를 누를수 없다")
    @Test
    void pressLikeOnNotExistingComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("member", university);
        CreateCommentLikeServiceRequest request = CreateCommentLikeServiceRequest.builder()
                .email(member.getEmail())
                .commentId(99L)
                .postId(post.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentLikeService.createCommentLike(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }


    @DisplayName("삭제된 댓글에 좋아요를 누를수 없다")
    @Test
    void pressLikeOnDeletedComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("member", university);
        commentRepository.delete(comment);
        CreateCommentLikeServiceRequest request = CreateCommentLikeServiceRequest.builder()
                .email(member.getEmail())
                .commentId(99L)
                .postId(post.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentLikeService.createCommentLike(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("댓글이 작성된 글이 삭제된 경우, 해당 댓글에 좋아요를 누를수 없다")
    @Test
    void commentLikeOnDeletedPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Member postMember = memberFactory.createStudentMember("postMember", university);
        Post post = postFactory.createPost(postMember, board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("member", university);

        postService.deletePost(post.getId(), postMember.getEmail());
        CreateCommentLikeServiceRequest request = CreateCommentLikeServiceRequest.builder()
                .email(member.getEmail())
                .commentId(comment.getId())
                .postId(post.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentLikeService.createCommentLike(request))
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
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("member", university);

        CommentLike commentLike = commentLikeFactory.createCommentLike(member, comment);

        CancelCommentLikeServiceRequest request = CancelCommentLikeServiceRequest
                .builder()
                .email(member.getEmail())
                .commentId(comment.getId())
                .postId(post.getId())
                .build();

        //when
        commentLikeService.cancelCommentLike(request);

        //then
        assertThat(commentLikeRepository.count()).isZero();
    }

    @DisplayName("좋아요를 누르지 않은 댓글에 좋아요 취소를 누를수 없다")
    @Test
    void cancelCommentLikeThatIDidNotPressLike() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("member", university);

        CancelCommentLikeServiceRequest request = CancelCommentLikeServiceRequest.builder()
                .email(member.getEmail())
                .commentId(comment.getId())
                .postId(post.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("해당 댓글에 좋아요를 누르지 않았습니다");

    }

    @DisplayName("이미 취소한 좋아요를 다시한번 취소할수 없다")
    @TestFactory
    Collection<DynamicTest> cancelCommentLikeThatIAlreadyCanceled() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("member", university);

        commentLikeFactory.createCommentLike(member, comment);

        return List.of(
                dynamicTest("좋아요 취소 실행",
                        () -> {
                            CancelCommentLikeServiceRequest request = CancelCommentLikeServiceRequest
                                    .builder()
                                    .email(member.getEmail())
                                    .commentId(comment.getId())
                                    .postId(post.getId())
                                    .build();

                            commentLikeService.cancelCommentLike(request);
                        }),

                dynamicTest("다시한번 좋아요 취소할 경우 예외 발생",
                        () -> {
                            //given
                            CancelCommentLikeServiceRequest request = CancelCommentLikeServiceRequest
                                    .builder()
                                    .email(member.getEmail())
                                    .commentId(comment.getId())
                                    .postId(post.getId())
                                    .build();

                            //when //then
                            assertThatThrownBy(() -> commentLikeService.cancelCommentLike(request))
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
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        commentLikeFactory.createCommentLike(
                memberFactory.createStudentMember("likeMember", university), comment);

        Member member = memberFactory.createStudentMember("member", university);

        CancelCommentLikeServiceRequest request = CancelCommentLikeServiceRequest
                .builder()
                .email(member.getEmail())
                .commentId(comment.getId())
                .postId(post.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("해당 댓글에 좋아요를 누르지 않았습니다");

    }

    @DisplayName("이미 삭제된 댓글에 눌린 좋아요를 취소할수 없다")
    @Test
    void cancelCommentLikeWhenCommentIsDeleted() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("member", university);
        commentLikeFactory.createCommentLike(member, comment);

        commentRepository.delete(comment);

        CancelCommentLikeServiceRequest request = CancelCommentLikeServiceRequest
                .builder()
                .email(member.getEmail())
                .commentId(comment.getId())
                .postId(post.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("댓글 좋아요 취소 - 댓글이 작성된 글이 취소된 경우 댓글 좋아요 취소를 할수 없다")
    @Test
    void cancelCommentLikeWhenPostIsDeleted() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Member postMember = memberFactory.createStudentMember("postMember", university);
        Post post = postFactory.createPost(postMember, board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("member", university);
        commentLikeFactory.createCommentLike(member, comment);

        postService.deletePost(post.getId(), postMember.getEmail());

        CancelCommentLikeServiceRequest request = CancelCommentLikeServiceRequest
                .builder()
                .email(member.getEmail())
                .commentId(comment.getId())
                .postId(post.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");

    }


    @DisplayName("존재하지 않는 회원은 댓글 좋아요 취소를 할수 있다")
    @Test
    void cancelCommentLikeByNotExistingMember() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("member", university);
        commentLikeFactory.createCommentLike(member, comment);

        CancelCommentLikeServiceRequest request = CancelCommentLikeServiceRequest
                .builder()
                .email("aaa")
                .commentId(comment.getId())
                .postId(post.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("존재하지 않는 댓글의 좋아요 취소를 할수 없다")
    @Test
    void commentIdIsMandatory() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("member", university);
        CancelCommentLikeServiceRequest request = CancelCommentLikeServiceRequest
                .builder()
                .email(member.getEmail())
                .commentId(999L)
                .postId(post.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("댓글 좋아요 취소 요청시 입력된 글의 Id 번호와 댓글이 작성된 글의 Id 번호는 일치해야 한다")
    @Test
    void postIdsMustMatch() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("member", university);
        commentLikeFactory.createCommentLike(member, comment);

        CancelCommentLikeServiceRequest request = CancelCommentLikeServiceRequest
                .builder()
                .email(member.getEmail())
                .commentId(comment.getId())
                .postId(999L)
                .build();

        //when //then
        assertThatThrownBy(() -> commentLikeService.cancelCommentLike(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");

    }
}
