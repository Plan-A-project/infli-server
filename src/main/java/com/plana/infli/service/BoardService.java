package com.plana.infli.service;

import static com.plana.infli.domain.MemberBoard.createNewMemberBoard;
import static com.plana.infli.domain.editor.MemberBoardEditor.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.MemberBoard;
import com.plana.infli.domain.University;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.memberboard.MemberBoardRepository;
import com.plana.infli.web.dto.request.board.CreateBoardRequest;
import com.plana.infli.web.dto.request.board.CreateMemberBoardRequest;
import com.plana.infli.web.dto.request.board.EditMemberBoardRequest;
import com.plana.infli.web.dto.response.board.all.BoardListResponse;
import com.plana.infli.web.dto.response.board.all.SingleBoard;
import com.plana.infli.web.dto.response.board.member.MemberBoardListResponse;
import com.plana.infli.web.dto.response.board.member.MemberSingleBoard;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final UniversityService universityService;

    private final MemberUtil memberUtil;

    private final BoardRepository boardRepository;

    private final MemberBoardRepository memberBoardRepository;


    public Boolean existsByBoardName(String boardName) {
        return boardRepository.existsByBoardName(boardName);
    }

    @Transactional
    public void create(CreateBoardRequest request) {

        University university = universityService.findById(request.getUniversityId());
        Board board = Board.create(request.getBoardName(), university, request.getIsAnonymous());
        boardRepository.save(board);
    }


    public BoardListResponse findAllExistingBoard() {

        Member member = memberUtil.getContextMember();

        University university = universityService.findByMember(member);

        List<SingleBoard> boards = boardRepository.findAllExistingBoards(university);

        return new BoardListResponse(boards.size(), boards);
    }



    @Transactional
    public void createMemberBoard(CreateMemberBoardRequest request) {
        Member member = memberUtil.getContextMember();

        deleteAllMemberBoard(member);

        createNewMemberBoards(request, member);
    }

    private void createNewMemberBoards(CreateMemberBoardRequest request, Member member) {

        List<Long> boardIds = request.getIds();


        boardIds.forEach(i -> {
            Board board = boardRepository.findBoardById(i);

            MemberBoard memberBoard = createNewMemberBoard(member, board);

            memberBoardRepository.save(memberBoard);
        });
    }

    public void deleteAllMemberBoard(Member member) {
        memberBoardRepository.bulkDeleteExistingMemberBoard(member);
    }


    public MemberBoardListResponse findAllMemberBoard() {
        Member member = memberUtil.getContextMember();

        List<MemberSingleBoard> boards = memberBoardRepository.findMemberBoards(member);

        return new MemberBoardListResponse(boards.size(), boards);
    }

    @Transactional
    public void edit(EditMemberBoardRequest request) {

        List<Long> ids = request.getMemberBoardIds();

        for (int i = 0; i < ids.size(); i++) {
            MemberBoard memberBoard = memberBoardRepository.findMemberBoardById(ids.get(i));
            editOrder((long) i, memberBoard);
        }
    }

    @Transactional
    public void delete(Long boardId) {
        Board board = boardRepository.findBoardById(boardId);

        boardRepository.delete(board);
    }
}
