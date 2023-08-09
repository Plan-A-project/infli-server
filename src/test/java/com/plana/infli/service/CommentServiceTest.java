package com.plana.infli.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.CommentLike;
import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
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
import com.plana.infli.web.dto.request.comment.create.CreateCommentServiceRequest;
import com.plana.infli.web.dto.request.comment.edit.EditCommentServiceRequest;
import com.plana.infli.web.dto.request.comment.view.post.LoadCommentsInPostServiceRequest;
import com.plana.infli.web.dto.response.comment.create.CreateCommentResponse;
import com.plana.infli.web.dto.response.comment.view.BestCommentResponse;
import com.plana.infli.web.dto.response.comment.view.mycomment.MyCommentsResponse;
import com.plana.infli.web.dto.response.comment.view.post.PostCommentsResponse;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
class CommentServiceTest {

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
     * 댓글 작성
     */

    @DisplayName("특정 글에 댓글을 작성하면 DB에 값이 저장된다")
    @Test
    void writeComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postmember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(null)
                .build();

        //when
        CreateCommentResponse response = commentService.createComment(request);

        //then
        Comment findComment = commentRepository.findById(response.getCommentId()).get();
        assertThat(commentRepository.findAllActiveCommentCount()).isEqualTo(1);
        assertThat(response)
                .extracting("commentId", "identifierNumber")
                .contains(findComment.getId(), findComment.getIdentifierNumber());
        assertThat(findComment.getStatus().isEdited()).isFalse();
    }

    @DisplayName("존재하지 않는 회원은 댓글을 작성할수 없다")
    @Test
    void writeCommentWithNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postmember", university), board);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email("aaa")
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(null)
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

        assertThat(commentRepository.findAllActiveCommentCount()).isEqualTo(0);
    }




    @DisplayName("탈퇴한 회원은 댓글을 작성할 수 없다")
    @Test
    void writeCommentByUnregisteredMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postmember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);
        memberRepository.delete(member);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(null)
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
        assertThat(commentRepository.findAllActiveCommentCount()).isEqualTo(0);
    }


    @DisplayName("최대 허용 댓글 길이는 500자 이다")
    @Test
    void maximumContentLengthIs500() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postmember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("하".repeat(500))
                .parentCommentId(null)
                .build();

        //when
        commentService.createComment(request);

        //then
        assertThat(commentRepository.findAllActiveCommentCount()).isEqualTo(1);
    }

    @DisplayName("최대 허용 댓글 길이는 500자 이다 2")
    @Test
    void contentLengthOver500() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postmember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("하".repeat(501))
                .parentCommentId(null)
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("최대 댓글 길이를 초과하였습니다");
        assertThat(commentRepository.findAllActiveCommentCount()).isEqualTo(0);
    }


    @DisplayName("존재하지 않는 글에 댓글을 작성할 수 없다")
    @Test
    void writeCommentOnNotExistingPostIsNotAllowed() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Member member = memberFactory.createStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(999L)
                .content("댓글입니다")
                .parentCommentId(null)
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
        assertThat(commentRepository.findAllActiveCommentCount()).isEqualTo(0);
    }

    @DisplayName("삭제된 글에 댓글을 작성할 수 없다")
    @Test
    void writeCommentOnDeletedPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postmember = memberFactory.createStudentMember("postmember", university);
        Post post = postFactory.createNormalPost(postmember, board);

        postService.deletePost(post.getId(), postmember.getEmail());
        Member member = memberFactory.createStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(null)
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
        assertThat(commentRepository.findAllActiveCommentCount()).isEqualTo(0);
    }

    @DisplayName("미인증 회원은 댓글을 작성할 수 없다")
    @Test
    void uncertifiedMemberCanNotWriteComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);

        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createUncertifiedStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(null)
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }

    @DisplayName("내가 소속된 대학이 아닌, 다른 대학에 작성된 글에 댓글을 달 수 없다")
    @Test
    void writeCommentOnOtherUniversity() {
        //given
        University otherUniversity = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(otherUniversity);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", otherUniversity), board);

        University myUniversity = universityFactory.createUniversity("서울대학교");

        Member member = memberFactory.createStudentMember("nickname", myUniversity);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(null)
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");
    }

    @DisplayName("글에 대댓글은 없고, 댓글만 있는 경우 식별자 번호 부여 시나리오")
    @TestFactory
    Collection<DynamicTest> identifierNumberAllocationDynamicTest() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentMember("postMember", university);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member1 = memberFactory.createStudentMember("nickname1", university);
        Member member2 = memberFactory.createStudentMember("nickname2", university);
        Member member3 = memberFactory.createStudentMember("nickname3", university);

        //when
        return List.of(
                dynamicTest("글에 작성된 댓글이 0개인 상태에서 특정 회원이 댓글을 작성한 경우, 식별자 번호 1번을 부여 받는다",
                        () -> {
                            //given
                            CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                                    .email(member1.getEmail())
                                    .postId(post.getId())
                                    .content("댓글입니다")
                                    .parentCommentId(null)
                                    .build();

                            //when
                            CreateCommentResponse response = commentService.createComment(request);

                            //then
                            assertThat(response.getIdentifierNumber()).isEqualTo(1);
                        }),

                dynamicTest("새로운 회원이 댓글을 작성할떄마다, 가장 최신 식별자 번호에서 1 증가된 번호를 부여받는다",
                        () -> {
                            //given
                            CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                                    .email(member2.getEmail())
                                    .postId(post.getId())
                                    .content("댓글입니다")
                                    .parentCommentId(null)
                                    .build();

                            //when
                            CreateCommentResponse response = commentService.createComment(request);

                            assertThat(response.getIdentifierNumber()).isEqualTo(2);
                        }),

                dynamicTest("글 작성자가 자신의 글에 댓글을 단 경우, 식별자 번호 0번을 부여받는다",
                        () -> {
                            CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                                    .email(postMember.getEmail())
                                    .postId(post.getId())
                                    .content("글 작성자가 남기는 댓글")
                                    .parentCommentId(null)
                                    .build();

                            CreateCommentResponse response = commentService.createComment(request);

                            assertThat(response.getIdentifierNumber()).isEqualTo(0);
                        }),

                dynamicTest("새로운 회원이 댓글을 작성할떄마다, 가장 최신 식별자 번호에서 1 증가된 번호를 부여받는다 2",
                        () -> {
                            //given
                            CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                                    .email(member3.getEmail())
                                    .postId(post.getId())
                                    .content("댓글입니다")
                                    .parentCommentId(null)
                                    .build();

                            //when
                            CreateCommentResponse response = commentService.createComment(request);

                            assertThat(response.getIdentifierNumber()).isEqualTo(3);
                        }),

                dynamicTest("해당 글에 댓글을 작성한 이력이 있는 경우, 댓글을 달더라도 전에 부여받은 동일한 식별자 번흐를 다시 부여받는다",
                        () -> {
                            //given
                            CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                                    .email(member2.getEmail())
                                    .postId(post.getId())
                                    .content("댓글입니다")
                                    .parentCommentId(null)
                                    .build();

                            //when
                            CreateCommentResponse response = commentService.createComment(request);

                            //then
                            assertThat(response.getIdentifierNumber()).isEqualTo(2);
                        }),

                dynamicTest("해당 글에 댓글을 작성한 이력이 있는 경우, 댓글을 달더라도 전에 부여받은 동일한 식별자 번흐를 다시 부여받는다 2",
                        () -> {
                            //given
                            CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                                    .email(member1.getEmail())
                                    .postId(post.getId())
                                    .content("댓글입니다")
                                    .parentCommentId(null)
                                    .build();

                            //when
                            CreateCommentResponse response = commentService.createComment(request);

                            //then
                            assertThat(response.getIdentifierNumber()).isEqualTo(1);
                        })
        );
    }

    @DisplayName("식별자 번호 동시성 테스트")
    @Test
    void identifierConcurrency() throws InterruptedException {
        //given
        University university = universityFactory.createUniversity("서울대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {

            int finalI = i;
            executorService.submit(() -> {

                try {
                    Member member = memberFactory.createStudentMember("" + finalI,
                            university);

                    commentService.createComment(CreateCommentServiceRequest.builder()
                            .email(member.getEmail())
                            .content("댓글")
                            .parentCommentId(null)
                            .postId(post.getId())
                            .build());

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(commentRepository.count()).isEqualTo(100);
        assertThat(findPost.getCommentMemberCount()).isEqualTo(100);
    }


    /**
     * 대댓글 작성
     */

    @DisplayName("대댓글 작성시 DB에 값이 저장된다")
    @Test
    void writeChildComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);
        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("대댓글입니다")
                .parentCommentId(parentComment.getId())
                .build();

        //when
        CreateCommentResponse response = commentService.createComment(request);

        //then
        assertThat(commentRepository.findAllActiveCommentCount()).isEqualTo(2);

        Comment findComment = commentRepository.findById(response.getCommentId()).get();

        assertThat(response)
                .extracting("commentId", "identifierNumber")
                .contains(findComment.getId(), findComment.getIdentifierNumber());

    }

    @DisplayName("존재하지 않는 회원은 대댓글을 작성할수 없다")
    @Test
    void writeChildCommentWithNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email("aaa")
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(parentComment.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }



    @DisplayName("최대 허용 대댓글 길이는 500자 이다")
    @Test
    void maximumChildCommentContentLengthIs500() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("하".repeat(500))
                .parentCommentId(parentComment.getId())
                .build();

        //when
        CreateCommentResponse response = commentService.createComment(request);

        //then
        Comment comment = commentRepository.findById(response.getCommentId()).get();
        assertThat(comment.getContent().length()).isEqualTo(500);
    }

    @DisplayName("최대 허용 대댓글 길이는 500자 이다 2")
    @Test
    void ChildCommentContentLengthOver500() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("하".repeat(501))
                .parentCommentId(parentComment.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("최대 댓글 길이를 초과하였습니다");

    }


    @DisplayName("삭제된 글에 대댓글을 작성할 수 없다")
    @Test
    void writeChildCommentOnDeletedPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentMember("postMember", university);
        Post post = postFactory.createNormalPost(postMember, board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        postService.deletePost(post.getId(), postMember.getEmail());

        Member member = memberFactory.createStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(parentComment.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("존재하지 않는 댓글에 대댓글을 작성할 수 없다")
    @Test
    void writeChildCommentOnNotExistingParentComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(999L)
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("삭제된 댓글에 대댓글을 작성할 수 없다")
    @Test
    void writeChildCommentOnDeletedParentComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        commentRepository.delete(parentComment);

        Member member = memberFactory.createStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(parentComment.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("대댓글에 대댓글을 작성할 수 없다")
    @Test
    void writeChildCommentOnChildComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("parentComment", university), post);

        Comment childComment = commentFactory.createChildComment(
                memberFactory.createStudentMember("childComment", university), post, parentComment);


        Member member = memberFactory.createStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(childComment.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("대댓글에는 자식댓글을 작성할수 없습니다");

    }

    @DisplayName("내가 소속된 대학이 아닌, 다른 대학에 작성된 글에 대댓글을 달 수 없다")
    @Test
    void writeChildCommentOnOtherUniversity() {
        //given
        University otherUniversity = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(otherUniversity);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", otherUniversity), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", otherUniversity), post);


        University myUniversity = universityFactory.createUniversity("서울대학교");

        Member member = memberFactory.createStudentMember("nickname", myUniversity);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(parentComment.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");

    }

    @DisplayName("미인증 회원은 대댓글을 작성할 수 없다")
    @Test
    void uncertifiedMemberCanNotWriteChildComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createUncertifiedStudentMember("nickname", university);

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .content("댓글입니다")
                .parentCommentId(parentComment.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");

    }

    //TODO 대댓글 identifier number 테스트 필요
    // TODO newParentComment 팩토리 메서드 identifier number

    /**
     * 댓글 수정
     */
    @DisplayName("댓글 내용을 수정하면 DB에 값이 변경된다")
    @Test
    void editCommentContent() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when
        commentService.editCommentContent(request);

        //then
        Comment findComment = commentRepository.findActiveCommentWithPostBy(comment.getId()).get();
        assertThat(findComment.getId()).isEqualTo(comment.getId());
        assertThat(findComment.getContent()).isEqualTo("수정된 댓글입니다");
        assertThat(findComment.getStatus().isEdited()).isTrue();

    }

    @DisplayName("댓글을 수정하더라도 부여받은 식별자 번호는 변하지 않는다")
    @TestFactory
    Collection<DynamicTest> editCommentDoesNotChangeIdentifierNumber() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        //when
        return List.of(
                dynamicTest("회원이 특정 글에 댓글을 작성한 경우 식별자 번호가 부여된다",
                        () -> {
                            //given
                            CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                                    .email(member.getEmail())
                                    .postId(post.getId())
                                    .content("댓글입니다")
                                    .parentCommentId(null)
                                    .build();

                            commentService.createComment(request);
                        }),

                dynamicTest("해당 댓글을 수정하더라도 식별자 번호는 변하지 않는다",
                        () -> {
                            //given
                            Comment comment = commentRepository.findAll().get(0);

                            EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                                    .email(member.getEmail())
                                    .postId(post.getId())
                                    .commentId(comment.getId())
                                    .content("수정된 댓글입니다")
                                    .build();

                            //when
                            commentService.editCommentContent(request);

                            //then
                            Comment findComment = commentRepository.findById(comment.getId()).get();
                            assertThat(findComment.getIdentifierNumber()).isEqualTo(
                                    comment.getIdentifierNumber());
                        })
        );
    }

    @DisplayName("수정된 댓글의 길이는 500자 이하여야 한다")
    @Test
    void editCommentMaximumContentLengthIs500() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(comment.getId())
                .content("하".repeat(500))
                .build();

        //when
        commentService.editCommentContent(request);

        //then
        Comment findComment = commentRepository.findById(comment.getId()).get();
        assertThat(findComment.getContent()).isEqualTo("하".repeat(500));
    }

    @DisplayName("수정한 댓글의 길이는 500자 이하여야 한다")
    @Test
    void editCommentContentLengthCannotBeOver500() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(comment.getId())
                .content("하".repeat(501))
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("최대 댓글 길이를 초과하였습니다");

    }


    @DisplayName("존재하지 않는 회원은 댓글을 수정할수 없다")
    @Test
    void editCommentByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email("aaa")
                .postId(post.getId())
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }


    @DisplayName("탈퇴한 회원은 자신이 작성한 댓글을 수정할수 없다")
    @Test
    void editCommentByUnregisteredMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        memberRepository.delete(member);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("내가 작성하지 않은 댓글을 수정할수 없다")
    @Test
    void editCommentThatIsNotMine() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member commentMember = memberFactory.createStudentMember("commentMember", university);

        Comment comment = commentFactory.createComment(commentMember, post);

        Member member = memberFactory.createStudentMember("nickname", university);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");

    }

    @DisplayName("존재하지 않는 댓글을 수정할수 없다")
    @Test
    void editNotExistingComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(999L)
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("이미 삭제된 댓글을 수정할수 없다")
    @Test
    void editDeletedComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        commentRepository.delete(comment);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("이미 삭제된 타인의 댓글을 수정할수 없다")
    @Test
    void editDeletedCommentWroteByOtherMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member commentMember = memberFactory.createStudentMember("commentMember", university);
        Comment comment = commentFactory.createComment(commentMember, post);
        commentRepository.delete(comment);

        Member member = memberFactory.createStudentMember("nickname", university);
        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("요청값으로 전달된 글 ID 번호와, 댓글 ID 번호를 통해 DB 에서 조회한 글 ID 번호는 동일해야 한다")
    @Test
    void postIdByRequestAndPostIdByCommentIdMustBeEqual() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post anotherPost = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember1", university), board);

        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember2", university), board);
        Member member = memberFactory.createStudentMember("nickname", university);
        Comment comment = commentFactory.createComment(member, post);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(anotherPost.getId())
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("존재하지 않는 글에 대한 댓글 수정 요청을 할수 없다")
    @Test
    void postIdByRequestAndPostIdByCommentIdMustBeEqual2() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(999L)
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("삭제된 글에 작성된 댓글을 수정할수 없다")
    @Test
    void editCommentInDeletedPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentMember("postMember", university);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        postService.deletePost(post.getId(), postMember.getEmail());

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");

    }

    /**
     * 대댓글 수정
     */
    @DisplayName("대댓글 내용을 수정하면 DB에 값이 변경된다")
    @Test
    void editChildCommentContent() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment childComment = commentFactory.createChildComment(member, post, parentComment);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(childComment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when
        commentService.editCommentContent(request);

        //then
        Comment findComment = commentRepository.findActiveCommentWithMemberAndPostBy(childComment.getId()).get();

        assertThat(findComment.getId()).isEqualTo(childComment.getId());
        assertThat(findComment.getContent()).isEqualTo("수정된 댓글입니다");
        assertThat(findComment.getStatus().isEdited()).isTrue();

    }

    @DisplayName("존재하지 않는 회원은 대댓글을 수정할수 없다")
    @Test
    void editChildCommentByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("parentMember", university), post);

        Comment childComment = commentFactory.createChildComment(
                memberFactory.createStudentMember("childMember", university), post, parentComment);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email("aaa")
                .postId(post.getId())
                .commentId(childComment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("수정후 대댓글의 길이는 500자 이하여야 한다")
    @Test
    void editedChildCommentContentMaximumLengthIs500() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment childComment = commentFactory.createChildComment(member, post, parentComment);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(childComment.getId())
                .content("하".repeat(500))
                .build();

        //when
        commentService.editCommentContent(request);

        //then
        Comment findComment = commentRepository.findById(childComment.getId()).get();
        assertThat(findComment.getContent()).isEqualTo("하".repeat(500));
    }

    @DisplayName("수정후 대댓글의 길이는 500자 이하여야 한다 2")
    @Test
    void editedChildCommentContentMaximumLengthIs500_2() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment childComment = commentFactory.createChildComment(
                member, post, parentComment);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(childComment.getId())
                .content("하".repeat(501))
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("최대 댓글 길이를 초과하였습니다");

    }

    @DisplayName("탈퇴한 회원은 자신이 작성한 대댓글을 수정할수 없다")
    @Test
    void editChildCommentByUnregisteredMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment childComment = commentFactory.createChildComment(
                member, post, parentComment);

        memberRepository.delete(member);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(childComment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("내가 작성하지 않은 대댓글을 수정할수 없다")
    @Test
    void editChildCommentThatIsNotMine() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("parentCommentMember", university), post);

        Comment childComment = commentFactory.createChildComment(
                memberFactory.createStudentMember("childCommentMember", university), post, parentComment);

        Member member = memberFactory.createStudentMember("nickname", university);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(childComment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");

    }


    @DisplayName("이미 삭제된 댓글을 수정할수 없다")
    @Test
    void editDeletedChildComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment childComment = commentFactory.createChildComment(
                member, post, parentComment);

        commentRepository.delete(childComment);

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(childComment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("이미 삭제된 타인의 대댓글을 수정할수 없다")
    @Test
    void editDeletedChildCommentWroteByOtherMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("parenCommentMember", university), post);

        Comment childComment = commentFactory.createChildComment(
                memberFactory.createStudentMember("childCommentMember", university), post,
                parentComment);

        commentRepository.delete(childComment);

        Member member = memberFactory.createStudentMember("nickname", university);
        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(childComment.getId())
                .content("수정된 댓글입니다")
                .build();


        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }


    @DisplayName("대댓글이 작성된 글이 삭제된 경우, 해당 대댓글을 수정할수 없다")
    @Test
    void editChildCommentInDeletedPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentMember("postMember", university);
        Post post = postFactory.createNormalPost(postMember, board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment childComment = commentFactory.createChildComment(member, post, parentComment);

        postService.deletePost(post.getId(), postMember.getEmail());

        EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                .email(member.getEmail())
                .postId(post.getId())
                .commentId(childComment.getId())
                .content("수정된 댓글입니다")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.editCommentContent(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");

    }

    /**
     * 댓글 삭제
     */
    @DisplayName("댓글 삭제 요청을 하면 해당 댓글의 isDeleted 컬럼의 값이 true 로 변경된다")
    @Test
    void deleteComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        //when
        commentService.deleteComment(member.getEmail(), comment.getId());

        //then
        assertThat(commentRepository.findAllActiveCommentCount()).isEqualTo(0);

        Comment deletedComment = commentRepository.findById(comment.getId()).get();
        assertThat(deletedComment.getStatus().isDeleted()).isTrue();
    }

    @DisplayName("존재하지 않는 회원은 댓글 삭제를 할 수 없다")
    @Test
    void deleteCommentByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);


        //when //then
        assertThatThrownBy(() -> commentService.deleteComment("aaa", comment.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }


    @DisplayName("존재하지 않는 댓글을 삭제할수 없다")
    @Test
    void deleteNotExistingComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");


        Member member = memberFactory.createStudentMember("nickname", university);


        //when //then
        assertThatThrownBy(() -> commentService.deleteComment(member.getEmail(), 999L))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("이미 삭제된 댓글을 삭제할수 없다")
    @Test
    void deletingAlreadyDeletedCommentIsNotAllowed() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        commentRepository.delete(comment);


        //when //then
        assertThatThrownBy(() -> commentService.deleteComment(member.getEmail(), comment.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("타인이 작성한 댓글을 삭제할수 없다")
    @Test
    void deletingCommentThatIsNotMine() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        //when //then
        assertThatThrownBy(() -> commentService.deleteComment(member.getEmail(), comment.getId()))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");

    }

    @DisplayName("관리자는 자신이 작성하지 않은 댓글도 삭제할수 있다")
    @Test
    void deletingCommentByAdmin() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member admin = memberFactory.createAdminMember(university);


        //when
        commentService.deleteComment(admin.getEmail(), comment.getId());

        // then
        assertThat(commentRepository.findAllActiveCommentCount()).isZero();
    }

    /**
     * 대댓글 삭제
     */
    @DisplayName("대댓글 삭제 요청을 하면 해당 대댓글의 isDeleted 컬럼의 값이 true 로 변경된다")
    @Test
    void deleteChildComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment childComment = commentFactory.createChildComment(
                member, post, parentComment);


        //when
        commentService.deleteComment(member.getEmail(), childComment.getId());

        //then
        Comment deletedComment = commentRepository.findById(childComment.getId()).get();
        assertThat(deletedComment.getStatus().isDeleted()).isTrue();
    }

    @DisplayName("존재하지 않는 회원은 대댓글 삭제를 할 수 없다")
    @Test
    void deleteChildCommentByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("parentCommentMember", university), post);

        Comment childComment = commentFactory.createChildComment(
                memberFactory.createStudentMember("childCommentMember", university), post,
                parentComment);


        //when //then
        assertThatThrownBy(() -> commentService.deleteComment("aaa", childComment.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }



    @DisplayName("이미 삭제된 대댓글을 삭제할수 없다")
    @Test
    void deletingAlreadyDeletedChildComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment childComment = commentFactory.createChildComment(member, post, parentComment);

        commentRepository.delete(childComment);

        //when //then
        assertThatThrownBy(
                () -> commentService.deleteComment(member.getEmail(), childComment.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("댓글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("타인이 작성한 대댓글을 삭제할수 없다")
    @Test
    void deletingChildCommentThatIsNotMine() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("parentCommentMember", university), post);

        Comment childComment = commentFactory.createChildComment(
                memberFactory.createStudentMember("childCommentMember", university), post,
                parentComment);

        Member member = memberFactory.createStudentMember("nickname", university);

        //when //then
        assertThatThrownBy(
                () -> commentService.deleteComment(member.getEmail(), childComment.getId()))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");

    }

    @DisplayName("관리자는 자신이 작성하지 않은 대댓글도 삭제할수 있다")
    @Test
    void deletingChildCommentByAdmin() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("parentCommentMember", university), post);

        Comment childComment = commentFactory.createChildComment(
                memberFactory.createStudentMember("childCommentMember", university), post,
                parentComment);

        Member admin = memberFactory.createAdminMember(university);

        //when
        commentService.deleteComment(admin.getEmail(), childComment.getId());

        // then
        Comment findComment = commentRepository.findById(childComment.getId()).get();
        assertThat(findComment.getStatus().isDeleted()).isTrue();
    }


    /**
     * 특정 글에 작성된 댓글 목록 조회
     */
    @DisplayName("익명 게시판 글에 작성된 댓글들 페이징 조회 - 댓글만 있고 대댓글은 없는 경우 조회 시나리오")
    @TestFactory
    Collection<DynamicTest> loadAnonymousCommentsInPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member1 = memberFactory.createStudentMember("nickname1", university);
        Member member2 = memberFactory.createStudentMember("nickname2", university);
        Member member3 = memberFactory.createStudentMember("nickname3", university);

        return List.of(
                dynamicTest("member1이 댓글을 작성한다",
                        () -> {
                            CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                                    .email(member1.getEmail())
                                    .postId(post.getId())
                                    .content("1번 댓글입니다")
                                    .parentCommentId(null)
                                    .build();

                            commentService.createComment(request);
                        }),
                dynamicTest("member2가 댓글을 작성한다",
                        () -> {
                            CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                                    .email(member2.getEmail())
                                    .postId(post.getId())
                                    .content("2번 댓글입니다")
                                    .parentCommentId(null)
                                    .build();

                            commentService.createComment(request);
                        }),
                dynamicTest("member3이 댓글을 작성한다",
                        () -> {
                            CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                                    .email(member3.getEmail())
                                    .postId(post.getId())
                                    .content("3번 댓글입니다")
                                    .parentCommentId(null)
                                    .build();

                            commentService.createComment(request);
                        }),

                dynamicTest("해당 글에 작성된 댓글을 조회한다",
                        () -> {
                            //given

                            LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                                    .email(member1.getEmail())
                                    .id(post.getId())
                                    .page("1")
                                    .build();

                            //when
                            PostCommentsResponse response = commentService.loadCommentsInPost(
                                    request);

                            //then
                            assertThat(response).extracting("postId", "isAnonymousBoard",
                                            "sizeRequest", "actualSize", "currentPage",
                                            "isAdmin")
                                    .contains(post.getId(), true, 100, 3, 1, false);

                            assertThat(response.getComments()).hasSize(3)
                                    .extracting("nickname", "profileImageUrl", "isDeleted",
                                            "identifier", "content", "likesCount",
                                            "isParentComment")
                                    .containsExactly(
                                            tuple(null, null, false, 1, "1번 댓글입니다", 0, true),

                                            tuple(null, null, false, 2, "2번 댓글입니다", 0, true),

                                            tuple(null, null, false, 3, "3번 댓글입니다", 0, true));
                        })
        );
    }

    @DisplayName("특정 글에 작성된 댓글 목록 조회시 해당 글에 작성된 댓글이 없는 경우, 빈 배열이 반환된다")
    @Test
    void viewPostThatDoesNotHaveComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getComments()).isEmpty();
    }

    @DisplayName("익명이 아닌 게시판에 작성된 댓글 조회시 닉네임과 프로필 사진 URL 이 조회된다 ")
    @Test
    void viewIdentifiedComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createClubBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member1 = memberFactory.createStudentMember("nickname1", university);
        Member member2 = memberFactory.createStudentMember("nickname2", university);

        Comment comment1 = commentFactory.createComment(member1, post);
        Comment comment2 = commentFactory.createComment(member2, post);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member1.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getComments()).hasSize(2)
                .extracting("nickname", "profileImageUrl").containsExactlyInAnyOrder(
                        tuple(member1.getName().getNickname(),
                                member1.getProfileImage().getThumbnailUrl()),
                        tuple(member2.getName().getNickname(),
                                member2.getProfileImage().getThumbnailUrl())
                );
    }

    @DisplayName("페이징 처리시 한페이지당 댓글 100개씩 조회된다")
    @Test
    void defaultSizeIs100() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        IntStream.rangeClosed(1, 101).forEach(i -> {
            commentFactory.createComment(member, post);
        });

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response)
                .extracting("sizeRequest", "actualSize")
                .contains(100, 100);
    }

    @DisplayName("내가 작성한 댓글은 isMyComment 컬럼의 값이 true 다")
    @Test
    void viewMyComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getComments()).hasSize(1)
                .extracting("isMyComment").contains(true);
    }

    @DisplayName("내가 작성하지 않은 댓글은 isMyComment 컬럼의 값이 false 다")
    @Test
    void viewNotMyComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getComments()).hasSize(1)
                .extracting("isMyComment").contains(false);
    }

    @DisplayName("내가 좋아요를 누른 댓글은 pressedLikeOnThisComment 컬럼의 값이 true 다")
    @Test
    void viewCommentThatIPressedLike() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        CommentLike commentLike = commentLikeFactory.createCommentLike(member, comment);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getComments()).hasSize(1)
                .extracting("pressedLikeOnThisComment").contains(true);
    }

    @DisplayName("내가 좋아요를 누르지 않은 댓글은 pressedLikeOnThisComment 컬럼의 값이 false 다")
    @Test
    void viewCommentThatIDidNotPressedLike() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getComments()).hasSize(1)
                .extracting("pressedLikeOnThisComment").contains(false);
    }

    @DisplayName("댓글이 수정된 경우, isEdited 컬럼값 변경 시나리오")
    @TestFactory
    Collection<DynamicTest> viewEditedComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member commentMember = memberFactory.createStudentMember("commentMember", university);
        Comment comment = commentFactory.createComment(commentMember, post);

        return List.of(
                dynamicTest("댓글이 수정된다",
                        () -> {
                            EditCommentServiceRequest request = EditCommentServiceRequest.builder()
                                    .email(commentMember.getEmail())
                                    .commentId(comment.getId())
                                    .postId(post.getId())
                                    .content("수정후 댓글")
                                    .build();

                            commentService.editCommentContent(request);

                        }),

                dynamicTest("수정된 댓글은 isEdited 컬럼의 값이 true 다",
                        () -> {
                            //given
                            Member member = memberFactory.createStudentMember("nickname",
                                    university);

                            LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                                    .email(member.getEmail())
                                    .id(post.getId())
                                    .page("1")
                                    .build();

                            //when
                            PostCommentsResponse response = commentService.loadCommentsInPost(
                                    request);

                            //then
                            assertThat(response.getComments()).hasSize(1)
                                    .extracting("isEdited").contains(true);
                        })
        );
    }

    @DisplayName("댓글이 수정되지 않은 경우 isEdited 컬럼의 값은 false 다")
    @Test
    void viewNotEditedComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getComments()).hasSize(1)
                .extracting("isEdited").contains(false);
    }

    @DisplayName("글 작성자가 자신의 글에 댓글을 남긴경우, 해당 댓글의 isPostWriter 컬럼의 값은 true 다")
    @Test
    void viewCommentWroteByPostWriter() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentMember("postMember", university);
        Post post = postFactory.createNormalPost(postMember, board);

        Comment comment = commentFactory.createComment(postMember, post);

        Member member = memberFactory.createStudentMember("nickname", university);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getComments()).hasSize(1)
                .extracting("isPostWriter").contains(true);
    }

    @DisplayName("글 작성자가 아닌 회원이 어떤 글에 댓글을 남기면, 해당 댓글의 isPostWriter 컬럼의 값은 false 다")
    @Test
    void viewCommentThatIsNotWroteByPostWriter() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getComments()).hasSize(1)
                .extracting("isPostWriter").contains(false);
    }

    @DisplayName("특정 글에 작성된 댓글들 조회시, 조회를 요청한 회원이 관리자인 경우 isAdmin 컬럼의 값은 true 다")
    @Test
    void viewCommentByAdmin() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member admin = memberFactory.createAdminMember(university);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(admin.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.isAdmin()).isTrue();
    }

    @DisplayName("존재하지 않는 글에 작성된 댓글을 조회할수 없다")
    @Test
    void viewCommentsInNotExistingPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Member member = memberFactory.createStudentMember("nickname", university);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(999L)
                .page("1")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.loadCommentsInPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("삭제된 글에 작성된 댓글을 조회할수 없다")
    @Test
    void viewCommentsInDeletedPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentMember("postMember", university);
        Post post = postFactory.createNormalPost(postMember, board);

        postService.deletePost(post.getId(), postMember.getEmail());

        Member member = memberFactory.createStudentMember("nickname", university);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when //then
        assertThatThrownBy(() -> commentService.loadCommentsInPost(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");

    }


    @DisplayName("페이지 요청값이 0인 경우 1페이지가 조회된다")
    @Test
    void showFirstPageWhenPageRequestIs0() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("0")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getCurrentPage()).isEqualTo(1);
    }

    @DisplayName("페이지 요청값이 숫자가 아닌 경우 1페이지가 조회된다")
    @Test
    void showFirstPageWhenPageRequestIsNotNumber() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("aaaa")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getCurrentPage()).isEqualTo(1);
    }

    @DisplayName("페이지 요청값이 음수인 경우 1페이지가 조회된다")
    @Test
    void showFirstPageWhenPageRequestIsMinus() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("nickname", university);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("-1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getCurrentPage()).isEqualTo(1);
    }

    @DisplayName("대댓글이 달리지 않은 댓글이 삭제된 경우, 조회되지 않는다")
    @Test
    void deletedCommentIsNotShown() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        commentRepository.delete(comment);

        Member member = memberFactory.createStudentMember("nickname", university);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getComments().size()).isZero();
    }

    @DisplayName("익명 게시판 글에 작성된 댓글들 페이징 조회 - 댓글과 대댓글은 모두 있는 경우")
    @Test
    void loadAnonymousCommentsAndChildCommentsInPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        CreateCommentResponse parentComment1 = commentService.createComment(
                CreateCommentServiceRequest.builder()
                        .email(member.getEmail())
                        .postId(post.getId())
                        .content("1번 댓글입니다")
                        .parentCommentId(null)
                        .build());

        CreateCommentResponse parentComment2 = commentService.createComment(
                CreateCommentServiceRequest.builder()
                        .email(member.getEmail())
                        .postId(post.getId())
                        .content("2번 댓글입니다")
                        .parentCommentId(null)
                        .build());

        CreateCommentResponse childComment1 = commentService.createComment(
                CreateCommentServiceRequest.builder()
                        .email(member.getEmail())
                        .postId(post.getId())
                        .content("1번 댓글의 첫번쨰 대댓글 입니다")
                        .parentCommentId(parentComment1.getCommentId())
                        .build());

        CreateCommentResponse parentComment3 = commentService.createComment(
                CreateCommentServiceRequest.builder()
                        .email(member.getEmail())
                        .postId(post.getId())
                        .content("3번 댓글입니다")
                        .parentCommentId(null)
                        .build());

        CreateCommentResponse childComment2 = commentService.createComment(
                CreateCommentServiceRequest.builder()
                        .email(member.getEmail())
                        .postId(post.getId())
                        .content("1번 댓글의 두번쨰 대댓글 입니다")
                        .parentCommentId(parentComment1.getCommentId())
                        .build());

        CreateCommentResponse childComment3 = commentService.createComment(
                CreateCommentServiceRequest.builder()
                        .email(member.getEmail())
                        .postId(post.getId())
                        .content("2번 댓글의 첫번쨰 대댓글 입니다")
                        .parentCommentId(parentComment2.getCommentId())
                        .build());

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getComments()).hasSize(6)
                .extracting("content").containsExactly(
                        "1번 댓글입니다",
                        "1번 댓글의 첫번쨰 대댓글 입니다",
                        "1번 댓글의 두번쨰 대댓글 입니다",

                        "2번 댓글입니다",
                        "2번 댓글의 첫번쨰 대댓글 입니다",

                        "3번 댓글입니다"
                );
    }

    @DisplayName("삭제된 대댓글은 조회되지 않는다")
    @Test
    void deletedChildCommentIsNotShown() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("parentCommentMember", university), post);

        Comment childComment = commentFactory.createChildComment(
                memberFactory.createStudentMember("childCommentMember", university), post,
                parentComment);

        commentRepository.delete(childComment);

        LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                .email(member.getEmail())
                .id(post.getId())
                .page("1")
                .build();

        //when
        PostCommentsResponse response = commentService.loadCommentsInPost(request);

        //then
        assertThat(response.getComments()).hasSize(1);
    }

    @DisplayName("댓글과 대댓글 삭제 여부에 따른 댓글 목록 조회 시나리오")
    @TestFactory
    Collection<DynamicTest> DeletedCommentViewScenarioTest() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("parentCommentMember", university), post);

        Comment childComment = commentFactory.createChildComment(
                memberFactory.createStudentMember("childCommentMember", university), post, parentComment);

        return List.of(
                dynamicTest("대댓글이 존재하는 댓글이 삭제된 경우 해당 댓글은 삭제된 댓글입니다 라고 조회된다",
                        () -> {
                            //given
                            commentRepository.delete(parentComment);

                            LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                                    .email(member.getEmail())
                                    .id(post.getId())
                                    .page("1")
                                    .build();

                            //when
                            PostCommentsResponse response = commentService.loadCommentsInPost(
                                    request);

                            //then
                            assertThat(response.getComments()).hasSize(2)
                                    .extracting("content")
                                    .containsExactly(
                                            "삭제된 댓글입니다",
                                            childComment.getContent()
                                    );
                        }),

                dynamicTest("대댓글 또한 삭제된 경우, 댓글과 대댓글 모두 조회되지 않는다",
                        () -> {
                            //given
                            commentRepository.delete(childComment);

                            LoadCommentsInPostServiceRequest request = LoadCommentsInPostServiceRequest.builder()
                                    .email(member.getEmail())
                                    .id(post.getId())
                                    .page("1")
                                    .build();

                            //when
                            PostCommentsResponse response = commentService.loadCommentsInPost(
                                    request);

                            //then
                            assertThat(response.getComments()).hasSize(0);
                        })
        );
    }

    /**
     * 특정 글에 작성된 베스트 댓글 조회
     */

    //TODO 기업 회원이 댓글 작성한 경우, 회원 이름 어떻게 조회되는지 테스트 필요함

    @DisplayName("특정글의 베스트 댓글은 좋아요가 10개 이상인 댓글들 중 가장 좋아요가 많은 댓글이 선정된다")
    @Test
    void findBestComment() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createClubBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        IntStream.rangeClosed(1, 10).forEach(i -> {
            commentLikeFactory.createCommentLike(member, comment);
        });

        //when
        BestCommentResponse response = commentService.loadBestCommentInPost(post.getId(),
                member.getEmail());

        //then
        assertThat(response).extracting("commentId", "postId", "isAnonymousBoard", "nickname",
                        "profileImageUrl", "content", "likesCount")
                .contains(comment.getId(), post.getId(), false,
                        member.getName().getNickname(),
                        member.getProfileImage().getThumbnailUrl(), comment.getContent(), 10);

    }

    @DisplayName("익명글의 베스트 댓글은 nickname 과 profileImageUrl 컬럼의 값이 null 이다")
    @Test
    void findBestAnonymousComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("commentMember", university);

        Comment comment = commentFactory.createComment(member, post);

        IntStream.rangeClosed(1, 10).forEach(i -> {
            commentLikeFactory.createCommentLike(member, comment);
        });

        //when
        BestCommentResponse response = commentService.loadBestCommentInPost(post.getId(),
                member.getEmail());

        //then
        assertThat(response.isAnonymousBoard()).isTrue();
        assertThat(response.getNickname()).isNull();
        assertThat(response.getProfileImageUrl()).isNull();
    }

    @DisplayName("특정글에 작성된 모든 댓글이 전부 좋아요 개수가 10개 미만인 경우, 베스트 댓글은 존재하지 않는다")
    @Test
    void bestCommentIsNotCreatedWhenCommentLikeIsLessThen10() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        IntStream.rangeClosed(1, 9).forEach(i -> {
            commentLikeFactory.createCommentLike(
                    memberFactory.createStudentMember("nickname" + i, university), comment);
        });

        Member member = memberFactory.createStudentMember("member", university);

        //when
        BestCommentResponse response = commentService.loadBestCommentInPost(post.getId(),
                member.getEmail());

        //then
        assertThat(response).isNull();
    }

    @DisplayName("특정글에 작성된 댓글중 좋아요가 10개 이상인 댓글이 여러개인 경우, 그중 가장 좋아요가 많은 댓글이 베스트 댓글로 선정된다")
    @Test
    void commentThatHasOver10LikesAndHasMostLikesBecomesBestComment() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment1 = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember1", university), post);

        Comment comment2 = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember2", university), post);

        Comment comment3 = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember3", university), post);

        IntStream.rangeClosed(1, 10).forEach(i -> {
            commentLikeFactory.createCommentLike(
                    memberFactory.createStudentMember("nicknameA" + i, university), comment1);
        });

        IntStream.rangeClosed(1, 11).forEach(i -> {
            commentLikeFactory.createCommentLike(
                    memberFactory.createStudentMember("nicknameB" + i, university), comment2);
        });

        IntStream.rangeClosed(1, 12).forEach(i -> {
            commentLikeFactory.createCommentLike(
                    memberFactory.createStudentMember("nicknameC" + i, university), comment3);
        });

        Member member = memberFactory.createStudentMember("member", university);

        //when
        BestCommentResponse response = commentService.loadBestCommentInPost(post.getId(),
                member.getEmail());

        //then
        assertThat(response.getCommentId()).isEqualTo(comment3.getId());
    }

    @DisplayName("가장 많은 좋아요를 받은 댓글이 여러개인 경우, 가장 먼저 작성된 댓글이 베스트 댓글로 선정된다")
    @Test
    void commentThatHasEarliestCreatedTimeBecomesBestComment() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment1 = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember1", university), post);

        Comment comment2 = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember2", university), post);

        Comment comment3 = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember3", university), post);


        IntStream.rangeClosed(1, 10).forEach(i -> {
            commentLikeFactory.createCommentLike(
                    memberFactory.createStudentMember("nicknameA" + i, university), comment1);
        });

        IntStream.rangeClosed(1, 10).forEach(i -> {
            commentLikeFactory.createCommentLike(
                    memberFactory.createStudentMember("nicknameB" + i, university), comment2);
        });

        IntStream.rangeClosed(1, 10).forEach(i -> {
            commentLikeFactory.createCommentLike(
                    memberFactory.createStudentMember("nicknameC" + i, university), comment3);
        });

        Member member = memberFactory.createStudentMember("member", university);

        //when
        BestCommentResponse response = commentService.loadBestCommentInPost(post.getId(),
                member.getEmail());

        //then
        assertThat(response.getCommentId()).isEqualTo(comment1.getId());
    }

    @DisplayName("대댓글도 베스트 댓글로 선정될수 있다")
    @Test
    void childCommentCanBeBestComment() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("parentCommentMember", university), post);

        Comment childComment = commentFactory.createChildComment(
                memberFactory.createStudentMember("childCommentMember", university), post,
                parentComment);

        IntStream.rangeClosed(1, 10).forEach(i -> {
            commentLikeFactory.createCommentLike(
                    memberFactory.createStudentMember("nickname" + i, university), childComment);
        });

        Member member = memberFactory.createStudentMember("member", university);

        //when
        BestCommentResponse response = commentService.loadBestCommentInPost(post.getId(),
                member.getEmail());

        //then
        assertThat(response.getCommentId()).isEqualTo(childComment.getId());
    }

    @DisplayName("존재하지 않는 글의 베스트 댓글을 조회할수 없다")
    @Test
    void viewBestCommentInNotExistingPost() {
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createStudentMember("member", university);

        //when //then
        assertThatThrownBy(
                () -> commentService.loadBestCommentInPost(999L, member.getEmail()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    @DisplayName("삭제된 글의 베스트 댓글을 조회할수 없다")
    @Test
    void viewBestCommentInDeletedPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentMember("postMember", university);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createStudentMember("member", university);

        postService.deletePost(post.getId(), postMember.getEmail());

        //when //then
        assertThatThrownBy(
                () -> commentService.loadBestCommentInPost(999L, member.getEmail()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }

    /**
     * 내가 작성한 댓글 목록 조회
     */

    @DisplayName("내가 작성한 댓글 목록 조회")
    @Test
    void listCommentsWroteByMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);

        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);
        Comment comment = commentFactory.createComment(member, post);

        //when
        MyCommentsResponse response = commentService.loadMyComments(1, member.getEmail());

        //then
        assertThat(response).extracting("sizeRequest", "actualSize", "currentPage")
                .contains(20, 1, 1);

        assertThat(response.getComments()).extracting("commentId", "postId", "content")
                .contains(
                        tuple(comment.getId(), post.getId(), comment.getContent())
                );
    }

    @DisplayName("내가 작성한 대댓글 목록 조회")
    @Test
    void listChildCommentsWroteByMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Member member = memberFactory.createStudentMember("nickname", university);

        Post post1 = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember1", university),
                boardFactory.createAnonymousBoard(university));

        Post post2 = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember2", university),
                boardFactory.createClubBoard(university));

        Comment myComment1 = commentFactory.createComment(member, post1);

        Comment myComment2 = commentFactory.createComment(member, post2);


        //when
        MyCommentsResponse response = commentService.loadMyComments(1, member.getEmail());

        //then
        assertThat(response).extracting("sizeRequest", "actualSize", "currentPage")
                .contains(20, 2, 1);

        assertThat(response.getComments()).extracting("commentId", "postId", "content")
                .contains(
                        tuple(myComment2.getId(), post2.getId(), myComment2.getContent()),
                        tuple(myComment1.getId(), post1.getId(), myComment1.getContent())
                );
    }

    @DisplayName("내가 작성한 댓글과 대댓글 목록 조회시 가장 최근에 작성된 순으로 조회된다")
    @Test
    void commentsOrderIsLIFO() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);

        Member member = memberFactory.createStudentMember("nickname", university);

        Post post1 = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember1", university), board);

        Post post2 = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember2", university), board);

        Comment comment1 = commentFactory.createComment(member, post1);

        Comment comment2 = commentFactory.createComment(member, post2);

        Comment comment3 = commentFactory.createComment(member, post1);

        //when
        MyCommentsResponse response = commentService.loadMyComments(1, member.getEmail());

        //then
        assertThat(response).extracting("sizeRequest", "actualSize", "currentPage")
                .contains(20, 3, 1);

        assertThat(response.getComments()).extracting("commentId", "postId", "content")
                .containsExactly(
                        tuple(comment3.getId(), post1.getId(), comment3.getContent()),
                        tuple(comment2.getId(), post2.getId(), comment2.getContent()),
                        tuple(comment1.getId(), post1.getId(), comment1.getContent())
                );
    }

    @DisplayName("내가 작성한 댓글 목록 조회시, 작성한 댓글이 없는 경우 목록이 반환된다")
    @Test
    void listCommentsWroteByMeWhoDidNotWroteAnyComments() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Member member = memberFactory.createStudentMember("nickname", university);

        //when
        MyCommentsResponse response = commentService.loadMyComments(1, member.getEmail());

        //then
        assertThat(response.getComments()).isEmpty();
    }


    @DisplayName("내가 작성한 댓글 목록 조회시, 삭제된 댓글은 조회되지 않는다")
    @Test
    void deletedCommentsAreNotListedWhenListingCommentsWroteByMe() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        commentRepository.delete(comment);

        //when
        MyCommentsResponse response = commentService.loadMyComments(1, member.getEmail());

        //then
        assertThat(response.getComments()).isEmpty();
    }

    @DisplayName("내가 작성한 댓글 목록 조회시, 댓글이 작성된 글이 삭제되더라도 해당 댓글은 조회된다")
    @Test
    void commentsInDeletedPostAreListedWhenListingCommentsWroteByMe() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentMember("postMember", university);
        Post post = postFactory.createNormalPost(postMember, board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        postService.deletePost(post.getId(), postMember.getEmail());

        //when
        MyCommentsResponse response = commentService.loadMyComments(1, member.getEmail());

        //then
        assertThat(response.getComments()).hasSize(1)
                .extracting("postId").contains(post.getId());
    }

    @DisplayName("내가 작성한 댓글 목록 조회시, 20개씩 한 페이지로 조회된다")
    @Test
    void defaultPageSizeIs20() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);

        Member member = memberFactory.createStudentMember("nickname", university);

        IntStream.rangeClosed(1, 30).forEach(i -> {
            Post post = postFactory.createNormalPost(
                    memberFactory.createStudentMember("postMember" + i, university), board);

            Comment parentComment = commentFactory.createComment(member, post);
        });

        //when
        MyCommentsResponse firstPage = commentService.loadMyComments(1, member.getEmail());
        MyCommentsResponse secondPage = commentService.loadMyComments(2, member.getEmail());

        //then
        assertThat(firstPage).extracting("sizeRequest", "actualSize", "currentPage")
                .contains(20, 20, 1);
        assertThat(firstPage.getComments()).hasSize(20);

        assertThat(secondPage).extracting("sizeRequest", "actualSize", "currentPage")
                .contains(20, 10, 2);
        assertThat(secondPage.getComments()).hasSize(10);
    }

    /**
     * 내가 작성한 총 댓글 갯수 조회
     */

    @DisplayName("내가 작성한 댓글 총 갯수 조회")
    @Test
    void countMyTotalCommentsNumber() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);

        Member member = memberFactory.createStudentMember("nickname", university);

        IntStream.rangeClosed(1, 30).forEach(i -> {
            Post post = postFactory.createNormalPost(
                    memberFactory.createStudentMember("postMember" + i, university), board);

            Comment comment = commentFactory.createComment(member, post);
        });

        //when
        Long commentCount = commentService.findCommentsCountByMember(member.getEmail());

        //then
        assertThat(commentCount).isEqualTo(30);
    }

    @DisplayName("내가 작성한 댓글 총 갯수 조회시 삭제된 댓글은 포함되지 않는다")
    @Test
    void deletedCommentIsNotIncludedWhenCountingMyCommentsNumber() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberFactory.createStudentMember("nickname", university);

        Comment comment = commentFactory.createComment(member, post);

        commentRepository.delete(comment);

        //when
        Long commentCount = commentService.findCommentsCountByMember(member.getEmail());

        //then
        assertThat(commentCount).isEqualTo(0);
    }



    @DisplayName("탈퇴한 상태로 내가 작성한 총 댓글 갯수를 조회할수 없다")
    @Test
    void countingMyCommentsNumberWithUnregisteredAccount() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Member member = memberFactory.createStudentMember("nickname", university);

        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> commentService.findCommentsCountByMember(member.getEmail()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("존재하지 않는 회원의 총 댓글 갯수를 조회할수 없다")
    @Test
    void countingMyCommentsNumberWithNotExistingMember() {
        //when //then
        assertThatThrownBy(() -> commentService.findCommentsCountByMember("aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    /**
     * 특정 글에 작성된 총 댓글 갯수 조회
     */

    @DisplayName("특정 글에 작성된 총 댓글 갯수 조회")
    @Test
    void countCommentsNumberInPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment1 = commentFactory.createComment(
                memberFactory.createStudentMember("parentCommentMember1", university), post);

        Comment parentComment2 = commentFactory.createComment(
                memberFactory.createStudentMember("parentCommentMember2", university), post);

        Comment childComment1 = commentFactory.createChildComment(
                memberFactory.createStudentMember("childCommentMember1", university), post,
                parentComment1);

        Comment childComment2 = commentFactory.createChildComment(
                memberFactory.createStudentMember("childCommentMember2", university), post,
                parentComment2);

        //when
        Long commentsCount = commentService.findActiveCommentsCountInPost(post.getId());

        //then
        assertThat(commentsCount).isEqualTo(4);
    }

    @DisplayName("특정 글에 작성된 총 댓글 갯수 조회 - 댓글이 없는 경우")
    @Test
    void countCommentsNumberInPostThatDoesNotHaveComment() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        //when
        Long commentsCount = commentService.findActiveCommentsCountInPost(post.getId());

        //then
        assertThat(commentsCount).isEqualTo(0);
    }

    @DisplayName("특정 글에 작성된 총 댓글 갯수 조회시 삭제된 댓글은 갯수에 포함되지 않는다")
    @Test
    void deletedCommentIsNotCounted() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        commentRepository.delete(comment);

        //when
        Long commentsCount = commentService.findActiveCommentsCountInPost(post.getId());

        //then
        assertThat(commentsCount).isEqualTo(0);
    }

    @DisplayName("존재하지 않는 글에 작성된 총 댓글 갯수를 조회할수 없다")
    @Test
    void countCommentsInNotExistingPost() {
        //when //then
        assertThatThrownBy(() -> commentService.findActiveCommentsCountInPost(999L))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");

    }

    @DisplayName("삭제된 글에 작성된 총 댓글 갯수를 조회할수 없다")
    @Test
    void countCommentsInDeletedPost() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member postMember = memberFactory.createStudentMember("postMember", university);
        Post post = postFactory.createNormalPost(postMember, board);

        postService.deletePost(post.getId(), postMember.getEmail());

        //when //then
        assertThatThrownBy(() -> commentService.findActiveCommentsCountInPost(post.getId()))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시글이 존재하지 않거나 삭제되었습니다");
    }
}
