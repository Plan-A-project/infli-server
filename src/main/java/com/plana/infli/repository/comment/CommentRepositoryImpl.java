package com.plana.infli.repository.comment;

import static com.plana.infli.domain.QComment.comment;
import static com.plana.infli.domain.QCommentLike.*;
import static com.plana.infli.domain.QMember.member;
import static com.plana.infli.domain.QPost.post;
import static com.plana.infli.domain.type.BoardType.*;
import static com.querydsl.core.types.dsl.Expressions.asBoolean;
import static com.querydsl.core.types.dsl.Expressions.booleanPath;
import static com.querydsl.core.types.dsl.Expressions.nullExpression;
import static com.querydsl.jpa.JPAExpressions.*;
import static java.util.Optional.*;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.web.dto.request.comment.view.CommentQueryRequest;
import com.plana.infli.web.dto.response.comment.view.BestCommentResponse;
import com.plana.infli.web.dto.response.comment.view.QBestCommentResponse;
import com.plana.infli.web.dto.response.comment.view.mycomment.MyComment;
import com.plana.infli.web.dto.response.comment.view.mycomment.QMyComment;
import com.plana.infli.web.dto.response.comment.view.post.PostComment;
import com.plana.infli.web.dto.response.comment.view.post.QPostComment;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;


    @Override
    public List<PostComment> findCommentsInPost(CommentQueryRequest request) {

        return jpaQueryFactory
                .select(new QPostComment(comment.id,
                        nicknameEq(),
                        profileImageUrlEq(),
                        comment.status.isDeleted,
                        comment.identifierNumber,
                        comment.createdAt,
                        comment.content,
                        isMyComment(request.getMember()),
                        comment.commentLikes.size(),
                        getPressedLikeOnThisComment(request),
                        comment.parentComment.isNull(),
                        comment.status.isEdited,
                        isMyComment(request.getPost().getMember()))
                )
                .from(comment)
                .leftJoin(comment.member, member)
                .where(comment.post.eq(request.getPost()))
                .where(commentIsNotDeleted()
                        .or(commentIsDeletedButChildCommentExists()))
                .orderBy(comment.root.id.asc(), comment.id.asc())
                .offset(request.getOffset())
                .limit(request.getSize())
                .fetch();
    }


    private StringExpression nicknameEq() {
        return new CaseBuilder()
                .when(commentIsNotAnonymous()).then(comment.member.basicCredentials.nickname)
                .otherwise(nullExpression());
    }

    private BooleanExpression commentIsNotAnonymous() {
        return comment.post.board.boardType
                .in(List.of(EMPLOYMENT, ACTIVITY, CLUB, CAMPUS_LIFE));
    }

    private StringExpression profileImageUrlEq() {
        return new CaseBuilder()
                .when(commentIsNotAnonymous())
                .then(comment.member.profileImage.thumbnailUrl)
                .otherwise(nullExpression());
    }

    private  BooleanExpression isMyComment(Member findMember) {
        return comment.in(myComments(findMember));
    }


    private JPQLQuery<Comment> myComments(Member findMember) {
        return selectFrom(comment)
                .where(comment.member.eq(findMember));
    }

    private BooleanExpression getPressedLikeOnThisComment(CommentQueryRequest request) {
        return comment.in(myLikedCommentsInThisPost(request.getPost(), request.getMember()));
    }

    private JPQLQuery<Comment> myLikedCommentsInThisPost(Post findPost, Member findMember) {
        return select(commentLike.comment)
                .from(commentLike)
                .where(commentLike.member.eq(findMember))
                .where(commentLike.comment.post.eq(findPost));
    }

    private BooleanExpression commentIsNotDeleted() {
        return comment.status.isDeleted.isFalse();
    }

    private BooleanExpression commentIsDeletedButChildCommentExists() {
        return comment.status.isDeleted.isTrue()
                .and(comment.children.isNotEmpty()
                        .and(comment.children.any().status.isDeleted.isFalse()));
    }

    @Override
    public BestCommentResponse findBestCommentIn(Post findPost) {

        return jpaQueryFactory
                .select(new QBestCommentResponse(comment.id, nicknameEq(), profileImageUrlEq(),
                        comment.identifierNumber, comment.createdAt,
                        comment.content, comment.commentLikes.size()))
                .from(comment)
                .innerJoin(comment.member, member)
                .innerJoin(comment.post, post)
                .where(comment.post.eq(findPost))
                .where(commentIsNotDeleted())
                .where(comment.commentLikes.size().goe(10))
                .orderBy(comment.commentLikes.size().desc())
                .fetchFirst();
    }

    @Override
    public List<MyComment> findMyComments(CommentQueryRequest request) {
        return jpaQueryFactory
                .select(new QMyComment(comment.id, comment.post.board.id,
                        comment.post.id, comment.content, comment.createdAt))
                .from(comment)
                .innerJoin(comment.member, member)
                .innerJoin(comment.post, post)
                .where(comment.member.eq(request.getMember()))
                .where(commentIsNotDeleted())
                .orderBy(comment.id.desc())
                .offset(request.getOffset())
                .limit(request.getSize())
                .fetch();
    }

    @Override
    public Optional<Comment> findActiveCommentWithPostBy(Long commentId) {
        return ofNullable(jpaQueryFactory.selectFrom(comment)
                .where(comment.id.eq(commentId))
                .where(comment.status.isDeleted.isFalse())
                .innerJoin(comment.post, post).fetchJoin()
                .fetchOne());
    }

    @Override
    public Optional<Comment> findActiveCommentWithMemberBy(Long commentId) {
        return ofNullable(jpaQueryFactory.selectFrom(comment)
                .where(commentIsNotDeleted())
                .where(comment.id.eq(commentId))
                .innerJoin(comment.member, member).fetchJoin()
                .fetchOne());
    }



    @Override
    public Long findActiveCommentsCountIn(Post findPost) {
        Long count = jpaQueryFactory.select(comment.count())
                .from(comment)
                .innerJoin(comment.post, post)
                .where(comment.post.eq(findPost))
                .where(commentIsNotDeleted())
                .fetchOne();

        return count != null ? count : 0;
    }


    @Override
    public Optional<Comment> findActiveCommentWithMemberAndPostBy(Long commentId) {
        return ofNullable(jpaQueryFactory.selectFrom(comment)
                .innerJoin(comment.member, member).fetchJoin()
                .innerJoin(comment.post, post).fetchJoin()
                .where(commentIsNotDeleted())
                .where(comment.id.eq(commentId))
                .fetchOne());
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
    public Long findAllActiveCommentCount() {
        return jpaQueryFactory.select(comment.count())
                .from(comment)
                .where(commentIsNotDeleted())
                .fetchOne();
    }


}
