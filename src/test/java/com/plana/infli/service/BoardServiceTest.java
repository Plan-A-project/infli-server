//package com.plana.infli.service;
//
//import static com.plana.infli.web.dto.request.board.popular.enable.controller.ChangePopularBoardVisibilityRequest.*;
//import static java.util.List.*;
//import static org.assertj.core.api.Assertions.*;
//
//import com.plana.infli.domain.Board;
//import com.plana.infli.domain.Member;
//import com.plana.infli.domain.University;
//import com.plana.infli.exception.custom.NotFoundException;
//import com.plana.infli.repository.board.BoardRepository;
//import com.plana.infli.repository.member.MemberRepository;
//import com.plana.infli.repository.popularboard.PopularBoardRepository;
//import com.plana.infli.repository.university.UniversityRepository;
//import com.plana.infli.web.dto.request.board.popular.enable.controller.ChangePopularBoardVisibilityRequest;
//import com.plana.infli.web.dto.response.board.settings.board.BoardListResponse;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.jdbc.Sql;
//
//@Sql("classpath:db/teardown.sql")
//@SpringBootTest
//@ActiveProfiles("test")
//public class BoardServiceTest {
//
//
//    @Autowired
//    private BoardService boardService;
//
//    @Autowired
//    private MemberRepository memberRepository;
//
//    @Autowired
//    private BoardRepository boardRepository;
//
//    @Autowired
//    private PopularBoardRepository popularBoardRepository;
//
//    @Autowired
//    private UniversityRepository universityRepository;
//
//    @DisplayName("특정 대학에 존재하는 모든 게시판을 조회한다")
//    @Test
//    void listAllBoardInUniversity() {
//        //given
//        University university = universityRepository.save(newUniversity());
//
//        Board board1 = newActivityBoard(university);
//        Board board2 = newAnonymousBoard(university);
//        Board board3 = newClubBoard(university);
//        boardRepository.saveAll(of(board1, board2, board3));
//
//        Member member = memberRepository.save(newStudentMember(university));
//
//        //when
//        BoardListResponse response = boardService.loadAllBoard(member.getEmail());
//
//        //then
//        assertThat(response.getUniversityId()).isEqualTo(university.getId());
////        assertThat(response.getBoardCount()).isEqualTo(3);
//        assertThat(response.getBoards()).hasSize(3)
//                .extracting("id", "boardName")
//                .containsExactlyInAnyOrder(
//                        tuple(board1.getId(), board1.getBoardName()),
//                        tuple(board2.getId(), board2.getBoardName()),
//                        tuple(board3.getId(), board3.getBoardName())
//                );
//    }
//
//    @DisplayName("특정 대학의 모든 게시판을 조회시 게시판이 존재하지 않는 경우 빈 ArrayList 가 반환된다")
//    @Test
//    void listAllBoardInUniversityWithEmptyBoard() {
//        //given
//        University university = universityRepository.save(newUniversity());
//
//        Member member = memberRepository.save(newStudentMember(university));
//
//        //when
//        BoardListResponse response = boardService.loadAllBoard(member.getEmail());
//
//        //then
//        assertThat(response.getUniversityId()).isEqualTo(university.getId());
////        assertThat(response.getBoardCount()).isEqualTo(0);
//        assertThat(response.getBoards()).hasSize(0);
//    }
//
////    @DisplayName("존재하지 않는 회원의 게시판 목록을 조회할수 없다")
////    @Test
////    void listAllBoardInUniversityWithEmptyBoard2() {
////
////        //when //then
////        assertThatThrownBy(() -> boardService.loadAllBoard(null))
////                .isInstanceOf();
////
////        //then
////        assertThat(response.getUniversityId()).isEqualTo(university.getId());
////        assertThat(response.getBoardCount()).isEqualTo(0);
////        assertThat(response.getBoards()).hasSize(0);
////    }
//
//    @DisplayName("기본값으로 한개의 대학에는 총 5개의 게시판이 존재해야 된다")
//    @Test
//    void list5DefaultBoard() {
//        //given
//        University university = universityRepository.save(newUniversity());
//
//        Board board1 = newActivityBoard(university);
//        Board board2 = newAnonymousBoard(university);
//        Board board3 = newClubBoard(university);
//        Board board4 = newCampusLifeBoard(university);
//        Board board5 = newEmploymentBoard(university);
//        boardRepository.saveAll(of(board1, board2, board3, board4, board5));
//
//        Member member = memberRepository.save(newStudentMember(university));
//
//        //when
//        BoardListResponse response = boardService.loadAllBoard(member.getEmail());
//
//        //then
//        assertThat(response.getUniversityId()).isEqualTo(university.getId());
////        assertThat(response.getBoardCount()).isEqualTo(5);
//        assertThat(response.getBoards()).hasSize(5)
//                .extracting("id", "boardName")
//                .containsExactlyInAnyOrder(
//                        tuple(board1.getId(), board1.getBoardName()),
//                        tuple(board2.getId(), board2.getBoardName()),
//                        tuple(board3.getId(), board3.getBoardName()),
//                        tuple(board4.getId(), board4.getBoardName()),
//                        tuple(board5.getId(), board5.getBoardName())
//                );
//    }
//
//    @DisplayName("특정 대학에 존재하는 모든 게시판을 조회할떄 기본 정렬 순서는 1.채용 2.대외활동 3.동아리 4.익명 5.학교생활 이다. ")
//    @Test
//    void listBoardWithDefaultOrder() {
//        //given
//        University university = universityRepository.save(newUniversity());
//
//        Board board1 = newActivityBoard(university);
//        Board board2 = newAnonymousBoard(university);
//        Board board3 = newClubBoard(university);
//        Board board4 = newCampusLifeBoard(university);
//        Board board5 = newEmploymentBoard(university);
//        boardRepository.saveAll(of(board1, board2, board3, board4, board5));
//
//        Member member = memberRepository.save(newStudentMember(university));
//
//        //when
//        BoardListResponse response = boardService.loadAllBoard(member.getEmail());
//
//        //then
//        assertThat(response.getUniversityId()).isEqualTo(university.getId());
////        assertThat(response.getBoardCount()).isEqualTo(5);
//        assertThat(response.getBoards()).hasSize(5)
//                .extracting("id", "boardName")
//                .containsExactly(
//                        tuple(board5.getId(), "채용"),
//                        tuple(board1.getId(), "대외활동"),
//                        tuple(board3.getId(), "동아리"),
//                        tuple(board2.getId(), "익명"),
//                        tuple(board4.getId(), "학교생활")
//                );
//    }
//
//    @DisplayName("존재하지 않는 회원은 게시판 목록을 조회할 수 없다")
//    @Test
//    void listBoardByNotExistingMember() {
//        //given
//        University university = universityRepository.save(newUniversity());
//
//        Board board1 = newActivityBoard(university);
//        Board board2 = newAnonymousBoard(university);
//        boardRepository.saveAll(of(board1, board2));
//
//        //when //then
//        assertThatThrownBy(() -> boardService.loadAllBoard("123@naver.com"))
//                .isInstanceOf(NotFoundException.class);
//    }
//
//    @DisplayName("로그인 하지 않은 상태로 게시판 목록을 조회할 수 없다")
//    @Test
//    void listBoardWithoutLogin() {
//        //given
//        University university = universityRepository.save(newUniversity());
//
//        Board board1 = newActivityBoard(university);
//        Board board2 = newAnonymousBoard(university);
//        boardRepository.saveAll(of(board1, board2));
//
//        //when //then
//        assertThatThrownBy(() -> boardService.loadAllBoard(null))
//                .isInstanceOf(NotFoundException.class)
//                .message().isEqualTo("사용자를 찾을수 없습니다");
//    }
//
//    @DisplayName("회원은 여러 게시판중 보고싶은 게시판만 조회되도록 선택 할 수 있다")
//    @Test
//    void createMemberBoard() {
//        //given
//        University university = universityRepository.save(newUniversity());
//
//        Board board1 = newActivityBoard(university);
//        Board board2 = newAnonymousBoard(university);
//        Board board3 = newClubBoard(university);
//        Board board4 = newCampusLifeBoard(university);
//        Board board5 = newEmploymentBoard(university);
//        boardRepository.saveAll(of(board1, board2, board3, board4, board5));
//
//        Member member = memberRepository.save(newStudentMember(university));
//
////        ChangePopularBoardVisibilityRequest request = builder()
////                .ids(of(board1.getId(), board2.getId(), board5.getId())).build();
//
//        //when
////        boardService.changeBoardVisibility(request, member.getEmail());
//
//        //then
//        assertThat(popularBoardRepository.count()).isEqualTo(3);
//        assertThat(popularBoardRepository.findAllMemberBoardWithMemberAndBoard())
//                .extracting("board")
//                .extracting("id")
//                .containsExactlyInAnyOrder(
//                        board1.getId(), board2.getId(), board5.getId()
//                );
//    }
//
//    @DisplayName("존재하지 않는 회원은 여러 게시판중 보고싶은 게시판만 조회되도록 선택 할 수 없다")
//    @Test
//    void createMemberBoardByNotExistingMember() {
//        //given
//        University university = universityRepository.save(newUniversity());
//
//        Board board1 = newActivityBoard(university);
//        Board board2 = newAnonymousBoard(university);
//        Board board3 = newClubBoard(university);
//        Board board4 = newCampusLifeBoard(university);
//        Board board5 = newEmploymentBoard(university);
//        boardRepository.saveAll(of(board1, board2, board3, board4, board5));
//
////        ChangePopularBoardVisibilityRequest request = builder()
////                .ids(of(board1.getId(), board2.getId(), board5.getId())).build();
//
//        //when //then
////        assertThatThrownBy(() -> boardService.changeBoardVisibility(request, "notExisting@naver.com"))
////                .isInstanceOf(NotFoundException.class)
////                .message().isEqualTo("사용자를 찾을수 없습니다");
//    }
//}
