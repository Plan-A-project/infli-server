package com.plana.infli.factory;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.PopularBoard;
import com.plana.infli.repository.popularboard.PopularBoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PopularBoardFactory {

    @Autowired
    private PopularBoardRepository popularBoardRepository;

    public PopularBoard create(Member member, Board board, int sequence) {
        PopularBoard popularBoard = PopularBoard.builder()
                .board(board)
                .member(member)
                .sequence(sequence)
                .build();

        return popularBoardRepository.save(popularBoard);
    }
}
