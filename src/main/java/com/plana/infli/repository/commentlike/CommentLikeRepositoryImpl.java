package com.plana.infli.repository.commentlike;

import static com.plana.infli.domain.QCommentLike.*;
import static java.util.Optional.*;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.CommentLike;
import com.plana.infli.domain.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentLikeRepositoryImpl implements CommentLikeRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<CommentLike> findByCommentAndMember(Comment comment, Member member) {
        return ofNullable(jpaQueryFactory.selectFrom(commentLike)
                .where(commentLike.member.eq(member))
                .where(commentLike.comment.eq(comment))
                .fetchOne());
    }
}
