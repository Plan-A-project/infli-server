package com.plana.infli.controller;

import static java.util.List.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.annotation.MockMvcTest;
import com.plana.infli.annotation.WithMockMember;
import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.PopularBoard;
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
import com.plana.infli.web.dto.request.board.popular.edit.controller.EditPopularBoardSequenceRequest;
import com.plana.infli.web.dto.request.board.popular.enable.controller.ChangePopularBoardVisibilityRequest;
import com.plana.infli.web.dto.response.board.view.SinglePopularBoard;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@MockMvcTest
public class BoardControllerTest {

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


    @DisplayName("인기 게시판 기본 설정값 존재여부 확인 - 기본값 없는 경우")
    @WithMockMember
    @Test
    void checkDefaultPopularBoardExists() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(get("/api/boards/popular/exists")
                .with(csrf()));

        //then
        resultActions.andExpect(content().string("false"))
                .andDo(print());
    }

    @DisplayName("인기 게시판 기본 설정값 존재여부 확인 - 기본값 있는 경우")
    @WithMockMember
    @Test
    void checkDefaultPopularBoardExists2() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Board board = boardFactory.createClubBoard(university);
        Member member = findContextMember();
        popularBoardFactory.create(member, board, 1);

        //when
        ResultActions resultActions = mvc.perform(get("/api/boards/popular/exists")
                .with(csrf()));

        //then
        resultActions.andExpect(content().string("true"))
                .andDo(print());
    }

    @DisplayName("인기 게시판 기본 설정값 존재여부 확인 - 로그인 하지 않은 경우 예외 발생")
    @Test
    void checkDefaultPopularBoardExists3() throws Exception {
        //when
        ResultActions resultActions = mvc.perform(
                get("/api/boards/popular/exists"));

        //then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
                .andDo(print());
    }

    @DisplayName("인기 게시판 기본 설정값 생성")
    @WithMockMember
    @Test
    void createDefaultPopularBoard() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = findContextMember();

        Board clubBoard = boardFactory.createClubBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);
        Board anonymousBoard = boardFactory.createAnonymousBoard(university);

        //when
        ResultActions resultActions = mvc
                .perform(post("/api/boards/popular"));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());

        List<SinglePopularBoard> boards = boardService.loadEnabledPopularBoardsBy(member.getEmail())
                .getBoards();

        assertThat(boards).extracting("boardId").contains(
                clubBoard.getId(),
                campusLifeBoard.getId(),
                employmentBoard.getId(),
                activityBoard.getId(),
                anonymousBoard.getId());
    }

    @DisplayName("회원이 보고싶다고 설정한 인기 게시판 조회")
    @WithMockMember
    @Test
    void loadPopularBoards() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = findContextMember();

        Board clubBoard = boardFactory.createClubBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);
        Board anonymousBoard = boardFactory.createAnonymousBoard(university);

        boardService.createDefaultPopularBoards(member.getEmail());

        //when
        ResultActions resultActions = mvc
                .perform(get("/api/boards/popular"));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.boards[0].boardId").value(employmentBoard.getId()))
                .andExpect(jsonPath("$.boards[1].boardId").value(activityBoard.getId()))
                .andExpect(jsonPath("$.boards[2].boardId").value(clubBoard.getId()))
                .andExpect(jsonPath("$.boards[3].boardId").value(anonymousBoard.getId()))
                .andExpect(jsonPath("$.boards[4].boardId").value(campusLifeBoard.getId()))
                .andDo(print());
    }


    /**
     * 홈설정 기능
     */
    @DisplayName("인기 게시판을 회원이 보고 싶은 순서대로 변경하기 위해, 인기 게시판 목록 조회")
    @WithMockMember
    @Test
    void loadEnabledPopularBoardsForSetting() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = findContextMember();

        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);

        boardService.createDefaultPopularBoards(member.getEmail());

        //when
        ResultActions resultActions = mvc
                .perform(get("/api/settings/boards/popular"));

        //then
        List<PopularBoard> boards = popularBoardRepository.findAllWithBoardOrderBySequenceByMember(
                member);

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.popularBoards[0].popularBoardId").value(boards.get(0).getId()))
                .andExpect(jsonPath("$.popularBoards[0].boardName").value("채용"))

                .andExpect(jsonPath("$.popularBoards[1].popularBoardId").value(boards.get(1).getId()))
                .andExpect(jsonPath("$.popularBoards[1].boardName").value("대외활동"))

                .andExpect(jsonPath("$.popularBoards[2].popularBoardId").value(boards.get(2).getId()))
                .andExpect(jsonPath("$.popularBoards[2].boardName").value("동아리"))

                .andExpect(jsonPath("$.popularBoards[3].popularBoardId").value(boards.get(3).getId()))
                .andExpect(jsonPath("$.popularBoards[3].boardName").value("익명"))

                .andExpect(jsonPath("$.popularBoards[4].popularBoardId").value(boards.get(4).getId()))
                .andExpect(jsonPath("$.popularBoards[4].boardName").value("학교생활"))
                .andDo(print());
    }


    @DisplayName("인기 게시판 보고싶은 순서대로 변경 - 1. 동아리 2. 학교생활, 채용 게시판 순으로 변경")
    @WithMockMember
    @Test
    void changePopularBoardSequence() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = findContextMember();

        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);

        PopularBoard campusLife = popularBoardFactory.create(member, campusLifeBoard, 1);
        PopularBoard employment = popularBoardFactory.create(member, employmentBoard, 2);
        PopularBoard club = popularBoardFactory.create(member, clubBoard, 3);

        String json = om.writeValueAsString(EditPopularBoardSequenceRequest.builder()
                .popularBoardIds(of(
                        club.getId(),
                        campusLife.getId(),
                        employment.getId()))
                .build());

        //when
        ResultActions resultActions = mvc
                .perform(patch("/api/settings/boards/popular")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());

        List<SinglePopularBoard> boards = boardService.loadEnabledPopularBoardsBy(member.getEmail())
                .getBoards();
        assertThat(boards).extracting("boardId", "boardName").containsExactly(
                tuple(clubBoard.getId(), clubBoard.getBoardName()),
                tuple(campusLifeBoard.getId(), campusLifeBoard.getBoardName()),
                tuple(employmentBoard.getId(), employmentBoard.getBoardName())
        );
    }

    @DisplayName("해당 대학에 존재하는 모든 게시판 조회 - 조회시 항상 1.채용 2.대외활동 3.동아리 4.익명 5.학교생활 순으로 조회된다")
    @WithMockMember
    @Test
    void listAllBoardsInUniversity() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = findContextMember();

        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);

        //when
        ResultActions resultActions = mvc
                .perform(get("/api/settings/boards")
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.universityId").value(university.getId()))
                .andExpect(jsonPath("$.boards[0].id").value(employmentBoard.getId()))
                .andExpect(jsonPath("$.boards[0].boardName").value(employmentBoard.getBoardName()))

                .andExpect(jsonPath("$.boards[1].id").value(activityBoard.getId()))
                .andExpect(jsonPath("$.boards[1].boardName").value(activityBoard.getBoardName()))

                .andExpect(jsonPath("$.boards[2].id").value(clubBoard.getId()))
                .andExpect(jsonPath("$.boards[2].boardName").value(clubBoard.getBoardName()))

                .andExpect(jsonPath("$.boards[3].id").value(anonymousBoard.getId()))
                .andExpect(jsonPath("$.boards[3].boardName").value(anonymousBoard.getBoardName()))

                .andExpect(jsonPath("$.boards[4].id").value(campusLifeBoard.getId()))
                .andExpect(jsonPath("$.boards[4].boardName").value(campusLifeBoard.getBoardName()))
                .andDo(print());
    }

    @DisplayName("모든 인기 게시판중 보고싶은 인기 게시판만 조회되도록 선택 - 학교생활, 동아리 인기 게시판만 선택")
    @WithMockMember
    @Test
    void choosePopularBoardToSee() throws Exception {
        //given
        University university = universityRepository.findByName("푸단대학교").get();
        Member member = findContextMember();

        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);
        boardService.createDefaultPopularBoards(member.getEmail());

        String json = om.writeValueAsString(ChangePopularBoardVisibilityRequest.builder()
                .boardIds(
                        of(campusLifeBoard.getId(),
                                clubBoard.getId()))
                .build());

        //when
        ResultActions resultActions = mvc
                .perform(post("/api/settings/boards/popular")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());

        /**
         * 별도로 정렬 순서를 변경하지 않는한 기본 조회 순서는 1.채용 2.대외활동 3.동아리 4.익명 5.학교생활 이다
         */
        List<SinglePopularBoard> enabledPopularBoards = popularBoardRepository.loadEnabledPopularBoardsBy(member);
        assertThat(enabledPopularBoards).hasSize(2)
                .extracting("boardName")
                .containsExactly("동아리", "학교생활");
    }

    private Member findContextMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email).get();
    }
}
