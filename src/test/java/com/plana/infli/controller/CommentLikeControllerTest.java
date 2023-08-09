package com.plana.infli.controller;

import static java.lang.String.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.annotation.MockMvcTest;
import com.plana.infli.annotation.WithMockMember;
import com.plana.infli.domain.Board;
import com.plana.infli.domain.Comment;
import com.plana.infli.domain.CommentLike;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.University;
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
import com.plana.infli.web.dto.request.commentlike.cancel.CancelCommentLikeRequest;
import com.plana.infli.web.dto.request.commentlike.create.CreateCommentLikeRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@MockMvcTest
public class CommentLikeControllerTest {


    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper om;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentFactory commentFactory;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private BoardFactory boardFactory;

    @Autowired
    private PostFactory postFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private CommentLikeFactory commentLikeFactory;

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
    @WithMockMember
    @Test
    void createNewCommentLike() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);
        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = findContextMember();

        String request = om.writeValueAsString(CreateCommentLikeRequest.builder()
                .postId(post.getId())
                .commentId(comment.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments/likes")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        CommentLike like = commentLikeRepository.findByCommentAndMember(comment, member).get();
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(valueOf(like.getId())))
                .andDo(print());
    }

    @DisplayName("로그인 하지 않은 상태로 댓글 좋아요를 누를수 없다")
    @Test
    void createNewCommentLikeWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);
        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        String request = om.writeValueAsString(CreateCommentLikeRequest.builder()
                .postId(post.getId())
                .commentId(comment.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments/likes")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
                .andDo(print());
    }

    @DisplayName("좋아요를 누를 댓글의 Id 번호는 필수다")
    @WithMockMember
    @Test
    void commentIdIsMandatory() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);

        String request = om.writeValueAsString(CreateCommentLikeRequest.builder()
                .postId(post.getId())
                .commentId(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments/likes")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.commentId").value("댓글 번호가 입력되지 않았습니다"))
                .andDo(print());
    }

    @DisplayName("댓글 좋아요 요청시 댓글이 작성된 글의 Id 번호는 필수다")
    @WithMockMember
    @Test
    void postIdIsMandatory() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);
        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        String request = om.writeValueAsString(CreateCommentLikeRequest.builder()
                .postId(null)
                .commentId(comment.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments/likes")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.postId").value("글 번호가 입력되지 않았습니다"))
                .andDo(print());
    }


    @DisplayName("댓글 좋아요 취소")
    @WithMockMember
    @Test
    void cancelCommentLike() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);
        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = findContextMember();
        CommentLike commentLike = commentLikeFactory.createCommentLike(member, comment);

        String request = om.writeValueAsString(CancelCommentLikeRequest.builder()
                .postId(post.getId())
                .commentId(comment.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(delete("/api/comments/likes")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());

        assertThat(commentLikeRepository.findByCommentAndMember(comment, member)).isEmpty();
    }

    @DisplayName("로그인 하지 않은 상태로 댓글 좋아요 취소를 할수 없다")
    @Test
    void cancelCommentLikeWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);
        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberFactory.createStudentMember("member", university);

        CommentLike commentLike = commentLikeFactory.createCommentLike(member, comment);

        String request = om.writeValueAsString(CancelCommentLikeRequest.builder()
                .postId(post.getId())
                .commentId(comment.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(delete("/api/comments/likes")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
                .andDo(print());
    }

    @DisplayName("댓글 좋아요 취소 요청시 댓글 Id 번호는 필수다")
    @WithMockMember
    @Test
    void cancelCommentLikeWithoutCommentId() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);
        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = findContextMember();
        CommentLike commentLike = commentLikeFactory.createCommentLike(member, comment);

        String request = om.writeValueAsString(CancelCommentLikeRequest.builder()
                .postId(post.getId())
                .commentId(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(delete("/api/comments/likes")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.commentId").value("댓글 번호가 입력되지 않았습니다"))
                .andDo(print());
    }

    @DisplayName("댓글 좋아요 취소 요청시 댓글이 작성된 글의 Id 번호는 필수다")
    @WithMockMember
    @Test
    void cancelCommentLikeWithoutPostId() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createStudentMember("postMember", university), board);
        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = findContextMember();
        CommentLike commentLike = commentLikeFactory.createCommentLike(member, comment);

        String request = om.writeValueAsString(CancelCommentLikeRequest.builder()
                .postId(null)
                .commentId(comment.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(delete("/api/comments/likes")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.postId").value("글 번호가 입력되지 않았습니다"))
                .andDo(print());
    }

    private Member findContextMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email).get();
    }
}
