package com.plana.infli.domain;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
@SQLDelete(sql = "UPDATE post SET is_deleted = true WHERE post_id=?")
public class Post extends BaseEntity {


    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String title;

    private String main;

    private boolean notice;

    private boolean isDeleted = false;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    //TODO 회원 컬럼

    private int viewCount;

    @Builder
    private Post(Member member, Board board, String title, String main) {
        this.member = member;
        this.board = board;
        this.title = title;
        this.main = main;
    }

    public static Post create(Member member, Board board, String title, String main) {
        return Post.builder()
                .member(member)
                .board(board)
                .title(title)
                .main(main)
                .build();
    }
}
