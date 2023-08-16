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
                memberFactory.createVerifiedStudentMember("postMember", university), board);
        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = findContextMember();

        //when
        ResultActions resultActions = mvc.perform(
                post("/api/comments/{commentId}/likes", comment.getId())
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isCreated())
                .andDo(print());
    }

    @DisplayName("로그인 하지 않은 상태로 댓글 좋아요를 누를수 없다")
    @Test
    void createNewCommentLikeWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(
                memberFactory.createVerifiedStudentMember("postMember", university), board);
        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        //when
        ResultActions resultActions = mvc.perform(
                post("/api/comments/{commentId}/likes", comment.getId())
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
                memberFactory.createVerifiedStudentMember("postMember", university), board);


        //when
        ResultActions resultActions = mvc.perform(post("/api/comments/{commentId}/likes", " ")
                        .with(csrf()))
                .andExpect(jsonPath("$.message").value("Path Variable 값이 입력되지 않았습니다"))
                .andExpect(jsonPath("$.validation.commentId").value("Long"));

        //then
        resultActions.andExpect(status().isBadRequest())
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
                memberFactory.createVerifiedStudentMember("postMember", university), board);
        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = findContextMember();
        CommentLike commentLike = commentLikeFactory.createCommentLike(member, comment);

        //when
        ResultActions resultActions = mvc.perform(
                delete("/api/comments/{commentId}/likes", comment.getId())
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
                memberFactory.createVerifiedStudentMember("postMember", university), board);
        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        CommentLike commentLike = commentLikeFactory.createCommentLike(member, comment);

        //when
        ResultActions resultActions = mvc.perform(
                delete("/api/comments/{commentId}/likes", comment.getId())
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
                memberFactory.createVerifiedStudentMember("postMember", university), board);
        Comment comment = commentFactory.createComment(
                memberFactory.createVerifiedStudentMember("commentMember", university), post);

        Member member = findContextMember();
        CommentLike commentLike = commentLikeFactory.createCommentLike(member, comment);

        //when
        ResultActions resultActions = mvc.perform(
                delete("/api/comments/{commentId}/likes", "")
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andDo(print());
    }


    private Member findContextMember() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findActiveMemberBy(username).get();
    }
}
