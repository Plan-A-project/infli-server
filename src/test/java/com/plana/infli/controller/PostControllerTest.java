package com.plana.infli.controller;

import static com.plana.infli.domain.type.PostType.ANNOUNCEMENT;
import static com.plana.infli.domain.type.PostType.NORMAL;
import static com.plana.infli.domain.type.PostType.RECRUITMENT;
import static com.plana.infli.domain.type.Role.ADMIN;
import static com.plana.infli.domain.type.Role.COMPANY;
import static com.plana.infli.domain.type.Role.STUDENT_COUNCIL;
import static java.io.InputStream.*;
import static java.lang.String.*;
import static java.time.LocalDateTime.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.annotation.MockMvcTest;
import com.plana.infli.annotation.WithMockMember;
import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
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
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.popularboard.PopularBoardRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.BoardService;
import com.plana.infli.web.dto.request.post.create.normal.CreateNormalPostRequest;
import com.plana.infli.web.dto.request.post.create.recruitment.CreateRecruitmentPostRequest;
import com.plana.infli.web.dto.request.post.edit.normal.EditNormalPostRequest;
import com.plana.infli.web.dto.request.post.edit.recruitment.EditRecruitmentPostRequest;
import java.io.InputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@MockMvcTest
public class PostControllerTest {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private BoardFactory boardFactory;

    @Autowired
    private PostFactory postFactory;

    @Autowired
    private MemberFactory memberFactory;

    @AfterEach
    void tearDown() {
        commentLikeRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }

