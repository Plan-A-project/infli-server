package com.plana.infli.controller;

import static com.plana.infli.domain.type.BoardType.ACTIVITY;
import static com.plana.infli.domain.type.BoardType.ANONYMOUS;
import static com.plana.infli.domain.type.BoardType.CAMPUS_LIFE;
import static com.plana.infli.domain.type.BoardType.CLUB;
import static com.plana.infli.domain.type.BoardType.EMPLOYMENT;
import static com.plana.infli.domain.type.PostType.*;
import static com.plana.infli.domain.type.Role.ADMIN;
import static com.plana.infli.domain.type.Role.COMPANY;
import static com.plana.infli.domain.type.Role.STUDENT;
import static com.plana.infli.domain.type.Role.STUDENT_COUNCIL;
import static java.lang.String.valueOf;
import static java.time.LocalDateTime.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.annotation.MockMvcTest;
import com.plana.infli.annotation.WithMockMember;
import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostLike;
import com.plana.infli.domain.University;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@MockMvcTest
public class PostLikeControllerTest {

    @Autowired
    private MockMvc mvc;

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
    private PostLikeFactory postLikeFactory;

    @Autowired
    private MemberFactory memberFactory;

    @AfterEach
    void tearDown() {
        postLikeRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }

    @DisplayName("글 좋아요 누르기 성공")
    @Test
    @WithMockMember
    void pressPostLike() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(memberFactory.createAdminMember(university),
                board);

        //when
        ResultActions resultActions = mvc.perform(post("/posts/{postId}/likes", post.getId())
                .with(csrf()));

        //then
        resultActions.andExpect(status().isCreated())
                .andDo(print());
        assertThat(postLikeRepository.count()).isEqualTo(1);
    }

    @DisplayName("글 좋아요 누르기 실패 - 로그인 하지 않은 상태로 글 좋아요를 누를수 없다")
    @Test
    void pressPostLikeWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(memberFactory.createAdminMember(university),
                board);

        //when
        ResultActions resultActions = mvc.perform(post("/posts/{postId}/likes", post.getId())
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
        assertThat(postLikeRepository.count()).isEqualTo(0);
    }

    @DisplayName("글 좋아요 누르기 실패 - 좋아요를 누를 글 ID 번호는 필수다")
    @Test
    @WithMockMember
    void postIdToPressLikeIsMandatory() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/posts/{postId}/likes", " ")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Path Variable 값이 입력되지 않았습니다"))
                .andExpect(jsonPath("$.validation.postId").value("Long"))
                .andDo(print());

        assertThat(postLikeRepository.count()).isEqualTo(0);
    }

    @DisplayName("글 좋아요 누르기 실패 - 좋아요를 누를 글 ID 번호는 숫자 형식으로 입력해야 된다")
    @Test
    @WithMockMember
    void postIdToPressLikeMustBeInNumberFormat() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/posts/{postId}/likes", "A")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.postId").value("필요한 파라미터 타입 : Long"))
                .andDo(print());

        assertThat(postLikeRepository.count()).isEqualTo(0);
    }

    @DisplayName("글 좋아요 취소 성공")
    @Test
    @WithMockMember
    void cancelPostLike() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(memberFactory.createAdminMember(university),
                board);
        Member member = findContextMember();

        PostLike postLike = postLikeFactory.createPostLike(member, post);

        //when
        ResultActions resultActions = mvc.perform(delete("/posts/{postId}/likes", post.getId())
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk()).andDo(print());
        assertThat(postLikeRepository.count()).isZero();
    }


    @DisplayName("글 좋아요 취소 실패 - 로그인 하지 않은 상태로 글 좋아요 취소를 할수 없다")
    @Test
    void cancelPostLikeWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createNormalPost(memberFactory.createAdminMember(university),
                board);

        //when
        ResultActions resultActions = mvc.perform(delete("/posts/{postId}/likes", post.getId())
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("글 좋아요 취소 실패 - 취소를 할 글 ID 번호는 필수다")
    @Test
    @WithMockMember
    void postIdToCancelLikeIsMandatory() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(delete("/posts/{postId}/likes", " ")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Path Variable 값이 입력되지 않았습니다"))
                .andExpect(jsonPath("$.validation.postId").value("Long"))
                .andDo(print());
    }

    @DisplayName("글 좋아요 취소 실패 - 글 ID 번호는 숫자 형식이여야 한다")
    @Test
    @WithMockMember
    void postIdToCancelLikeMustBeInNumberFormat() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(delete("/posts/{postId}/likes", "a")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.postId").value("필요한 파라미터 타입 : Long"))
                .andDo(print());
    }

    private Member findContextMember() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findActiveMemberBy(username).get();
    }
}
