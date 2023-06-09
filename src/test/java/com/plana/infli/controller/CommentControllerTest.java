package com.plana.infli.controller;


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
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.PostFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.comment.create.controller.CreateCommentRequest;
import com.plana.infli.web.dto.request.comment.delete.controller.DeleteCommentRequest;
import com.plana.infli.web.dto.request.comment.edit.controller.EditCommentRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
    private CommentFactory commentFactory;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private BoardFactory boardFactory;

    @Autowired
    private PostFactory postFactory;

    @Autowired
    private MemberFactory memberFactory;

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
                .andExpect(jsonPath("$.content").value("댓글입니다"))
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.parentComment").value(true))
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
        Comment comment = commentRepository.findAll().get(1);
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(comment.getId()))
                .andExpect(jsonPath("$.content").value("대댓글입니다"))
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.parentComment").value(false))
                .andDo(print());
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
    @WithMockMember(nickname = "youngjin")
    @Test
    void editComment() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberRepository.findByNickname("youngjin").get();
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
        Comment findComment = commentRepository.findById(comment.getId()).get();
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(findComment.getId()))
                .andExpect(jsonPath("$.content").value("수정된 댓글입니다"))
                .andExpect(jsonPath("writerId").value(member.getId()))
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.parentComment").value(true))
                .andExpect(jsonPath("$.edited").value(true))
                .andDo(print());
    }

    @DisplayName("대댓글 내용 수정시 DB에 값이 변경된다")
    @WithMockMember(nickname = "youngjin")
    @Test
    void editChildComment() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberRepository.findByNickname("youngjin").get();
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
        Comment findComment = commentRepository.findById(comment.getId()).get();
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(findComment.getId()))
                .andExpect(jsonPath("$.content").value("수정된 대댓글입니다"))
                .andExpect(jsonPath("writerId").value(member.getId()))
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.parentComment").value(false))
                .andExpect(jsonPath("$.edited").value(true))
                .andDo(print());
    }

    @DisplayName("댓글 내용 수정시 수정할 댓글 Id 번호는 필수다")
    @WithMockMember(nickname = "youngjin")
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
    @WithMockMember(nickname = "youngjin")
    @Test
    void editCommentWithoutContent() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberRepository.findByNickname("youngjin").get();
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
    @WithMockMember(nickname = "youngjin")
    @Test
    void editCommentWithoutContent2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberRepository.findByNickname("youngjin").get();
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
    @WithMockMember(nickname = "youngjin")
    @Test
    void editChildCommentWithoutContent() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberRepository.findByNickname("youngjin").get();
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
    @WithMockMember(nickname = "youngjin")
    @Test
    void editChildCommentWithoutContent2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberRepository.findByNickname("youngjin").get();
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
    @WithMockMember(nickname = "youngjin")
    @Test
    void editCommentWithoutPostId() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberRepository.findByNickname("youngjin").get();
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
    @WithMockMember(nickname = "youngjin")
    @Test
    void editChildCommentWithoutPostID() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberRepository.findByNickname("youngjin").get();
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
    @WithMockMember(nickname = "youngjin")
    @Test
    void deleteComment() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberRepository.findByNickname("youngjin").get();
        Comment comment = commentFactory.createComment(member, post);

        String request = om.writeValueAsString(DeleteCommentRequest.builder()
                .ids(List.of(comment.getId()))
                .build());

        //when
        ResultActions resultActions = mvc.perform(delete("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        Comment findComment = commentRepository.findById(comment.getId()).get();
        assertThat(findComment.isDeleted()).isTrue();
    }

    @DisplayName("대댓글 삭제시 해당 대댓글의 isDeleted 컬럼의 값이 true 가 된다")
    @WithMockMember(nickname = "youngjin")
    @Test
    void deleteChildComment() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Comment parentComment = commentFactory.createComment(
                memberFactory.createStudentMember("commentMember", university), post);

        Member member = memberRepository.findByNickname("youngjin").get();
        Comment comment = commentFactory.createChildComment(member, post, parentComment);

        String request = om.writeValueAsString(DeleteCommentRequest.builder()
                .ids(List.of(comment.getId()))
                .build());

        //when
        ResultActions resultActions = mvc.perform(delete("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        Comment findComment = commentRepository.findById(comment.getId()).get();
        assertThat(findComment.isDeleted()).isTrue();
    }

    @DisplayName("댓글 삭제시 삭제할 댓글의 Id 번호는 필수다")
    @WithMockMember(nickname = "youngjin")
    @Test
    void deleteCommentWithoutCommentId() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(
                memberFactory.createStudentMember("postMember", university), board);

        Member member = memberRepository.findByNickname("youngjin").get();
        Comment comment = commentFactory.createComment(member, post);

        String request = om.writeValueAsString(DeleteCommentRequest.builder()
                .ids(List.of())
                .build());

        //when
        ResultActions resultActions = mvc.perform(delete("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.ids").value("삭제할 댓글 ID가 입력되지 않았습니다"));
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

        String request = om.writeValueAsString(DeleteCommentRequest.builder()
                .ids(List.of(comment.getId()))
                .build());

        //when
        ResultActions resultActions = mvc.perform(delete("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
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
}
