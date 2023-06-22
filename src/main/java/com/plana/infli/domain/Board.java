package com.plana.infli.domain;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import com.plana.infli.web.dto.request.board.CreateBoardRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE board SET is_enabled = false WHERE board_id=?")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @Column(unique = true)
    private String boardName;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "university_id")
    private University university;

    private boolean isEnabled = true;

    private boolean isAnonymous;

    public Board(String boardName, University university, Boolean isAnonymous) {
        this.boardName = boardName;
        this.university = university;
        this.isAnonymous = isAnonymous;
    }

    public static Board create(String boardName, University university, Boolean isAnonymous) {
        return new Board(boardName, university, isAnonymous);
    }

}
