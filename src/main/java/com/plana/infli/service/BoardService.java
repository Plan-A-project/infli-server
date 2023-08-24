package com.plana.infli.service;

import static com.plana.infli.domain.PopularBoard.newPopularBoard;
import static com.plana.infli.domain.editor.PopularBoardEditor.*;
import static com.plana.infli.infra.exception.custom.BadRequestException.NOT_ALL_POPULARBOARD_WAS_CHOSEN;
import static com.plana.infli.infra.exception.custom.ConflictException.DEFAULT_POPULAR_BOARD_EXISTS;
import static com.plana.infli.infra.exception.custom.NotFoundException.BOARD_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.MEMBER_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.POPULAR_BOARD_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.UNIVERSITY_NOT_FOUND;
import static com.plana.infli.web.dto.response.board.settings.board.BoardListResponse.createBoardListResponse;

import com.plana.infli.domain.PopularBoard;
import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.infra.exception.custom.AuthorizationFailedException;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.popularboard.PopularBoardRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.board.popular.edit.EditPopularBoardSequenceServiceRequest;
import com.plana.infli.web.dto.request.board.popular.enable.ChangePopularBoardVisibilityServiceRequest;
import com.plana.infli.web.dto.response.board.settings.board.BoardListResponse;
import com.plana.infli.web.dto.response.board.settings.board.SingleBoard;
import com.plana.infli.web.dto.response.board.settings.polularboard.PopularBoardsSettingsResponse;
import com.plana.infli.web.dto.response.board.settings.polularboard.SinglePopularBoardForSetting;
import com.plana.infli.web.dto.response.board.view.PopularBoardsResponse;
import com.plana.infli.web.dto.response.board.view.SinglePopularBoard;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    private final PopularBoardRepository popularBoardRepository;

    private final MemberRepository memberRepository;

    private final UniversityRepository universityRepository;

    public boolean popularBoardExistsBy(String username) {

        Member member = findMemberBy(username);

        return popularBoardRepository.existsByMember(member);
    }

    private Member findMemberBy(String email) {
        return memberRepository.findActiveMemberBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    public PopularBoardsResponse loadEnabledPopularBoardsBy(String username) {

        Member member = findMemberBy(username);

        checkPopularBoardExists(member);

        List<SinglePopularBoard> boards = popularBoardRepository.loadEnabledPopularBoardsBy(member);

        return PopularBoardsResponse.of(boards);
    }

    private void checkPopularBoardExists(Member member) {
        if (popularBoardRepository.existsByMember(member) == false) {
            throw new BadRequestException(POPULAR_BOARD_NOT_FOUND);
        }
    }

    @Transactional
    public void createDefaultPopularBoards(String username) {

        Member member = findMemberWithUniversityJoinedBy(username);

        checkPopularBoardsAlreadyExists(member);

        createWithDefaultSequence(member);
    }

    private void checkPopularBoardsAlreadyExists(Member member) {
        if (popularBoardRepository.existsByMember(member)) {
            throw new ConflictException(DEFAULT_POPULAR_BOARD_EXISTS);
        }
    }

    private Member findMemberWithUniversityJoinedBy(String username) {
        return memberRepository.findActiveMemberWithUniversityBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    private void createWithDefaultSequence(Member member) {

        // 해당 대학에 존재하는 모든 게시판 조회
        List<Board> boards = boardRepository.findAllActiveBoardBy(member.getUniversity());

        // 그 모든 게시판들을 전부 보고 싶다고 간주함
        // 각 게시판에 대응 되는 "보고싶은 게시판" 새로 생성
        // EX) 해당 대학에 동아리 게시판이 존재하는 경우, "보고싶은 동아리 게시판" 생성

        // 새로 생성된 "보고싶은 게시판"의 기본 정렬순서는, 그에 대응하는 게시판의 기본 정렬 순서와 동일하다
        // EX) 동아리 게시판의 기본 정렬 순서: 3번
        //     생성된 "보고싶은 동아리 게시판"의 정렬순서 : 3번
        boards.forEach(board -> popularBoardRepository.save(newPopularBoard(member, board)));
    }


    public PopularBoardsSettingsResponse loadEnabledPopularBoardsForSettingBy(String username) {

        Member member = findMemberWithUniversityJoinedBy(username);

        // 회원이 보고싶다고 설정한 "인기 게시판" 모두 조회
        List<SinglePopularBoardForSetting> popularBoards = popularBoardRepository.findAllEnabledPopularBoardsForSettingBy(
                member);

        return PopularBoardsSettingsResponse.of(popularBoards);
    }

    @Transactional
    public void changePopularBoardSequence(EditPopularBoardSequenceServiceRequest request) {

        // "인기 게시판"의 ID 번호들이, 회원이 보고싶어하는 순서대로 담겨져 있다
        List<Long> ids = request.getPopularBoardIds();

        Member member = findMemberBy(request.getUsername());

        List<PopularBoard> popularBoards = findPopularBoardsBy(ids);

        validateChangePopularBoardSequenceRequest(member, popularBoards);

        changeMemberBoardSequences(popularBoards);
    }

    private List<PopularBoard> findPopularBoardsBy(List<Long> popularBoardIds) {

        List<PopularBoard> list = new ArrayList<>();

        for (Long id : popularBoardIds) {
            PopularBoard popularBoard = popularBoardRepository.findWithMemberById(id)
                    .orElseThrow(() -> new NotFoundException(POPULAR_BOARD_NOT_FOUND));

            list.add(popularBoard);
        }

        return list;
    }

    private void validateChangePopularBoardSequenceRequest(Member member,
            List<PopularBoard> popularBoards) {

        for (PopularBoard popularBoard : popularBoards) {
            if (popularBoard.getMember().equals(member) == false) {
                throw new AuthorizationFailedException();
            }
        }

        int count = popularBoardRepository.findEnabledPopularBoardCountBy(member);

        //TODO
        if (popularBoards.size() != count) {
            throw new BadRequestException(NOT_ALL_POPULARBOARD_WAS_CHOSEN);
        }
    }

    private void changeMemberBoardSequences(List<PopularBoard> popularBoards) {

        // 새로운 정렬 번호 생성후
        // 1씩 증가시키면서 각 "보고싶은 게시판"의 새로운 정렬순서 설정
        int newSequence = createNextSequenceNumber();

        for (PopularBoard popularBoard : popularBoards) {
            editSequence(popularBoard, newSequence);
            newSequence++;
        }
    }

    private int createNextSequenceNumber() {
        Integer latestSequenceNumber = popularBoardRepository.findLatestSequenceNumber();
        if (latestSequenceNumber == null) {
            return 0;
        }

        return latestSequenceNumber + 1;
    }


    public BoardListResponse loadAllBoard(String username) {

        University university = findUniversityByMemberUsername(username);

        // 해당 대학에 존재하는 모든 게시판 조회
        List<SingleBoard> boards = boardRepository.loadAllBoardBy(university);

        return createBoardListResponse(university.getId(), boards);
    }

    private University findUniversityByMemberUsername(String email) {
        return universityRepository.findByMemberUsername(email)
                .orElseThrow(() -> new NotFoundException(UNIVERSITY_NOT_FOUND));
    }


    @Transactional
    public void changeBoardVisibility(ChangePopularBoardVisibilityServiceRequest request) {

        // 보고싶은 인기 게시판에 해당되는 게시판의 Id 번호 List
        // Ex) 동아리 인기 게시판과 익명 인기 게시판을 보고싶은 경우,
        //     동아리 게시판의 ID 번호와 익명 게시판의 ID 번호 목록
        List<Long> boardIds = request.getBoardIds();

        Member member = findMemberWithUniversityJoinedBy(request.getUsername());

        // 조회되길 희망하는 게시판 List
        List<Board> wantToSeeBoards = boardRepository.findAllWithUniversityByIdIn(boardIds);

        //TODO
        validateChangeBoardVisibilityRequest(boardIds, wantToSeeBoards, member.getUniversity());

        // DB에 저장된 해당 회원의 "인기 게시판" 모두 조회
        // 비활성화된 "인기 게시판"까지 전부 조회된다.
        //TODO 조회 정렬 순서 확인 필요
        List<PopularBoard> popularBoards = popularBoardRepository.findAllWithBoardOrderBySequenceByMember(
                member);

        changeVisibility(wantToSeeBoards, popularBoards);
    }

    private void validateChangeBoardVisibilityRequest(List<Long> boardIds,
            List<Board> wandToSeeBoards, University university) {

        // 클라이언트가 요청한 게시판 ID의 갯수와, 해당 ID를 토대로 실제 DB 에서 조회된 게시판의 갯수가 다른경우
        if (wandToSeeBoards.size() != boardIds.size()) {
            throw new NotFoundException(BOARD_NOT_FOUND);
        }

        // 조회된 게시판이 해당 회원의 대학에 있는 게시판이 아닌, 다른 대학의 게시판인 경우
        for (Board board : wandToSeeBoards) {
            if (board.getUniversity().equals(university) == false) {
                throw new AuthorizationFailedException();
            }
        }
    }

    private void changeVisibility(List<Board> wantToSeeBoards, List<PopularBoard> popularBoards) {
        popularBoards.forEach(popularBoard -> {

            // 해당 "보고싶은 게시판"이 조회되기를 원하는 경우
            if (wantToSeeThisDisabledPopularBoard(popularBoard, wantToSeeBoards)) {

                // 비활성화 상태인 "보고싶은 게시판"을
                // 다시 활성화 한 이후, 새로운 정렬 번호 부여
                setNewSequenceAndEnableThis(popularBoard);

                // 해당 "보고싶은 게시판"을 더이상 보고싶지 않은 경우
            } else if (dontWantToSeeThisPopularBoard(popularBoard, wantToSeeBoards)) {

                // 해당 "보고싶은 게시판"을 비활성화 처리
                disableThis(popularBoard);
            }
        });
    }


    // 해당 "보고싶은 게시판"이 비활성화 되어있지만, 이 게시판이 보고싶은 게시판 목록에 포함되어 있는 경우
    private boolean wantToSeeThisDisabledPopularBoard(PopularBoard popularBoard,
            List<Board> boardsToEnable) {

        return boardsToEnable.contains(popularBoard.getBoard()) && popularBoard.isEnabled();
    }

    // 비활성화 되어있는 해당 "보고싶은 게시판"을 활성화 시킨후, 새로운 정렬 번호 부여
    private void setNewSequenceAndEnableThis(PopularBoard popularBoard) {

        enableThis(popularBoard);

        editSequence(popularBoard, createNextSequenceNumber());
    }

    // 해당 "보고싶은 게시판"이 활성화 되어 있지만, 이 게시판이 보고싶은 게시판 목록에 더이상 포함되지 않는 경우
    private boolean dontWantToSeeThisPopularBoard(PopularBoard popularBoard,
            List<Board> boardsToEnable) {
        return boardsToEnable.contains(popularBoard.getBoard()) == false
                && popularBoard.isEnabled();
    }
}


