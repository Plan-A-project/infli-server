package com.plana.infli.repository.comment;

import static com.plana.infli.domain.Board.*;
import static com.plana.infli.domain.QComment.comment;
import static com.plana.infli.domain.QCommentLike.*;
import static com.plana.infli.domain.QMember.member;
import static com.plana.infli.domain.QPost.post;
import static com.querydsl.core.types.dsl.Expressions.*;
import static com.querydsl.core.types.dsl.Expressions.booleanPath;
import static com.querydsl.core.types.dsl.Expressions.nullExpression;
import static com.querydsl.jpa.JPAExpressions.*;
import static java.util.Optional.*;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.web.dto.response.comment.view.BestCommentResponse;
import com.plana.infli.web.dto.response.comment.view.QBestCommentResponse;
import com.plana.infli.web.dto.response.comment.view.mycomment.MyComment;
import com.plana.infli.web.dto.response.comment.view.mycomment.QMyComment;
import com.plana.infli.web.dto.response.comment.view.post.PostComment;
import com.plana.infli.web.dto.response.comment.view.post.QPostComment;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;


    @Override
    public List<PostComment> findCommentsInPost(Post findPost, Member findMember,
            PageRequest pageRequest) {

        boolean isAnonymousBoard = isAnonymousBoard(findPost.getBoard());

        return jpaQueryFactory
                .select(new QPostComment(comment.id,
                        nicknameEq(isAnonymousBoard),
                        profileImageUrlEq(isAnonymousBoard),
                        comment.isDeleted, comment.identifierNumber,
                        comment.createdAt, comment.content,
                        comment.member.eq(findMember),
                        comment.commentLikes.size(),
                        comment.id.in(select(commentLike.comment.id)
                                .from(commentLike)
                                .where(commentLike.member.eq(findMember))
                                .where(commentLike.comment.post.eq(findPost))),
                        comment.parentComment.isNull(), comment.isEdited,
                        comment.member.eq(findPost.getMember())))
                .from(comment)
                .innerJoin(comment.member, member)
                .innerJoin(comment.post, post)
                .where(comment.post.eq(findPost))
                .where(comment.isDeleted.isFalse()
                        .or(comment.isDeleted.isTrue()
                                .and(comment.children.isNotEmpty()
                                        .and(comment.children.any().isDeleted.isFalse()))))
                .orderBy(comment.root.id.asc(), comment.id.asc())
                .offset((long) (pageRequest.getPageNumber() - 1) * pageRequest.getPageSize())
                .limit(pageRequest.getPageSize())
                .fetch();
    }

    @Override
    public BestCommentResponse findBestCommentIn(Post findPost) {

        boolean isAnonymousBoard = isAnonymousBoard(findPost.getBoard());

        return jpaQueryFactory
                .select(new QBestCommentResponse(comment.id, comment.post.id,
                        constant(isAnonymousBoard),
                        nicknameEq(isAnonymousBoard),
                        profileImageUrlEq(isAnonymousBoard),
                        comment.identifierNumber, comment.createdAt,
                        comment.content, comment.commentLikes.size()))
                .from(comment)
                .innerJoin(comment.member, member)
                .innerJoin(comment.post, post)
                .where(comment.post.eq(findPost))
                .where(comment.isDeleted.isFalse())
                .where(comment.commentLikes.size().goe(10))
                .orderBy(comment.commentLikes.size().desc())
                .fetchFirst();
    }

    @Override
    public List<MyComment> findMyComments(Member findMember, PageRequest pageRequest) {
        return jpaQueryFactory
                .select(new QMyComment(comment.id, comment.post.id,
                        comment.content, comment.createdAt))
                .from(comment)
                .innerJoin(comment.member, member)
                .innerJoin(comment.post, post)
                .where(comment.member.eq(findMember))
                .where(comment.isDeleted.isFalse())
                .orderBy(comment.id.desc())
                .offset((long) (pageRequest.getPageNumber() - 1) * pageRequest.getPageSize())
                .limit(pageRequest.getPageSize())
                .fetch();
    }

    @Override
    public Long findActiveCommentsCountBy(Member findMember) {
        return jpaQueryFactory.select(comment.count())
                .from(comment)
                .innerJoin(comment.member, member)
                .where(comment.isDeleted.isFalse())
                .where(comment.member.eq(findMember))
                .fetchOne();
    }

    @Override
    public List<Comment> findAllOrderByIdAsc() {
        return jpaQueryFactory.selectFrom(comment)
                .orderBy(comment.id.asc())
                .fetch();
    }

    private Expression<String> profileImageUrlEq(boolean isAnonymousBoard) {
        return isAnonymousBoard ? nullExpression() : comment.member.profileImageUrl;
    }

    private  Expression<String> nicknameEq(boolean isAnonymousBoard) {
        return isAnonymousBoard ? nullExpression() : comment.member.nickname;
    }


    @Override
    public Long findActiveCommentsCountIn(Post findPost) {
        Long count = jpaQueryFactory.select(comment.count())
                .from(comment)
                .innerJoin(comment.post, post)
                .where(comment.post.eq(findPost))
                .where(comment.isDeleted.isFalse())
                .fetchOne();

        return count != null ? count : 0;
    }

    @Override
    public void deleteAllByIdsInBatch(List<Long> ids) {
        jpaQueryFactory.update(comment)
                .set(comment.isDeleted, true)
                .where(comment.id.in(ids))
                .execute();
    }

    @Override
    public Optional<Comment> findActiveCommentWithMemberAndPostBy(Long commentId) {
        return ofNullable(jpaQueryFactory.selectFrom(comment)
                .innerJoin(comment.member, member).fetchJoin()
                .innerJoin(comment.post, post).fetchJoin()
                .where(comment.isDeleted.isFalse())
                .where(comment.id.eq(commentId))
                .fetchOne());
    }

    @Override
    public List<Comment> findActiveCommentWithMemberByIdsIn(List<Long> ids) {
        return jpaQueryFactory.selectFrom(comment)
                .innerJoin(comment.member, member).fetchJoin()
                .where(comment.isDeleted.isFalse())
                .where(comment.id.in(ids))
                .fetch();
    }

    @Override
    public Integer findIdentifierNumberBy(Post findPost, Member findMember) {
        return jpaQueryFactory.select(comment.identifierNumber)
                .from(comment)
                .where(comment.post.eq(findPost))
                .where(comment.member.eq(findMember))
                .fetchFirst();
    }

    @Override
    public Integer findLatestIdentifierNumberBy(Post findPost) {
        return jpaQueryFactory.select(comment.identifierNumber)
                .from(comment)
                .innerJoin(comment.post, post)
                .where(comment.post.eq(findPost))
                .orderBy(comment.identifierNumber.desc())
                .fetchFirst();
    }

    @Override
    public Long findAllActiveCommentCount() {
        return jpaQueryFactory.select(comment.count())
                .from(comment)
                .where(comment.isDeleted.isFalse())
                .fetchOne();
    }


}
