package com.plana.infli.repository.comment;

import static com.plana.infli.domain.QComment.*;
import static com.plana.infli.domain.QMember.*;
import static com.plana.infli.service.MemberUtil.getAuthenticatedEmail;

import com.plana.infli.web.dto.response.comment.postcomment.PostComment;
import com.plana.infli.web.dto.response.comment.postcomment.QPostComment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void bulkDeleteByIds(List<Long> ids) {
        jpaQueryFactory.update(comment)
                .set(comment.isEnabled, false)
                .where(comment.id.in(ids))
                .execute();
    }

    @Override
    public List<PostComment> findCommentsInPostBy(Long postId, Integer page) {
        return jpaQueryFactory
                .select(new QPostComment(comment.id, comment.isEnabled, comment.content,
                        comment.member.nickname, comment.member.email.eq(getAuthenticatedEmail()),
                        comment.parentComment.isNull(), comment.createdAt))
                .from(comment)
                .innerJoin(comment.member, member)
                .where(comment.post.id.eq(postId))
                .where(comment.isEnabled.isTrue()
                        .or(comment.isEnabled.isFalse()
                                .and(comment.children.isNotEmpty()
                                        .and(comment.children.any().isEnabled.isTrue()))
                ))
                .orderBy(comment.root.id.asc(), comment.id.asc())
                .offset((page - 1) * 100L)
                .limit(100)
                .fetch();
    }


    @Override
    public Long findCommentCountInPostBy(Long postId) {
        return jpaQueryFactory.select(comment.count())
                .from(comment)
                .where(comment.post.id.eq(postId))
                .where(comment.isEnabled.isTrue())
                .fetchOne();
    }

}
