package com.plana.infli.controller;


import static java.lang.String.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.annotation.MockMvcTest;
import com.plana.infli.annotation.WithMockMember;
import com.plana.infli.domain.Board;
import com.plana.infli.domain.Comment;
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
import com.plana.infli.web.dto.request.comment.create.CreateCommentRequest;
import com.plana.infli.web.dto.request.comment.edit.EditCommentRequest;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@MockMvcTest
public class CommentControllerTest {


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

    @DisplayName("댓글 작성")
    @WithMockMember
    @Test
    void writeNewComment() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        String request = om.writeValueAsString(CreateCommentRequest.builder()
                .postId(post.getId())
                .parentCommentId(null)
                .content("댓글입니다")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        Comment comment = commentRepository.findAll().get(0);
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(comment.getId()))
                .andExpect(jsonPath("$.identifierNumber").value(1))
                .andDo(print());

    }

    @DisplayName("대댓글 작성")
    @WithMockMember
    @Test
    void writeNewChildComment() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentRepository.save(commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post));

        String request = om.writeValueAsString(CreateCommentRequest.builder()
                .postId(post.getId())
                .parentCommentId(parentComment.getId())
                .content("대댓글입니다")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());
        Comment comment = commentRepository.findAll().get(1);
        assertThat(comment).isNotNull();
    }

    @DisplayName("로그인 하지 않은 상태로 댓글을 작성할수 없다")
    @Test
    void writeNewCommentWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        String request = om.writeValueAsString(CreateCommentRequest.builder()
                .postId(post.getId())
                .parentCommentId(null)
                .content("댓글입니다")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
                .andDo(print());
    }

    @DisplayName("로그인 하지 않은 상태로 대댓글을 작성할수 없다")
    @Test
    void writeNewChildCommentWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentRepository.save(commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post));

        String request = om.writeValueAsString(CreateCommentRequest.builder()
                .postId(post.getId())
                .parentCommentId(parentComment.getId())
                .content("대댓글입니다")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
                .andDo(print());
    }

    @DisplayName("댓글을 작성시 글 ID 번호는 필수다")
    @WithMockMember
    @Test
    void writeNewCommentWithoutPostId() throws Exception {
        //given
        String request = om.writeValueAsString(CreateCommentRequest.builder()
                .postId(null)
                .parentCommentId(null)
                .content("댓글입니다")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.postId").value("글 번호가 입력되지 않았습니다"))
                .andDo(print());
    }

    @DisplayName("대댓글을 작성시 글 ID 번호는 필수다")
    @WithMockMember
    @Test
    void writeNewChildCommentWithoutPostId() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentRepository.save(commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post));

        String request = om.writeValueAsString(CreateCommentRequest.builder()
                .postId(null)
                .parentCommentId(parentComment.getId())
                .content("대댓글입니다")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.postId").value("글 번호가 입력되지 않았습니다"))
                .andDo(print());
    }

    @DisplayName("댓글 작성시 내용은 필수다")
    @WithMockMember
    @Test
    void writeNewCommentWithoutContent() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        String request = om.writeValueAsString(CreateCommentRequest.builder()
                .postId(post.getId())
                .parentCommentId(null)
                .content(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("내용을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("댓글 작성시 내용은 필수다2")
    @WithMockMember
    @Test
    void writeNewCommentWithoutContent2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        String request = om.writeValueAsString(CreateCommentRequest.builder()
                .postId(post.getId())
                .parentCommentId(null)
                .content("")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("내용을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("대댓글을 작성시 내용은 필수다")
    @WithMockMember
    @Test
    void writeNewChildCommentWithoutContent() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentRepository.save(commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post));

        String request = om.writeValueAsString(CreateCommentRequest.builder()
                .postId(post.getId())
                .parentCommentId(parentComment.getId())
                .content(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("내용을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("대댓글을 작성시 내용은 필수다2")
    @WithMockMember
    @Test
    void writeNewChildCommentWithoutContent2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentRepository.save(commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post));

        String request = om.writeValueAsString(CreateCommentRequest.builder()
                .postId(post.getId())
                .parentCommentId(parentComment.getId())
                .content("")
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("내용을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("댓글 내용 수정시 DB에 값이 변경된다")
    @WithMockMember
    @Test
    void editComment() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = findContextMember();
        Comment comment = commentFactory.createComment(member, post);

        String request = om.writeValueAsString(EditCommentRequest.builder()
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .postId(post.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());
        Comment findComment = commentRepository.findById(comment.getId()).get();
        assertThat(findComment.getContent()).isEqualTo("수정된 댓글입니다");
    }

    @DisplayName("대댓글 내용 수정시 DB에 값이 변경된다")
    @WithMockMember
    @Test
    void editChildComment() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = findContextMember();
        Comment comment = commentRepository.save(
                commentFactory.createChildComment(member, post, parentComment));

        String request = om.writeValueAsString(EditCommentRequest.builder()
                .commentId(comment.getId())
                .content("수정된 대댓글입니다")
                .postId(post.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());
        Comment findComment = commentRepository.findById(comment.getId()).get();
        assertThat(findComment.getContent()).isEqualTo("수정된 대댓글입니다");
        assertThat(findComment.isEdited()).isTrue();
    }

    @DisplayName("댓글 내용 수정시 수정할 댓글 Id 번호는 필수다")
    @WithMockMember
    @Test
    void editCommentWithoutCommentId() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        String request = om.writeValueAsString(EditCommentRequest.builder()
                .commentId(null)
                .content("수정된 댓글입니다")
                .postId(post.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.commentId").value("수정할 댓글번호가 입력되지 않았습니다"))
                .andDo(print());
    }

    @DisplayName("댓글 내용 수정시 내용은 필수다")
    @WithMockMember
    @Test
    void editCommentWithoutContent() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = findContextMember();
        Comment comment = commentFactory.createComment(member, post);

        String request = om.writeValueAsString(EditCommentRequest.builder()
                .commentId(comment.getId())
                .content(null)
                .postId(post.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("내용을 입력하지 않았습니다"))
                .andDo(print());
    }

    @DisplayName("댓글 내용 수정시 내용은 필수다2")
    @WithMockMember
    @Test
    void editCommentWithoutContent2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = findContextMember();
        Comment comment = commentFactory.createComment(member, post);

        String request = om.writeValueAsString(EditCommentRequest.builder()
                .commentId(comment.getId())
                .content("")
                .postId(post.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("내용을 입력하지 않았습니다"))
                .andDo(print());
    }

    @DisplayName("대댓글 내용 수정시 내용은 필수다")
    @WithMockMember
    @Test
    void editChildCommentWithoutContent() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = findContextMember();
        Comment comment = commentRepository.save(
                commentFactory.createChildComment(member, post, parentComment));

        String request = om.writeValueAsString(EditCommentRequest.builder()
                .commentId(comment.getId())
                .content(null)
                .postId(post.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("내용을 입력하지 않았습니다"))
                .andDo(print());
    }

    @DisplayName("대댓글 내용 수정시 내용은 필수다2")
    @WithMockMember
    @Test
    void editChildCommentWithoutContent2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = findContextMember();
        Comment comment = commentRepository.save(
                commentFactory.createChildComment(member, post, parentComment));

        String request = om.writeValueAsString(EditCommentRequest.builder()
                .commentId(comment.getId())
                .content("")
                .postId(post.getId())
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("내용을 입력하지 않았습니다"))
                .andDo(print());
    }

    @DisplayName("댓글 내용 수정시 글 ID 번호는  필수다")
    @WithMockMember
    @Test
    void editCommentWithoutPostId() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = findContextMember();
        Comment comment = commentFactory.createComment(member, post);

        String request = om.writeValueAsString(EditCommentRequest.builder()
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .postId(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.postId").value("글 번호가 입력되지 않았습니다"))
                .andDo(print());
    }

    @DisplayName("대댓글 내용 수정시 글 ID 번호는 필수다")
    @WithMockMember
    @Test
    void editChildCommentWithoutPostID() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = findContextMember();
        Comment comment = commentRepository.save(
                commentFactory.createChildComment(member, post, parentComment));

        String request = om.writeValueAsString(EditCommentRequest.builder()
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .postId(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.postId").value("글 번호가 입력되지 않았습니다"))
                .andDo(print());
    }

    @DisplayName("로그인 하지 않은 상태로 댓글을 수정할수 없다")
    @Test
    void editCommentWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        String request = om.writeValueAsString(EditCommentRequest.builder()
                .commentId(comment.getId())
                .content("수정된 댓글입니다")
                .postId(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
                .andDo(print());
    }

    @DisplayName("댓글 삭제시 해당 댓글의 isDeleted 컬럼의 값이 true 가 된다")
    @WithMockMember
    @Test
    void deleteComment() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = findContextMember();

        Comment comment = commentFactory.createComment(member, post);

        //when
        ResultActions resultActions = mvc.perform(
                delete("/api/comments/{commentId}", comment.getId())
                        .with(csrf()));

        //then
        Comment findComment = commentRepository.findById(comment.getId()).get();
        assertThat(findComment.isDeleted()).isTrue();
    }

    @DisplayName("대댓글 삭제시 해당 대댓글의 isDeleted 컬럼의 값이 true 가 된다")
    @WithMockMember
    @Test
    void deleteChildComment() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = findContextMember();
        Comment comment = commentFactory.createChildComment(member, post, parentComment);

        //when
        ResultActions resultActions = mvc.perform(
                delete("/api/comments/{commentId}", comment.getId())
                        .with(csrf()));

        //then
        Comment findComment = commentRepository.findById(comment.getId()).get();
        assertThat(findComment.isDeleted()).isTrue();
    }

    @DisplayName("댓글 삭제시 삭제할 댓글의 Id 번호는 필수다")
    @WithMockMember
    @Test
    void deleteCommentWithoutCommentId() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = findContextMember();
        Comment comment = commentFactory.createComment(member, post);

        //when
        ResultActions resultActions = mvc.perform(delete("/api/comments/{commentId}", " ")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Path Variable 값이 입력되지 않았습니다"))
                .andExpect(jsonPath("$.validation.commentId").value("Long"))
                .andDo(print());
    }

    @DisplayName("로그인하지 않은 상태로 댓글을 삭제할 수 없다")
    @Test
    void deleteCommentWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        //when
        ResultActions resultActions = mvc.perform(
                delete("/api/comments/{commentId}", comment.getId())
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(unauthenticated());
    }


    @DisplayName("특정 글에 작성된 댓글과 대댓글 목록 조회")
    @WithMockMember
    @Test
    void viewCommentsInPost() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment1 = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember1", university), post);

        Comment comment2 = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember2", university), post);

        Comment childComment1 = commentFactory.createChildComment(
                memberFactory.createStudentMember("commentMember3", university), post, comment1);

        Comment childComment2 = commentFactory.createChildComment(
                memberFactory.createStudentMember("commentMember4", university), post, comment1);


        //when
        ResultActions resultActions = mvc.perform(
                get("/api/posts/comments")
                        .param("id", post.getId().toString())
                        .param("page", "1")
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.anonymousBoard").value(true))
                .andExpect(jsonPath("$.sizeRequest").value(100))
                .andExpect(jsonPath("$.actualSize").value(4))
                .andExpect(jsonPath("$.comments.size()").value(4))
                .andExpect(jsonPath("$.comments[0].id").value(comment1.getId()))
                .andExpect(jsonPath("$.comments[1].id").value(childComment1.getId()))
                .andExpect(jsonPath("$.comments[2].id").value(childComment2.getId()))
                .andExpect(jsonPath("$.comments[3].id").value(comment2.getId()));
    }

    @DisplayName("특정 글에 작성된 댓글 목록 조회시 글 Id 번호는 필수다")
    @WithMockMember
    @Test
    void viewCommentsInPostWithoutPostId() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();

        //when
        ResultActions resultActions = mvc.perform(
                get("/api/posts/comments")
                        .param("id", (String) null)
                        .param("page", "1")
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.id").value("글 번호가 입력되지 않았습니다"))
                .andDo(print());
    }

    @DisplayName("특정 글에 작성된 댓글 목록 조회시 페이지 정보가 없는 경우 1페이지가 조회된다")
    @WithMockMember
    @Test
    void viewCommentsInPostWithoutPageInfo() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        //when
        ResultActions resultActions = mvc.perform(
                get("/api/posts/comments")
                        .param("id", post.getId().toString())
                        .param("page", "1")
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andDo(print());
    }

    @DisplayName("로그인 하지 않은 상태로 글에 작성된 댓글을 볼수 없다")
    @Test
    void viewCommentsInPostWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        //when
        ResultActions resultActions = mvc.perform(
                get("/api/posts/comments")
                        .param("id", post.getId().toString())
                        .param("page", "1")
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
                .andDo(print());
    }

    @DisplayName("특정 글에 작성된 베스트 댓글 조회")
    @WithMockMember
    @Test
    void viewBestCommentInPost() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("member", university), post);

        IntStream.rangeClosed(1, 10).forEach(i -> {
            commentLikeFactory.createCommentLike(
                    memberFactory.createStudentMember("likeMember" + i, university), comment);
        });


        //when
        ResultActions resultActions = mvc.perform(
                get("/api/posts/{postId}/comments/best", post.getId())
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(comment.getId()))
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.content").value("내용입니다"))
                .andDo(print());
    }

    @DisplayName("특정 글에 좋아요 10개 이상 받은 댓글이 없는 경우 베스트 댓글은 존재하지 않는다")
    @WithMockMember
    @Test
    void viewBestCommentInPostThatHasNoBestComment() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment comment = commentFactory.createComment(
                memberFactory.createStudentMember("member", university), post);

        //when
        ResultActions resultActions = mvc.perform(
                get("/api/posts/{postId}/comments/best", post.getId())
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").doesNotExist())
                .andDo(print());
    }

    private Member findContextMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email).get();
    }
}
