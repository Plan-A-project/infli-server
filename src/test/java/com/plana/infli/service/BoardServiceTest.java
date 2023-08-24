package com.plana.infli.service;

import static java.util.List.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.PopularBoard;
import com.plana.infli.domain.University;
import com.plana.infli.infra.exception.custom.AuthorizationFailedException;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.factory.BoardFactory;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.PopularBoardFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.popularboard.PopularBoardRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.board.popular.edit.EditPopularBoardSequenceServiceRequest;
import com.plana.infli.web.dto.request.board.popular.enable.ChangePopularBoardVisibilityServiceRequest;
import com.plana.infli.web.dto.response.board.settings.board.BoardListResponse;
import com.plana.infli.web.dto.response.board.settings.polularboard.PopularBoardsSettingsResponse;
import com.plana.infli.web.dto.response.board.view.PopularBoardsResponse;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BoardServiceTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private PopularBoardRepository popularBoardRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private BoardFactory boardFactory;

    @Autowired
    private PopularBoardFactory popularBoardFactory;

    @AfterEach
    void tearDown() {
        commentRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        popularBoardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }

    @DisplayName("특정 회원의 인기 게시판 기본 설정값이 존재하는지 조회- 기본값 없는 경우 False 를 응답한다")
    @Test
    void defaultPopularBoardNotExists() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        boolean exists = boardService.popularBoardExistsBy(
                member.getLoginCredentials().getUsername());

        //then
        assertThat(exists).isFalse();
    }

    @DisplayName("특정 회원의 인기 게시판 기본 설정값이 존재하는지 조회- 기본값 있는 경우 True 를 응답한다")
    @Test
    void defaultPopularBoardExists() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        boardService.createDefaultPopularBoards(member.getLoginCredentials().getUsername());

        //when
        boolean exists = boardService.popularBoardExistsBy(
                member.getLoginCredentials().getUsername());

        //then
        assertThat(exists).isTrue();
    }


    @DisplayName("존재하지 않는 회원의 인기 게시판 기본 설정값 존재여부를 조회할 수 없다")
    @Test
    void defaultPopularBoardExistsByNotExistingMember() {
        //when //then
        assertThatThrownBy(() -> boardService.popularBoardExistsBy("aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("특정 회원의 인기 게시판 기본 설정값이 없는 경우 기본값을 생성한다")
    @Test
    void createDefaultPopularBoard() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        boardService.createDefaultPopularBoards(member.getLoginCredentials().getUsername());

        //then
        List<PopularBoard> popularBoards = popularBoardRepository.findAllWithBoardOrderBySequenceByMember(
                member);
        assertThat(popularBoards.size()).isEqualTo(5);
    }

    @DisplayName("특정 회원이 이미 보고싶은 인기 게시판 설정을 한경우, 기본값 생성  요청을 할 수 없다")
    @Test
    void createDefaultPopularBoardWhilePopularBoardAlreadyExists() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        boardService.createDefaultPopularBoards(member.getLoginCredentials().getUsername());

        //when //then
        assertThatThrownBy(() -> boardService.createDefaultPopularBoards(
                member.getLoginCredentials().getUsername()))
                .isInstanceOf(ConflictException.class)
                .message().isEqualTo("인기 게시판 기본값이 이미 생성되었습니다");
    }

    @DisplayName("존재하지 않는 회원의 인기 게시판 기본 설정값을 생성할수 없다")
    @Test
    void createDefaultPopularBoardForNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);

        //when //then
        assertThatThrownBy(() -> boardService.createDefaultPopularBoards("aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }


    @DisplayName("특정 회원이 보고싶은 인기 게시판 기본 설정을 하지 않았을 경우 조회 시나리오")
    @TestFactory
    Collection<DynamicTest> listPopularBoardScenario() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        return of(
                dynamicTest("특정 회원이 보고싶은 인기 게시판 기본 설정값이 없는 것을 확인한다",
                        () -> {
                            //when
                            boolean exists = boardService.popularBoardExistsBy(
                                    member.getLoginCredentials().getUsername());

                            //then
                            assertThat(exists).isFalse();
                        }),

                dynamicTest("기본 설정값이 없는 경우, 해당 대학에 존재하는 모든 게시판에 각각 대응하는 인기 게시판을 생성한다",
                        () -> {
                            //when
                            boardService.createDefaultPopularBoards(
                                    member.getLoginCredentials().getUsername());

                            //then
                            assertThat(popularBoardRepository.findEnabledPopularBoardCountBy(
                                    member)).isEqualTo(5);
                        }),

                dynamicTest("기본값 설정 후 조회시 기본값은 항상 채용, 대외활동, 동아리, 익명, 학교생활 인기게시판 순으로 조회된다",
                        () -> {
                            //when
                            PopularBoardsResponse response = boardService.loadEnabledPopularBoardsBy(
                                    member.getLoginCredentials().getUsername());

                            //then
                            assertThat(response.getBoards().size()).isEqualTo(5);
                            assertThat(response.getBoards()).extracting("boardId", "boardName",
                                            "boardType")
                                    .containsExactly(
                                            tuple(employmentBoard.getId(),
                                                    employmentBoard.getBoardName(),
                                                    employmentBoard.getBoardType().toString()),

                                            tuple(activityBoard.getId(),
                                                    activityBoard.getBoardName(),
                                                    activityBoard.getBoardType().toString()),

                                            tuple(clubBoard.getId(),
                                                    clubBoard.getBoardName(),
                                                    clubBoard.getBoardType().toString()),

                                            tuple(anonymousBoard.getId(),
                                                    anonymousBoard.getBoardName(),
                                                    anonymousBoard.getBoardType().toString()),

                                            tuple(campusLifeBoard.getId(),
                                                    campusLifeBoard.getBoardName(),
                                                    campusLifeBoard.getBoardType().toString()));
                        })
        );
    }

    /**
     * 홈설정 기능
     */

    @DisplayName("인기 게시판 기본값 설정후, 조회순서 변경을 위해 인기 게시판 목록 조회")
    @TestFactory
    Collection<DynamicTest> listPopularBoardForSettingScenario() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        return of(
                dynamicTest("해당 대학에 존재하는 모든 게시판에 각각 대응하는 인기 게시판을 생성한다 (기본값 생성)",
                        () -> {
                            //when
                            boardService.createDefaultPopularBoards(
                                    member.getLoginCredentials().getUsername());

                            //then
                            assertThat(popularBoardRepository.findEnabledPopularBoardCountBy(
                                    member)).isEqualTo(5);
                        }),

                dynamicTest(
                        "기본값 설정 후 인기 게시판 목록 조회시 기본값은 1.채용 2.대외활동 3.동아리 4.익명 5.학교생활 인기게시판 순으로 조회된다",
                        () -> {
                            //when
                            PopularBoardsSettingsResponse response = boardService.loadEnabledPopularBoardsForSettingBy(
                                    member.getLoginCredentials().getUsername());

                            //then
                            assertThat(response.getPopularBoards().size()).isEqualTo(5);

                            List<PopularBoard> boards = popularBoardRepository.findAllWithBoardOrderBySequenceByMember(
                                    member);
                            assertThat(response.getPopularBoards()).extracting("popularBoardId",
                                            "boardName",
                                            "boardType")
                                    .containsExactly(
                                            tuple(boards.get(0).getId(),
                                                    employmentBoard.getBoardName(),
                                                    employmentBoard.getBoardType().toString()),

                                            tuple(boards.get(1).getId(),
                                                    activityBoard.getBoardName(),
                                                    activityBoard.getBoardType().toString()),

                                            tuple(boards.get(2).getId(),
                                                    clubBoard.getBoardName(),
                                                    clubBoard.getBoardType().toString()),

                                            tuple(boards.get(3).getId(),
                                                    anonymousBoard.getBoardName(),
                                                    anonymousBoard.getBoardType().toString()),

                                            tuple(boards.get(4).getId(),
                                                    campusLifeBoard.getBoardName(),
                                                    campusLifeBoard.getBoardType().toString()));
                        })
        );
    }


    @DisplayName("존재하지 않는 회원의 인기 게시판 목록 조회를 할수 없다")
    @Test
    void listPopularBoardByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);

        //when //then
        assertThatThrownBy(() -> boardService.loadEnabledPopularBoardsForSettingBy("aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("인기 게시판 조회 순서 변경 - 1.익명, 2.채용, 3.대외활동, 4.동아리, 5.학교생활 순으로 변경되길 희망함")
    @Test
    void changePopularBoardSequence() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        boardService.createDefaultPopularBoards(member.getLoginCredentials().getUsername());

        List<PopularBoard> boards = popularBoardRepository.findAllWithBoardOrderBySequenceByMember(
                member);
        PopularBoard employMemberPopularBoard = boards.get(0);
        PopularBoard activityPopularBoard = boards.get(1);
        PopularBoard clubPopularBoard = boards.get(2);
        PopularBoard anonymousPopularBoard = boards.get(3);
        PopularBoard campusLifePopularBoard = boards.get(4);

        EditPopularBoardSequenceServiceRequest request = EditPopularBoardSequenceServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .popularBoardIds(of(
                        anonymousPopularBoard.getId(),
                        employMemberPopularBoard.getId(),
                        activityPopularBoard.getId(),
                        clubPopularBoard.getId(),
                        campusLifePopularBoard.getId()))
                .build();

        //when
        boardService.changePopularBoardSequence(request);

        //then
        List<PopularBoard> popularBoards = popularBoardRepository.findAllWithBoardOrderBySequenceByMember(
                member);

        assertThat(popularBoards).extracting("board").extracting("boardName")
                .containsExactly("익명", "채용", "대외활동", "동아리", "학교생활");
    }

    @DisplayName("인기 게시판중 보고싶은 인기 게시판만 선택하기 위해 해당 대학에 존재하는 모든 게시판 조회. "
            + "모든 게시판 조회시 항상 1.채용 2.대외활동 3.동아리 4.익명 5.학교생활 순으로 조회된다")
    @Test
    void listAllExistingBoardsForSetting() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");

        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        boardService.createDefaultPopularBoards(member.getLoginCredentials().getUsername());

        //when
        BoardListResponse response = boardService.loadAllBoard(
                member.getLoginCredentials().getUsername());

        //then
        assertThat(response.getUniversityId()).isEqualTo(university.getId());
        assertThat(response.getBoards()).hasSize(5)
                .extracting("id", "boardName")
                .containsExactly(
                        tuple(employmentBoard.getId(), employmentBoard.getBoardName()),
                        tuple(activityBoard.getId(), activityBoard.getBoardName()),
                        tuple(clubBoard.getId(), clubBoard.getBoardName()),
                        tuple(anonymousBoard.getId(), anonymousBoard.getBoardName()),
                        tuple(campusLifeBoard.getId(), campusLifeBoard.getBoardName())
                );
    }

    @DisplayName("해당 대학에 존재하는 모든 게시판중, 보고싶은 인기 게시판만 선택하는 시나리오 테스트")
    @TestFactory
    Collection<DynamicTest> chooseWantToSeePopularBoardScenario() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        return of(
                dynamicTest("인기 게시판 기본값 설정 후, 5개의 인기 게시판 중에서 학교생활과 익명 인기게시판만 보고싶다고 선택한다",
                        () -> {
                            //given
                            boardService.createDefaultPopularBoards(
                                    member.getLoginCredentials().getUsername());

                            ChangePopularBoardVisibilityServiceRequest request = ChangePopularBoardVisibilityServiceRequest.builder()
                                    .username(member.getLoginCredentials().getUsername())
                                    .boardIds(of(
                                            campusLifeBoard.getId(),
                                            anonymousBoard.getId()))
                                    .build();

                            //when
                            boardService.changeBoardVisibility(request);


                        }),

                dynamicTest("학교생활과, 익명 인기 게시판만 보고싶다고 설정 완료후, 보고싶다고 설정한 인기 게시판 목록을 조회하면"
                                + "학교생활과 익명 인기 게시판만 조회되며, 1.채용 2.대외활동 3.동아리 4.익명 5.학교생활 순으로 조회된다",
                        () -> {
                            //then
                            PopularBoardsResponse response = boardService.loadEnabledPopularBoardsBy(
                                    member.getLoginCredentials().getUsername());
                            assertThat(response.getBoards()).hasSize(2)
                                    .extracting("boardId", "boardName", "boardType")
                                    .containsExactly(
                                            tuple(anonymousBoard.getId(),
                                                    anonymousBoard.getBoardName(),
                                                    anonymousBoard.getBoardType().toString()),

                                            tuple(campusLifeBoard.getId(),
                                                    campusLifeBoard.getBoardName(),
                                                    campusLifeBoard.getBoardType().toString())
                                    );
                        })
        );
    }

    @DisplayName("보고싶은 인기 게시판 선택, 보고싶은 인기 게시판 조회 순서 변경 시나리오")
    @TestFactory
    Collection<DynamicTest> homeSettingScenario() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        //when
        return of(
                dynamicTest("인기 게시판 기본값 설정 후, 5개의 인기 게시판 중에서 동아리, 익명, 채용 인기게시판만 보고싶다고 선택한다",
                        () -> {
                            //given
                            boardService.createDefaultPopularBoards(
                                    member.getLoginCredentials().getUsername());

                            ChangePopularBoardVisibilityServiceRequest request = ChangePopularBoardVisibilityServiceRequest.builder()
                                    .username(member.getLoginCredentials().getUsername())
                                    .boardIds(
                                            of(clubBoard.getId(),
                                                    anonymousBoard.getId(),
                                                    employmentBoard.getId()))
                                    .build();

                            //when
                            boardService.changeBoardVisibility(request);
                        }),

                dynamicTest("동아리, 익명, 채용 인기게시판만 보고싶다고 설정 완료후, 보고싶다고 설정한 인기 게시판 목록을 조회하면"
                                + "동아리, 익명, 채용 인기 게시판만 조회되며, 기본값인 1.채용 2.대외활동 3.동아리 4.익명 5.학교생활 순으로 조회된다",
                        () -> {
                            //then
                            PopularBoardsResponse response = boardService.loadEnabledPopularBoardsBy(
                                    member.getLoginCredentials().getUsername());
                            assertThat(response.getBoards()).hasSize(3)
                                    .extracting("boardId", "boardName", "boardType")
                                    .containsExactly(
                                            tuple(employmentBoard.getId(),
                                                    employmentBoard.getBoardName(),
                                                    employmentBoard.getBoardType().toString()),

                                            tuple(clubBoard.getId(),
                                                    clubBoard.getBoardName(),
                                                    clubBoard.getBoardType().toString()),

                                            tuple(anonymousBoard.getId(),
                                                    anonymousBoard.getBoardName(),
                                                    anonymousBoard.getBoardType().toString())
                                    );
                        }),

                dynamicTest("보고싶다고 설정한 동아리, 익명, 채용 인기 게시판을 1.익명 2.동아리 3.채용 순서로 조회되길 선택한후 변경한다",
                        () -> {

                            PopularBoard clubPopularBoard = popularBoardRepository.findByBoardAndMember(
                                    clubBoard, member);

                            PopularBoard anonymousPopularBoard = popularBoardRepository.findByBoardAndMember(
                                    anonymousBoard, member);

                            PopularBoard employmentPopularBoard = popularBoardRepository.findByBoardAndMember(
                                    employmentBoard, member);

                            EditPopularBoardSequenceServiceRequest request = EditPopularBoardSequenceServiceRequest.builder()
                                    .username(member.getLoginCredentials().getUsername())
                                    .popularBoardIds(
                                            of(anonymousPopularBoard.getId(),
                                                    clubPopularBoard.getId(),
                                                    employmentPopularBoard.getId()))
                                    .build();

                            boardService.changePopularBoardSequence(request);
                        }),

                dynamicTest("다시한번 보고싶다고 설정한 인기 게시판 목록을 조회하면 1.익명 2.동아리 3.채용 순서대로 총 3개만 조회된다",
                        () -> {
                            //then
                            PopularBoardsResponse response = boardService.loadEnabledPopularBoardsBy(
                                    member.getLoginCredentials().getUsername());
                            assertThat(response.getBoards()).hasSize(3)
                                    .extracting("boardId", "boardName", "boardType")
                                    .containsExactly(
                                            tuple(anonymousBoard.getId(),
                                                    anonymousBoard.getBoardName(),
                                                    anonymousBoard.getBoardType().toString()),

                                            tuple(clubBoard.getId(),
                                                    clubBoard.getBoardName(),
                                                    clubBoard.getBoardType().toString()),

                                            tuple(employmentBoard.getId(),
                                                    employmentBoard.getBoardName(),
                                                    employmentBoard.getBoardType().toString())
                                    );
                        })
        );
    }

    @DisplayName("존재하지 않는 게시판의 인기 게시물을 보고싶다고 선택할수 없다")
    @Test
    void choosingNotExistingBoardToSeePopularPosts() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("member", university);

        ChangePopularBoardVisibilityServiceRequest request = ChangePopularBoardVisibilityServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardIds(of(999L))
                .build();

        //when //then
        assertThatThrownBy(() -> boardService.changeBoardVisibility(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("게시판을 찾을수 없습니다");
    }

    @DisplayName("다른 대학에 있는 게시판의 인기 게시물을 보고싶다고 선택할수 없다")
    @Test
    void choosingBoardThatIsNotInMyUniversityToSeePopularPosts() {

        //given
        University myUniversity = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("member", myUniversity);

        University otherUniversity = universityFactory.createUniversity("서울대학교");
        Board otherBoard = boardFactory.createClubBoard(otherUniversity);

        ChangePopularBoardVisibilityServiceRequest request = ChangePopularBoardVisibilityServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .boardIds(of(otherBoard.getId()))
                .build();

        //when //then
        assertThatThrownBy(() -> boardService.changeBoardVisibility(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");

    }

    @DisplayName("존재하지 않는 회원은 보고싶은 인기 게시판을 선택할수 없다")
    @Test
    void choosingToSeePopularPostsByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board anonymousBoard = boardFactory.createAnonymousBoard(university);

        ChangePopularBoardVisibilityServiceRequest request = ChangePopularBoardVisibilityServiceRequest.builder()
                .username("aaaaaaa")
                .boardIds(of(anonymousBoard.getId()))
                .build();

        //when //then
        assertThatThrownBy(() -> boardService.changeBoardVisibility(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("홈설정 변경을 위해서, 존재하지 않는 회원의 대학에 존재하는 모든 게시판을 조회할수 없다")
    @Test
    void listAllExistingBoardsForSettingByNotExistingMember() {
        //when //then
        assertThatThrownBy(() -> boardService.createDefaultPopularBoards("aaaaaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("존재하지 않는 인기 게시판의 정렬 순서를 변경할 수 없다")
    @Test
    void changeNotExistingPopularBoardSequence() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("member", university);

        EditPopularBoardSequenceServiceRequest request = EditPopularBoardSequenceServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .popularBoardIds(of(999L, 111L, 2L))
                .build();

        //when //then
        assertThatThrownBy(
                () -> boardService.changePopularBoardSequence(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("인기 게시판을 찾을수 없습니다");

    }


    @DisplayName("존재하지 않는 회원은 보고싶은 인기 게시판의 정렬 순서를 변경할 수 없다")
    @Test
    void changePopularBoardSequenceByNotExistingMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createCampusLifeBoard(university);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        PopularBoard popularBoard = popularBoardFactory.create(member, board, 0);

        EditPopularBoardSequenceServiceRequest request = EditPopularBoardSequenceServiceRequest.builder()
                .username("aaaaaaa")
                .popularBoardIds(of(popularBoard.getId()))
                .build();

        //when //then
        assertThatThrownBy(
                () -> boardService.changePopularBoardSequence(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");

    }

    @DisplayName("다른 회원이 보고싶어하는 인기 게시판의 정렬 순서를 내가 변경할 수 없다")
    @Test
    void changingOtherMemberPopularBoardSequence() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);

        Member otherMember = memberFactory.createVerifiedStudentMember("otherMember", university);
        boardService.createDefaultPopularBoards(otherMember.getLoginCredentials().getUsername());

        List<PopularBoard> popularBoards = popularBoardRepository.findAllWithBoardOrderBySequenceByMember(
                otherMember);

        Member member = memberFactory.createVerifiedStudentMember("member", university);

        EditPopularBoardSequenceServiceRequest request = EditPopularBoardSequenceServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .popularBoardIds(
                        of(popularBoards.get(2).getId(),
                                popularBoards.get(1).getId(),
                                popularBoards.get(4).getId(),
                                popularBoards.get(0).getId(),
                                popularBoards.get(3).getId()))
                .build();

        //when //then
        assertThatThrownBy(
                () -> boardService.changePopularBoardSequence(request))
                .isInstanceOf(AuthorizationFailedException.class)
                .message().isEqualTo("해당 권한이 없습니다");

    }

    @DisplayName("보고싶어하는 인기 게시판의 정렬 순서 변경 요청시, 존재하는 모든 보고싶은 인기 게시판의 ID 번호를 순서대로 담아서 요청해야 된다"
            + "Ex) 보고싶다고 설정한 인기게시판이 5개이지만, 2개의 게시판만 정렬 순서 변경 요청을 할수 없다")
    @Test
    void changingOnlySomeOfPopularBoardSequence() {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board anonymousBoard = boardFactory.createAnonymousBoard(university);
        Board activityBoard = boardFactory.createActivityBoard(university);
        Board employmentBoard = boardFactory.createEmploymentBoard(university);
        Board clubBoard = boardFactory.createClubBoard(university);
        Board campusLifeBoard = boardFactory.createCampusLifeBoard(university);

        Member member = memberFactory.createVerifiedStudentMember("member", university);
        boardService.createDefaultPopularBoards(member.getLoginCredentials().getUsername());

        List<PopularBoard> popularBoards = popularBoardRepository.findAllWithBoardOrderBySequenceByMember(
                member);

        EditPopularBoardSequenceServiceRequest request = EditPopularBoardSequenceServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .popularBoardIds(
                        of(popularBoards.get(2).getId(),
                                popularBoards.get(3).getId()))
                .build();

        //when //then
        assertThatThrownBy(
                () -> boardService.changePopularBoardSequence(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("모든 보고싶은 게시판이 선택되지 않았습니다");

    }
}
