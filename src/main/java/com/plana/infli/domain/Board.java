package com.plana.infli.domain;

import static com.plana.infli.domain.BoardType.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import com.plana.infli.exception.custom.AuthorizationFailedException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
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
@SQLDelete(sql = "UPDATE board SET is_deleted = true WHERE board_id=?")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = STRING)
    private BoardType boardType;

    @Column(nullable = false)
    private String boardName;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "university_id")
    private University university;

    private int sequence;

    private boolean isDeleted = false;

    @Builder
    private Board(BoardType boardType, University university) {
        this.boardType = boardType;
        this.boardName = boardType.getBoardName();
        this.university = university;
        this.sequence = boardType.getDefaultSequence();
    }

    public static Board create(BoardType boardType, University university) {
        return new Board(boardType, university);
    }

    public void hasWritePermissionByWithThis(Role role) {
        if (boardType.getRoles().contains(role) == false) {
            throw new AuthorizationFailedException();
        }
    }

    public static boolean isAnonymousBoard(Board board) {
        return board.getBoardType().equals(ANONYMOUS);
    }
}
