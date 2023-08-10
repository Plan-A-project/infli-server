//package com.plana.infli.controller;
//
//import static com.plana.infli.domain.PostType.*;
//import static java.lang.String.*;
//import static java.time.LocalDateTime.*;
//import static org.assertj.core.api.Assertions.*;
//import static org.springframework.http.MediaType.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.plana.infli.annotation.MockMvcTest;
//import com.plana.infli.annotation.WithMockMember;
//import com.plana.infli.domain.Board;
//import com.plana.infli.domain.Member;
//import com.plana.infli.domain.Post;
//import com.plana.infli.domain.University;
//import com.plana.infli.factory.BoardFactory;
//import com.plana.infli.factory.CommentFactory;
//import com.plana.infli.factory.CommentLikeFactory;
//import com.plana.infli.factory.MemberFactory;
//import com.plana.infli.factory.PopularBoardFactory;
//import com.plana.infli.factory.PostFactory;
//import com.plana.infli.factory.UniversityFactory;
//import com.plana.infli.repository.board.BoardRepository;
//import com.plana.infli.repository.comment.CommentRepository;
//import com.plana.infli.repository.commentlike.CommentLikeRepository;
//import com.plana.infli.repository.member.MemberRepository;
//import com.plana.infli.repository.popularboard.PopularBoardRepository;
//import com.plana.infli.repository.post.PostRepository;
//import com.plana.infli.repository.university.UniversityRepository;
//import com.plana.infli.service.BoardService;
//import com.plana.infli.web.dto.request.post.create.normal.CreateNormalPostRequest;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//
//@MockMvcTest
//public class PostControllerTest {
//
//    @Autowired
//    private ObjectMapper om;
//
//    @Autowired
//    private MockMvc mvc;
//
//    @Autowired
//    private BoardService boardService;
//
//    @Autowired
//    private UniversityRepository universityRepository;
//
//    @Autowired
//    private PostRepository postRepository;
//
//    @Autowired
//    private MemberRepository memberRepository;
//
//    @Autowired
//    private BoardRepository boardRepository;
//
//    @Autowired
//    private CommentRepository commentRepository;
//
//    @Autowired
//    private CommentLikeRepository commentLikeRepository;
//
//    @Autowired
//    private PopularBoardRepository popularBoardRepository;
//
//    @Autowired
//    private CommentFactory commentFactory;
//
//    @Autowired
//    private UniversityFactory universityFactory;
//
//    @Autowired
//    private PopularBoardFactory popularBoardFactory;
//
//    @Autowired
//    private BoardFactory boardFactory;
//
//    @Autowired
//    private PostFactory postFactory;
//
//    @Autowired
//    private MemberFactory memberFactory;
//
//    @Autowired
//    private CommentLikeFactory commentLikeFactory;
//
//    @AfterEach
//    void tearDown() {
//        commentLikeRepository.deleteAllInBatch();
//        commentRepository.deleteAllInBatch();
//        postRepository.deleteAllInBatch();
//        popularBoardRepository.deleteAllInBatch();
//        boardRepository.deleteAllInBatch();
//        memberRepository.deleteAllInBatch();
//        universityRepository.deleteAllInBatch();
//    }
//
////    @DisplayName("익명 글 생성")
////    @WithMockMember
////    @Test
////    void createPost() throws Exception {
////        //given
////        University university = universityRepository.findByName("푸단대학교").get();
////        Board board = boardFactory.createAnonymousBoard(university);
////
////        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
////                .boardId(board.getId())
////                .title("제목입니다")
////                .content("내용입니다")
////                .postType(NORMAL)
////                .build());
////
////        //when
////        ResultActions resultActions = mvc.perform(post("/api/posts")
////                .contentType(APPLICATION_JSON)
////                .content(request)
////                .with(csrf()));
////
////        //then
////        Post findPost = postRepository.findAll().get(0);
////        resultActions.andExpect(status().isCreated())
////                .andExpect(content().string(valueOf(findPost.getId())))
////                .andDo(print());
////
////        assertThat(findPost.getTitle()).isEqualTo("제목입니다");
////        assertThat(findPost.getBoard().getId()).isEqualTo(board.getId());
////        assertThat(findPost.getContent()).isEqualTo("내용입니다");
////        assertThat(findPost.getPostType()).isEqualTo(NORMAL);
////    }
////
////    @DisplayName("로그인을 하지 않은 상태로 글 작성을 할수 업다")
////    @Test
////    void createAnonymousPostWithoutLogin() throws Exception {
////        //given
////        University university = universityFactory.createUniversity("푸단대학교");
////        Board board = boardFactory.createEmploymentBoard(university);
////
////        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
////                .boardId(board.getId())
////                .title("제목입니다")
////                .content("내용입니다")
////                .postType(NORMAL)
////                .build());
////
////        //when
////        ResultActions resultActions = mvc.perform(post("/api/posts")
////                .contentType(APPLICATION_JSON)
////                .content(request)
////                .with(csrf()));
////
////        //then
////        resultActions.andExpect(status().isUnauthorized())
////                .andExpect(jsonPath("$.message").value("토큰이 올바르지 않습니다."))
////                .andDo(print());
////
////    }
////
////    @DisplayName("글 작성시 게시판 Id 번호는 필수다")
////    @WithMockMember
////    @Test
////    void createPostWithoutBoardId() throws Exception {
////
////        //given
////        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
////                .boardId(null)
////                .title("제목입니다")
////                .content("내용입니다")
////                .postType(NORMAL)
////                .build());
////
////        //when
////        ResultActions resultActions = mvc.perform(post("/api/posts")
////                .contentType(APPLICATION_JSON)
////                .content(request)
////                .with(csrf()));
////
////        //then
////        resultActions.andExpect(status().isBadRequest())
////                .andExpect(jsonPath("$.validation.boardId").value("게시판 ID를 입력해주세요"))
////                .andDo(print());
////    }
////
////    @DisplayName("글 작성시 제목은 필수다")
////    @WithMockMember
////    @Test
////    void createPostWithoutPostId() throws Exception {
////
////        //given
////        University university = universityRepository.findByName("푸단대학교").get();
////        Board board = boardFactory.createEmploymentBoard(university);
////
////        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
////                .boardId(board.getId())
////                .title(null)
////                .content("내용입니다")
////                .postType(NORMAL)
////                .build());
////
////        //when
////        ResultActions resultActions = mvc.perform(post("/api/posts")
////                .contentType(APPLICATION_JSON)
////                .content(request)
////                .with(csrf()));
////
////        //then
////        resultActions.andExpect(status().isBadRequest())
////                .andExpect(jsonPath("$.validation.title").value("글 제목을 입력해주세요"))
////                .andDo(print());
////    }
////
////    @DisplayName("글 작성시 내용은 필수다")
////    @WithMockMember
////    @Test
////    void createPostWithoutContent() throws Exception {
////
////        //given
////        University university = universityRepository.findByName("푸단대학교").get();
////        Board board = boardFactory.createEmploymentBoard(university);
////
////        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
////                .boardId(board.getId())
////                .title("제목입니다")
////                .content(null)
////                .postType(NORMAL)
////                .build());
////
////        //when
////        ResultActions resultActions = mvc.perform(post("/api/posts")
////                .contentType(APPLICATION_JSON)
////                .content(request)
////                .with(csrf()));
////
////        //then
////        resultActions.andExpect(status().isBadRequest())
////                .andExpect(jsonPath("$.validation.content").value("글 내용을 입력해주세요"))
////                .andDo(print());
////    }
////
////    @DisplayName("글 작성시 글 종류 선택은 필수다")
////    @WithMockMember
////    @Test
////    void createPostWithoutPostType() throws Exception {
////
////        //given
////        University university = universityRepository.findByName("푸단대학교").get();
////        Board board = boardFactory.createEmploymentBoard(university);
////
////        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
////                .boardId(board.getId())
////                .title("제목입니다")
////                .content("내용입니다")
////                .postType(null)
////                .build());
////
////        //when
////        ResultActions resultActions = mvc.perform(post("/api/posts")
////                .contentType(APPLICATION_JSON)
////                .content(request)
////                .with(csrf()));
////
////        //then
////        resultActions.andExpect(status().isBadRequest())
////                .andExpect(jsonPath("$.validation.postType").value("게시글 종류를 선택해주세요"))
////                .andDo(print());
////    }
////
////    @DisplayName("모집글 글 작성시 모집 회사 입력은 필수다")
////    @WithMockMember
////    @Test
////    void createRecruitmentPostWithoutCompanyName() throws Exception {
////
////        //given
////        University university = universityRepository.findByName("푸단대학교").get();
////        Board board = boardFactory.createEmploymentBoard(university);
////
////        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
////                .boardId(board.getId())
////                .title("제목입니다")
////                .content("내용입니다")
////                .postType(RECRUITMENT)
////                .build());
////
////        //when
////        ResultActions resultActions = mvc.perform(post("/api/posts")
////                .contentType(APPLICATION_JSON)
////                .content(request)
////                .with(csrf()));
////
////        //then
////        resultActions.andExpect(status().isBadRequest())
////                .andExpect(jsonPath("$['validation']['recruitment.companyName']").value("회사명을 입력해주세요"))
////                .andDo(print());
////    }
//
////    @DisplayName("모집글 글 작성시 모집 시작일은 필수다")
////    @WithMockMember
////    @Test
////    void createRecruitmentPostWithoutStartDate() throws Exception {
////
////        //given
////        University university = universityRepository.findByName("푸단대학교").get();
////        Board board = boardFactory.createEmploymentBoard(university);
////
////        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
////                .boardId(board.getId())
////                .title("제목입니다")
////                .content("내용입니다")
////                .postType(RECRUITMENT)
////                .build());
////
////        //when
////        ResultActions resultActions = mvc.perform(post("/api/posts")
////                .contentType(APPLICATION_JSON)
////                .content(request)
////                .with(csrf()));
////
////        //then
////        resultActions.andExpect(status().isBadRequest())
////                .andExpect(jsonPath("$['validation']['recruitment.startDate']").value("모집 시작일을 입력해주세요"))
////                .andDo(print());
////    }
////
////    @DisplayName("모집글 글 작성시 모집 종료일은 필수다")
////    @WithMockMember
////    @Test
////    void createRecruitmentPostWithoutEndDate() throws Exception {
////
////        //given
////        University university = universityRepository.findByName("푸단대학교").get();
////        Board board = boardFactory.createEmploymentBoard(university);
////
////        String request = om.writeValueAsString(CreateNormalPostRequest.builder()
////                .boardId(board.getId())
////                .title("제목입니다")
////                .content("내용입니다")
////                .postType(RECRUITMENT)
////                .build());
////
////        //when
////        ResultActions resultActions = mvc.perform(post("/api/posts")
////                .contentType(APPLICATION_JSON)
////                .content(request)
////                .with(csrf()));
////
////        //then
////        resultActions.andExpect(status().isBadRequest())
////                .andExpect(jsonPath("$['validation']['recruitment.endDate']").value("모집 종료일을 입력해주세요"))
////                .andDo(print());
////    }
//
//    @DisplayName("특정 게시판의 글 목록 조회")
//    @WithMockMember
//    @Test
//    void loadPostsByBoard() throws Exception {
//
//        //given
//        University university = universityRepository.findByName("푸단대학교").get();
//        Board board = boardFactory.createActivityBoard(university);
//        Member member = memberFactory.createStudentMember("a", university);
//        Post post = postFactory.createPost(member, board, NORMAL);
//
//        //when
//        ResultActions resultActions = mvc.perform(
//                get("/api/posts/{boardId}?type=NORMAL&page=1&order=recent", board.getId())
//                        .with(csrf()));
//
//        //then
//        resultActions.andExpect(status().isOk())
//                .andDo(print());
//    }
//
//
//
//
//
////    @DisplayName("채용게시판의 모집글 생성")
////    @WithMockMember(role = ADMIN)
////    @Test
////    void createEmploymentRecruitmentPost() throws Exception {
////        //given
////        University university = universityRepository.findByName("푸단대학교").get();
////        Board board = boardFactory.createAnonymousBoard(university);
////
////        String request = om.writeValueAsString(CreatePostRequest.builder()
////                .boardId(board.getId())
////                .title("제목입니다")
////                .content("내용입니다")
////                .postType(RECRUITMENT)
////                .recruitment(null)
////                .build());
////
////        //when
////        ResultActions resultActions = mvc.perform(post("/api/posts")
////                .contentType(APPLICATION_JSON)
////                .content(request)
////                .with(csrf()));
////
////        //then
////        Post findPost = postRepository.findAll().get(0);
////        resultActions.andExpect(status().isCreated())
////                .andExpect(content().string(valueOf(findPost.getId())))
////                .andDo(print());
////
////        assertThat(findPost.getTitle()).isEqualTo("제목입니다");
////        assertThat(findPost.getBoard().getId()).isEqualTo(board.getId());
////        assertThat(findPost.getContent()).isEqualTo("내용입니다");
////        assertThat(findPost.getPostType()).isEqualTo(RECRUITMENT);
////    }
//
//
//
//
//}