    @DisplayName("일반 글 작성 성공")
    @WithMockMember
    @Test
    void createPost() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);

        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(NORMAL)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/normal")
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
        assertThat(findPost.getPostType()).isEqualTo(NORMAL);
    }

    @DisplayName("일반 글 작성 실패 - 로그인을 하지 않은 경우")
    @Test
    void createAnonymousPostWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(NORMAL)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/normal")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("일반글 작성 실패 - 게시판 ID번호가 입력되지 않은 경우")
    @WithMockMember
    @Test
    void createPostWithoutBoardId() throws Exception {
        //given
        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
                .boardId(null)
                .title("제목입니다")
                .content("내용입니다")
                .postType(NORMAL)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/normal")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.boardId").value("게시판 ID를 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("일반글 작성 실패 - 제목을 입력하지 않은 경우")
    @WithMockMember
    @Test
    void createPostWithoutPostId() throws Exception {

        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
                .boardId(board.getId())
                .title(null)
                .content("내용입니다")
                .postType(NORMAL)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/normal")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.title").value("글 제목을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("일반글 작성 실패 - 제목을 입력하지 않은 경우2")
    @WithMockMember
    @Test
    void createPostWithoutPostId2() throws Exception {

        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
                .boardId(board.getId())
                .title("")
                .content("내용입니다")
                .postType(NORMAL)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/normal")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.title").value("글 제목을 입력해주세요"))
                .andDo(print());
    }


    @DisplayName("일반글 작성 실패 - 내용을 입력하지 않은 경우")
    @WithMockMember
    @Test
    void createPostWithoutContent() throws Exception {

        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content(null)
                .postType(NORMAL)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/normal")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("글 내용을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("일반글 작성 실패 - 내용을 입력하지 않은 경우2")
    @WithMockMember
    @Test
    void createPostWithoutContent2() throws Exception {

        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("")
                .postType(NORMAL)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/normal")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("글 내용을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("일반글 작성 실패 - 글 종류를 입력하지 않은 경우")
    @WithMockMember
    @Test
    void createPostWithoutPostType() throws Exception {

        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/normal")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.postType").value("글 종류를 선택해주세요"))
                .andDo(print());
    }


    @DisplayName("일반글 작성 실패 - 요청 본문은 필수다")
    @WithMockMember
    @Test
    void createPostWithoutRequestBody() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(post("/posts/normal")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 본문의 형식이 올바르지 않습니다"))
                .andDo(print());
    }


    @DisplayName("공지글 작성 성공")
    @WithMockMember(role = STUDENT_COUNCIL)
    @Test
    void createAnnouncementPost() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createCampusLifeBoard(university);

        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .postType(ANNOUNCEMENT)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/normal")
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
        assertThat(findPost.getPostType()).isEqualTo(ANNOUNCEMENT);
    }

    @DisplayName("모집글 작성 성공")
    @WithMockMember(role = COMPANY)
    @Test
    void createRecruitmentPost() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateRecruitmentPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        Post findPost = postRepository.findAll().get(0);
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(valueOf(findPost.getId())))
                .andDo(print());

        assertThat(findPost.getRecruitment().getCompanyName()).isEqualTo("카카오");
        assertThat(findPost.getRecruitment().getStartDate()).isEqualTo(of(2023, 8, 1, 0, 0));
        assertThat(findPost.getRecruitment().getEndDate()).isEqualTo(of(2023, 9, 1, 0, 0));
    }

    @DisplayName("모집글 작성 실패 - 로그인을 하지 않은 경우")
    @Test
    void createRecruitmentPostWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateRecruitmentPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("모집글 작성 실패 - 게시판 Id 번호는 필수다")
    @WithMockMember
    @Test
    void createRecruitmentPostWithoutBoardId() throws Exception {
        //given
        String request = om.writeValueAsString(CreateRecruitmentPostRequest.builder()
                .boardId(null)
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.boardId").value("게시판 ID를 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("모집글 작성 실패 - 제목은 필수다")
    @WithMockMember
    @Test
    void createRecruitmentPostWithoutTitle() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateRecruitmentPostRequest.builder()
                .boardId(board.getId())
                .title(null)
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.title").value("글 제목을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("모집글 작성 실패 - 제목은 필수다2")
    @WithMockMember
    @Test
    void createRecruitmentPostWithoutTitle2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateRecruitmentPostRequest.builder()
                .boardId(board.getId())
                .title("")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.title").value("글 제목을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("모집글 작성 실패 - 내용은 필수다")
    @WithMockMember
    @Test
    void createRecruitmentPostWithoutContent() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateRecruitmentPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content(null)
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("글 내용을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("모집글 작성 실패 - 내용은 필수다2")
    @WithMockMember
    @Test
    void createRecruitmentPostWithoutContent2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateRecruitmentPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("글 내용을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("모집글 작성 실패 - 모집 회사 이름은 필수다")
    @WithMockMember
    @Test
    void createRecruitmentPostWithoutCompanyName() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateRecruitmentPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName(null)
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.recruitmentCompanyName").value("모집 회사명을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("모집글 작성 실패 - 모집 회사 이름은 필수다2")
    @WithMockMember
    @Test
    void createRecruitmentPostWithoutCompanyName2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateRecruitmentPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.recruitmentCompanyName").value("모집 회사명을 입력해주세요"))
                .andDo(print());
    }


    @DisplayName("모집글 작성 실패 - 모집 시작일은 필수다")
    @WithMockMember
    @Test
    void createRecruitmentPostWithoutStartDate() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateRecruitmentPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(null)
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.recruitmentStartDate").value("모집 시작일을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("모집글 작성 실패 - 모집 종료일은 필수다")
    @WithMockMember
    @Test
    void createRecruitmentPostWithoutEndDate() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);

        String request = om.writeValueAsString(CreateRecruitmentPostRequest.builder()
                .boardId(board.getId())
                .title("제목입니다")
                .content("내용입니다")
                .recruitmentCompanyName("카카오")
                .recruitmentStartDate(of(2023, 9, 1, 0, 0))
                .recruitmentEndDate(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(post("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(request)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.recruitmentEndDate").value("모집 종료일을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("특정 게시판에 글 작성 권한이 있는지 여부 확인 - 권한 있는 경우")
    @WithMockMember
    @Test
    void checkMemberHasWritePermission() throws Exception {

        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);

        //when
        ResultActions resultActions = mvc.perform(
                get("/boards/{boardId}/permission?postType=NORMAL", board.getId())
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(print());
    }

    @DisplayName("특정 게시판에 글 작성 권한이 있는지 여부 확인 - 권한 없는 경우")
    @WithMockMember
    @Test
    void checkMemberHasWritePermission_NoPermission() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createCampusLifeBoard(university);

        //when
        ResultActions resultActions = mvc.perform(
                get("/boards/{boardId}/permission?postType=ANNOUNCEMENT", board.getId())
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("해당 권한이 없습니다"))
                .andDo(print());
    }

    @DisplayName("특정 게시판에 글 작성 권한이 있는지 여부 확인 실패 - 로그인을 하지 않은 경우")
    @Test
    void checkMemberHasWritePermissionWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);

        //when
        ResultActions resultActions = mvc.perform(
                get("/boards/{boardId}/permission?postType=ANNOUNCEMENT", board.getId())
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("특정 게시판에 글 작성 권한이 있는지 여부 확인 실패 - 글 종류를 선택하지 않은 경우")
    @WithMockMember
    @Test
    void checkMemberHasWritePermissionWithoutPostType() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createCampusLifeBoard(university);

        //when
        ResultActions resultActions = mvc.perform(
                get("/boards/{boardId}/permission", board.getId())
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request Parameter가 누락되었습니다"))
                .andExpect(jsonPath("$.validation.postType").value("파라미터 타입 : PostType"))
                .andDo(print());
    }

    @DisplayName("특정 게시판에 글 작성 권한이 있는지 여부 확인 실패 - 게시판 ID 번호를 입력하지 않은 경우")
    @WithMockMember
    @Test
    void checkMemberHasWritePermissionWithoutBoardId() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(
                get("/boards/{boardId}/permission", " ")
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Path Variable 값이 입력되지 않았습니다"))
                .andExpect(jsonPath("$.validation.boardId").value("Long"))
                .andDo(print());
    }

    @DisplayName("특정글에 사진 업로드 실패 - 로그인을 하지 않은 경우")
    @Test
    void uploadImagesInPost() throws Exception {
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("member", university);
        Post post = postFactory.createNormalPost(member, board);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", IMAGE_PNG_VALUE, "test image content".getBytes());

        //when
        ResultActions resultActions = mvc.perform(
                multipart("/posts/{postId}/images", post.getId())
                        .file(multipartFile)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("특정글에 사진 업로드 실패 - 빈 파일을 업로드 한 경우")
    @WithMockMember
    @Test
    void uploadEmptyImagesInPost() throws Exception {
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("member", university);
        Post post = postFactory.createNormalPost(member, board);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", IMAGE_PNG_VALUE, nullInputStream());

        //when
        ResultActions resultActions = mvc.perform(
                multipart("/posts/{postId}/images", post.getId())
                        .file(multipartFile)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("멀티 파트 요청에서 파일이 누락되었습니다"))
                .andDo(print());
    }

    @DisplayName("특정글에 사진 업로드 실패 - 이름이 없는 파일을 업로드한 경우")
    @WithMockMember
    @Test
    void uploadImageThatHasNoName() throws Exception {
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("member", university);
        Post post = postFactory.createNormalPost(member, board);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", null, IMAGE_PNG_VALUE, "test image content".getBytes());

        //when
        ResultActions resultActions = mvc.perform(
                multipart("/posts/{postId}/images", post.getId())
                        .file(multipartFile)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("멀티 파트 요청에서 파일이 누락되었습니다"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 일반글 수정 성공")
    @WithMockMember
    @Test
    void editMyNormalPost() throws Exception {
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createNormalPost(member, board);

        String json = om.writeValueAsString(EditNormalPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("https://aws.com.example.jpg")
                .build());
        //when
        ResultActions resultActions = mvc.perform(
                patch("/posts/normal")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("글 수정 완료"))
                .andDo(print());

        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(findPost.getContent()).isEqualTo("수정된 내용입니다");
        assertThat(findPost.getTitle()).isEqualTo("수정된 제목입니다");
        assertThat(findPost.getThumbnailUrl()).isEqualTo("https://aws.com.example.jpg");
    }

    @DisplayName("내가 작성한 일반글 수정 실패 - 로그인을 하지 않은 경우")
    @Test
    void editMyNormalPostWithoutLogin() throws Exception {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("member", university);
        Post post = postFactory.createNormalPost(member, board);

        String json = om.writeValueAsString(EditNormalPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("https://aws.com.example.jpg")
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                patch("/posts/normal")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 일반글 수정 실패 - 글 Id 번호는 필수다")
    @WithMockMember
    @Test
    void editMyNormalPostWithoutPostId() throws Exception {

        //given
        String json = om.writeValueAsString(EditNormalPostRequest.builder()
                .postId(null)
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("https://aws.com.example.jpg")
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                patch("/posts/normal")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.postId").value("글 Id 번호를 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 일반글 수정 실패 - 제목은 필수다")
    @WithMockMember
    @Test
    void editMyNormalPostWithoutTitle() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createNormalPost(member, board);

        String json = om.writeValueAsString(EditNormalPostRequest.builder()
                .postId(post.getId())
                .title(null)
                .content("수정된 내용입니다")
                .thumbnailUrl("https://aws.com.example.jpg")
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                patch("/posts/normal")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.title").value("글 제목을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 일반글 수정 실패 - 제목은 필수다2")
    @WithMockMember
    @Test
    void editMyNormalPostWithoutTitle2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createNormalPost(member, board);

        String json = om.writeValueAsString(EditNormalPostRequest.builder()
                .postId(post.getId())
                .title("")
                .content("수정된 내용입니다")
                .thumbnailUrl("https://aws.com.example.jpg")
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                patch("/posts/normal")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.title").value("글 제목을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 일반글 수정 실패 - 내용은 필수다")
    @WithMockMember
    @Test
    void editMyNormalPostWithoutContent() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createNormalPost(member, board);

        String json = om.writeValueAsString(EditNormalPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content(null)
                .thumbnailUrl("https://aws.com.example.jpg")
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                patch("/posts/normal")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("글 내용을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 일반글 수정 실패 - 내용은 필수다2")
    @WithMockMember
    @Test
    void editMyNormalPostWithoutContent2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createNormalPost(member, board);

        String json = om.writeValueAsString(EditNormalPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("")
                .thumbnailUrl("https://aws.com.example.jpg")
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                patch("/posts/normal")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("글 내용을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 일반글 수정 성공 - 썸네일 Url은 null값이 허용된다")
    @WithMockMember
    @Test
    void editMyNormalPostWithoutThumbnailUrl() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createAnonymousBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createNormalPost(member, board);

        String json = om.writeValueAsString(EditNormalPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(
                patch("/posts/normal")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isOk());

        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(findPost.getThumbnailUrl()).isNull();
    }

    @DisplayName("내가 작성한 모집글 수정 성공")
    @WithMockMember(role = COMPANY)
    @Test
    void editMyRecruitmentPost() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createRecruitmentPost(member, board);

        String json = om.writeValueAsString(EditRecruitmentPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("https://aws.com.example.jpg")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());
        //when
        ResultActions resultActions = mvc.perform(patch("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("글 수정 완료"))
                .andDo(print());

        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(findPost.getContent()).isEqualTo("수정된 내용입니다");
        assertThat(findPost.getTitle()).isEqualTo("수정된 제목입니다");
        assertThat(findPost.getThumbnailUrl()).isEqualTo("https://aws.com.example.jpg");
        assertThat(findPost.getRecruitment().getCompanyName()).isEqualTo("삼성전자");
        assertThat(findPost.getRecruitment().getStartDate()).isEqualTo(of(2023, 8, 1, 0, 0));
        assertThat(findPost.getRecruitment().getEndDate()).isEqualTo(of(2023, 9, 1, 0, 0));
    }


    @DisplayName("내가 작성한 모집글 수정 성공 - 썸네일 url은 null 값이 허용된다")
    @WithMockMember(role = COMPANY)
    @Test
    void editMyRecruitmentPostWithoutThumbnailUrl() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createRecruitmentPost(member, board);

        String json = om.writeValueAsString(EditRecruitmentPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl(null)
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("글 수정 완료"))
                .andDo(print());

        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(findPost.getThumbnailUrl()).isNull();
    }


    @DisplayName("내가 작성한 모집글 수정 실패 - 로그인을 하지 않은 경우")
    @Test
    void editMyRecruitmentPostWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = memberFactory.createAdminMember(university);
        Post post = postFactory.createRecruitmentPost(member, board);

        String json = om.writeValueAsString(EditRecruitmentPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("https://aws.com.example.jpg")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());
        //when
        ResultActions resultActions = mvc.perform(patch("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 모집글 수정 실패 - 글 Id 번호는 필수다")
    @WithMockMember(role = COMPANY)
    @Test
    void editMyRecruitmentPostWithoutPostId() throws Exception {
        //given
        String json = om.writeValueAsString(EditRecruitmentPostRequest.builder()
                .postId(null)
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("https://aws.com.example.jpg")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());
        //when
        ResultActions resultActions = mvc.perform(patch("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.postId").value("글 Id 번호를 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 모집글 수정 실패 - 글 제목은 필수다")
    @WithMockMember(role = COMPANY)
    @Test
    void editMyRecruitmentPostWithoutTitle() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createRecruitmentPost(member, board);

        String json = om.writeValueAsString(EditRecruitmentPostRequest.builder()
                .postId(post.getId())
                .title(null)
                .content("수정된 내용입니다")
                .thumbnailUrl("https://aws.com.example.jpg")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());
        //when
        ResultActions resultActions = mvc.perform(patch("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.title").value("글 제목을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 모집글 수정 실패 - 글 제목은 필수다2")
    @WithMockMember(role = COMPANY)
    @Test
    void editMyRecruitmentPostWithoutTitle2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createRecruitmentPost(member, board);

        String json = om.writeValueAsString(EditRecruitmentPostRequest.builder()
                .postId(post.getId())
                .title("")
                .content("수정된 내용입니다")
                .thumbnailUrl("https://aws.com.example.jpg")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());
        //when
        ResultActions resultActions = mvc.perform(patch("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.title").value("글 제목을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 모집글 수정 실패 - 글 내용은 필수다")
    @WithMockMember(role = COMPANY)
    @Test
    void editMyRecruitmentPostWithoutContent() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createRecruitmentPost(member, board);

        String json = om.writeValueAsString(EditRecruitmentPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content(null)
                .thumbnailUrl("https://aws.com.example.jpg")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());
        //when
        ResultActions resultActions = mvc.perform(patch("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("글 내용을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 모집글 수정 실패 - 글 내용은 필수다2")
    @WithMockMember(role = COMPANY)
    @Test
    void editMyRecruitmentPostWithoutContent2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createRecruitmentPost(member, board);

        String json = om.writeValueAsString(EditRecruitmentPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("")
                .thumbnailUrl("https://aws.com.example.jpg")
                .recruitmentCompanyName("삼성전자")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());
        //when
        ResultActions resultActions = mvc.perform(patch("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("글 내용을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 모집글 수정 실패 - 모집 회사 이름은 필수다")
    @WithMockMember(role = COMPANY)
    @Test
    void editMyRecruitmentPostWithoutCompanyName() throws Exception {

        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createRecruitmentPost(member, board);

        String json = om.writeValueAsString(EditRecruitmentPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("")
                .thumbnailUrl("https://aws.com.example.jpg")
                .recruitmentCompanyName(null)
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.recruitmentCompanyName").value("회사명을 입력해주세요"))
                .andDo(print());
    }


    @DisplayName("내가 작성한 모집글 수정 실패 - 모집 회사 이름은 필수다2")
    @WithMockMember(role = COMPANY)
    @Test
    void editMyRecruitmentPostWithoutCompanyName2() throws Exception {

        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createRecruitmentPost(member, board);

        String json = om.writeValueAsString(EditRecruitmentPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("https://aws.com.example.jpg")
                .recruitmentCompanyName("")
                .recruitmentStartDate(of(2023, 8, 1, 0, 0))
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.recruitmentCompanyName").value("회사명을 입력해주세요"))
                .andDo(print());
    }


    @DisplayName("내가 작성한 모집글 수정 실패 - 모집 시작일은 필수다")
    @WithMockMember(role = COMPANY)
    @Test
    void editMyRecruitmentPostWithoutStartDate() throws Exception {

        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createRecruitmentPost(member, board);

        String json = om.writeValueAsString(EditRecruitmentPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("수정된 내용입니다")
                .thumbnailUrl("https://aws.com.example.jpg")
                .recruitmentCompanyName("")
                .recruitmentStartDate(null)
                .recruitmentEndDate(of(2023, 9, 1, 0, 0))
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.recruitmentStartDate").value("모집 시작일을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("내가 작성한 모집글 수정 실패 - 모집 종료일은 필수다")
    @WithMockMember(role = COMPANY)
    @Test
    void editMyRecruitmentPostWithoutEndDate() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createRecruitmentPost(member, board);

        String json = om.writeValueAsString(EditRecruitmentPostRequest.builder()
                .postId(post.getId())
                .title("수정된 제목입니다")
                .content("")
                .thumbnailUrl("https://aws.com.example.jpg")
                .recruitmentCompanyName("쿠팡")
                .recruitmentStartDate(of(2023, 9, 1, 0, 0))
                .recruitmentEndDate(null)
                .build());

        //when
        ResultActions resultActions = mvc.perform(patch("/posts/recruitment")
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.recruitmentEndDate").value("모집 종료일을 입력해주세요"))
                .andDo(print());
    }

    @DisplayName("글 삭제 성공 - 내가 작성한 글 삭제")
    @WithMockMember
    @Test
    void deleteMyPost() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = findContextMember();
        Post post = postFactory.createRecruitmentPost(member, board);

        //when
        ResultActions resultActions = mvc.perform(delete("/posts/{postId}", post.getId())
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());

        Post deletedPost = postRepository.findPostById(post.getId()).get();
        assertThat(deletedPost.isDeleted()).isTrue();
    }

    @DisplayName("글 삭제 성공  - 관리자가 타인이 작성한 글 삭제")
    @WithMockMember(role = ADMIN)
    @Test
    void deletePostByAdmin() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = memberFactory.createVerifiedCompanyMember(university);
        Post post = postFactory.createRecruitmentPost(member, board);

        //when
        ResultActions resultActions = mvc.perform(delete("/posts/{postId}", post.getId())
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());

        Post deletedPost = postRepository.findPostById(post.getId()).get();
        assertThat(deletedPost.isDeleted()).isTrue();
    }

    @DisplayName("글 삭제 실패 - 로그인을 하지 않은 경우")
    @Test
    void deleteMyPostWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = memberFactory.createVerifiedCompanyMember(university);
        Post post = postFactory.createRecruitmentPost(member, board);

        //when
        ResultActions resultActions = mvc.perform(delete("/posts/{postId}", post.getId())
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("글 삭제 실패 - 삭제할 글 Id 번호를 입력하지 않은 경우")
    @WithMockMember
    @Test
    void deleteMyPostWithoutPostId() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(delete("/posts/{postId}", " ")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Path Variable 값이 입력되지 않았습니다"))
                .andExpect(jsonPath("$.validation.postId").value("Long"))
                .andDo(print());
    }

    @DisplayName("글 단건 조회 성공 - 일반글인 경우")
    @WithMockMember
    @Test
    void loadSingleNormalPost() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createClubBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        //when
        ResultActions resultActions = mvc.perform(get("/posts/{postId}", post.getId())
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.boardName").value(board.getBoardName()))
                .andExpect(jsonPath("$.boardId").value(board.getId()))
                .andExpect(jsonPath("$.postType").value(NORMAL.name()))
                .andExpect(jsonPath("$.nickname").value(member.getBasicCredentials().getNickname()))
                .andExpect(jsonPath("$.content").value(post.getContent()))
                .andExpect(jsonPath("$.myPost").value(false))
                .andExpect(jsonPath("$.admin").value(false))
                .andExpect(jsonPath("$.recruitment").isEmpty())
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.title").value(post.getTitle()))
                .andExpect(jsonPath("$.commentCount").value(0))
                .andExpect(jsonPath("$.pressedLike").value(false))
                .andExpect(jsonPath("$.likeCount").value(post.getLikes().size()))
                .andExpect(jsonPath("$.viewCount").value(1))
                .andExpect(jsonPath("$.thumbnailUrl").value(post.getThumbnailUrl()))
                .andDo(print());
    }

    @DisplayName("글 단건 조회 성공 - 모집글인 경우")
    @WithMockMember
    @Test
    void loadSingleRecruitmentPost() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createEmploymentBoard(university);
        Member member = memberFactory.createVerifiedCompanyMember(university);
        Post post = postFactory.createRecruitmentPost(member, board);

        //when
        ResultActions resultActions = mvc.perform(get("/posts/{postId}", post.getId())
                .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.boardName").value(board.getBoardName()))
                .andExpect(jsonPath("$.boardId").value(board.getId()))
                .andExpect(jsonPath("$.postType").value(RECRUITMENT.name()))
                .andExpect(jsonPath("$.nickname").value(member.getBasicCredentials().getNickname()))
                .andExpect(jsonPath("$.content").value(post.getContent()))
                .andExpect(jsonPath("$.myPost").value(false))
                .andExpect(jsonPath("$.admin").value(false))
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.title").value(post.getTitle()))
                .andExpect(jsonPath("$.commentCount").value(0))
                .andExpect(jsonPath("$.pressedLike").value(false))
                .andExpect(jsonPath("$.likeCount").value(post.getLikes().size()))
                .andExpect(jsonPath("$.viewCount").value(1))
                .andExpect(jsonPath("$.thumbnailUrl").value(post.getThumbnailUrl()))
                .andExpect(jsonPath("$.recruitment.companyName").value(post.getRecruitment().getCompanyName()))
                .andDo(print());
    }

    void loadSinglePostWithoutLogin() throws Exception {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createClubBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        //when
        ResultActions resultActions = mvc.perform(get("/posts/{postId}", post.getId())
                .with(csrf()));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("인증을 하지 못하였습니다. 로그인 후 이용해 주세요"))
                .andDo(print());
    }

    @DisplayName("글 단건 조회 성공 - 글 Id 번호를 입력하지 않은 경우")
    @WithMockMember
    @Test
    void loadSinglePostWithoutPostId() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createClubBoard(university);
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);
        Post post = postFactory.createNormalPost(member, board);

        //when
        ResultActions resultActions = mvc.perform(get("/posts/{postId}", " ")
                .with(csrf()));

        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Path Variable 값이 입력되지 않았습니다"))
                .andDo(print());
    }

    Member findContextMember() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findActiveMemberBy(username).get();
    }
}
