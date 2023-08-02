package com.plana.infli.controller;

import static com.plana.infli.domain.PostType.*;
import static java.lang.String.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.annotation.MockMvcTest;
import com.plana.infli.annotation.WithMockMember;
import com.plana.infli.domain.Board;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.domain.University;
import com.plana.infli.factory.BoardFactory;
import com.plana.infli.factory.CommentFactory;
import com.plana.infli.factory.CommentLikeFactory;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.PopularBoardFactory;
import com.plana.infli.factory.PostFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.commentlike.CommentLikeRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.popularboard.PopularBoardRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.BoardService;
import com.plana.infli.web.dto.request.post.create.CreatePostRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@MockMvcTest
public class PostControllerTest {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BoardService boardService;

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
    private PopularBoardRepository popularBoardRepository;

    @Autowired
    private CommentFactory commentFactory;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private PopularBoardFactory popularBoardFactory;

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
        popularBoardRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }


    @DisplayName("글 생성")
    @WithMockMember
    @Test
    void checkDefaultPopularBoardExists() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);

        String request = om.writeValueAsString(CreatePostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(NORMAL)
                .recruitment(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/api/posts")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        Post findPost = postRepository.findAll().get(0);
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(valueOf(findPost.getId())))
                .andDo(print());

        assertThat(findPost.getTitle()).isEqualTo("제목입니다");
        assertThat(findPost.getBoard().getId()).isEqualTo(board.getId());
        assertThat(findPost.getContent()).isEqualTo("내용입니다");
        assertThat(findPost.getThumbnailUrl()).isNull();
    }
}
