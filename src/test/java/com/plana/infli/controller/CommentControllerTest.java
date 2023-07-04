package com.plana.infli.controller;

import static org.springframework.security.test.context.support.TestExecutionEvent.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.University;
import com.plana.infli.dummy.DummyObject;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.comment.create.controller.CreateCommentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:db/teardown.sql")
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public class CommentControllerTest extends DummyObject {

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


    @BeforeEach
    public void setUp() {

    }

//    @DisplayName("댓글을 작성하면 DB에 값이 저장된다")
//    @WithUserDetails(value = "a", setupBefore = TEST_EXECUTION)
//    @Test
//    void writeNewComment() throws Exception {
//        //given
//        University university = universityRepository.save(newUniversity());
//\
//        Post post = postRepository.save(newPost(memberRepository.save(newStudentMember(university)),
//                boardRepository.save(newAnonymousBoard(university))));
//
//        Member member = memberRepository.save(newStudentMember(university));
//
//        String request = om.writeValueAsString(CreateCommentRequest.builder()
//                .postId(post.getId())
//                .parentCommentId(null)
//                .content("댓글입니다")
//                .build());
//
//        //when
//        ResultActions resultActions = mvc.perform(post("/comments")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(request)
//                .with(csrf()));
//
//        //then
//        resultActions.andExpect(status().isNotFound()).andDo(print());
//        resultActions.andExpect(jsonPath("$.content").value("댓글입니다"));
//    }

}
