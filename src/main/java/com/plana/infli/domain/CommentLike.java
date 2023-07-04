package com.plana.infli.domain;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class CommentLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "comment_like_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String email;

    @Builder
    private CommentLike(Comment comment, Member member) {
        this.member = member;
        this.email = member.getEmail();
        //양방향 연관관계 설정
        bindCommentAndLike(comment);
    }

    private void bindCommentAndLike(Comment comment) {
        this.comment = comment;
        comment.getCommentLikes().add(this);
    }

    public static CommentLike create(Comment comment, Member member) {
        return CommentLike.builder()
                .comment(comment)
                .member(member)
                .build();
    }
}
