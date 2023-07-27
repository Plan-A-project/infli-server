package com.plana.infli.repository.post;

import static com.plana.infli.domain.BoardType.*;
import static com.plana.infli.domain.PostType.*;
import static com.plana.infli.domain.QBoard.*;
import static com.plana.infli.domain.QComment.*;
import static com.plana.infli.domain.QMember.*;
import static com.plana.infli.domain.QPost.*;
import static com.plana.infli.domain.QPostLike.*;
import static com.plana.infli.web.dto.request.post.view.PostQueryRequest.PostViewOrder.popular;
import static com.querydsl.core.types.dsl.Expressions.*;
import static com.querydsl.core.types.dsl.Expressions.nullExpression;
import static com.querydsl.jpa.JPAExpressions.*;
import static java.util.Collections.*;
import static java.util.Optional.*;
import static java.util.stream.Collectors.groupingBy;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.web.dto.request.post.view.PostQueryRequest;
import com.plana.infli.web.dto.request.post.view.PostQueryRequest.PostViewOrder;
import com.plana.infli.web.dto.response.post.QCommentCount;
import com.plana.infli.web.dto.response.post.board.BoardPost;
import com.plana.infli.web.dto.response.post.DefaultPost;
import com.plana.infli.web.dto.response.post.board.QBoardPost;
import com.plana.infli.web.dto.response.post.my.MyPost;
import com.plana.infli.web.dto.response.post.my.QMyPost;
import com.plana.infli.web.dto.response.post.CommentCount;
import com.plana.infli.web.dto.response.post.search.QSearchedPost;
import com.plana.infli.web.dto.response.post.search.SearchedPost;
import com.plana.infli.web.dto.response.post.single.QSinglePostResponse;
import com.plana.infli.web.dto.response.post.single.SinglePostResponse;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Post> findActivePostBy(Long id) {
        return ofNullable(jpaQueryFactory.selectFrom(post)
                .where(postIsActiveAndIdEqual(id))
                .fetchOne());
    }

    private BooleanExpression postIsActiveAndIdEqual(Long id) {
        return postIsActive().and(postIdEqual(id));
    }

    private BooleanExpression postIsActive() {
        return post.isDeleted.isFalse();
    }

    private BooleanExpression postIdEqual(Long id) {
        return post.id.eq(id);
    }

    @Override
    public Optional<Post> findActivePostWithBoardBy(Long id) {
        return ofNullable(jpaQueryFactory.selectFrom(post)
                .innerJoin(post.board, board).fetchJoin()
                .where(postIsActiveAndIdEqual(id))
                .fetchOne());
    }

    @Override
    public Optional<Post> findPessimisticLockActivePostWithBoardAndMemberBy(Long id) {
        return ofNullable(jpaQueryFactory.selectFrom(post)
                .innerJoin(post.board, board).fetchJoin()
                .where(postIsActiveAndIdEqual(id))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }


    @Override
    public Optional<Post> findActivePostWithMemberBy(Long id) {
        return ofNullable(jpaQueryFactory
                .selectFrom(post)
                .innerJoin(post.member, member).fetchJoin()
                .where(postIsActiveAndIdEqual(id))
                .fetchOne());
    }

    @Override
    public SinglePostResponse loadSinglePostResponse(PostQueryRequest request) {

        return jpaQueryFactory.select(new QSinglePostResponse(
                        post.board.boardName, post.board.id, post.postType.stringValue(),
                        nicknameEq(), post.id, post.title, post.content,
                        post.createdAt, postWriterEqual(request.getMember()),
                        isAdmin(request.getMember()), post.viewCount, post.likes.size(),
                        pressedLikeOnThisPost(request.getMember()), post.thumbnailUrl,
                        companyNameEqual(), recruitmentStartDateEqual(), recruitmentEndDateEqual()))
                .from(post)
                .where(post.eq(request.getPost()))
                .fetchOne();
    }


    private StringExpression nicknameEq() {
        return new CaseBuilder()
                .when(post.board.boardType.eq(ACTIVITY)).then(post.member.nickname)
                .when(post.board.boardType.eq(ANONYMOUS)).then(nullExpression())
                .otherwise(post.member.nickname);
    }

    private BooleanExpression pressedLikeOnThisPost(Member member) {
        return post.in(myLikedPosts(member));
    }

    private JPQLQuery<Post> myLikedPosts(Member findMember) {
        return select(postLike.post)
                .from(postLike)
                .where(postLike.member.eq(findMember));
    }

    private BooleanExpression postWriterEqual(Member findMember) {
        return post.member.eq(findMember);
    }

    private StringExpression companyNameEqual() {
        return new CaseBuilder()
                .when(post.postType.eq(RECRUITMENT)).then(post.recruitment.companyName)
                .otherwise(nullExpression());
    }

    private DateTimeExpression<LocalDateTime> recruitmentStartDateEqual() {
        return new CaseBuilder()
                .when(postTypeEqual(RECRUITMENT)).then(post.recruitment.startDate)
                .otherwise(nullExpression());
    }

    private DateTimeExpression<LocalDateTime> recruitmentEndDateEqual() {
        return new CaseBuilder()
                .when(postTypeEqual(RECRUITMENT)).then(post.recruitment.endDate)
                .otherwise(nullExpression());
    }

    private Expression<Boolean> isAdmin(Member member) {
        return Member.isAdmin(member) ? asBoolean(true) : asBoolean(false);
    }

    @Override
    public List<MyPost> loadMyPosts(PostQueryRequest request) {

        List<Long> ids = findMyPostIds(request);

        List<MyPost> posts = findMyPosts(ids, request.getMember());

        setCommentCount(ids, posts);

        return null;
    }

    private List<Long> findMyPostIds(PostQueryRequest request) {
        return jpaQueryFactory.select(post.id)
                .from(post)
                .where(postIsActiveAndWriterEqual(request.getMember()))
                .orderBy(post.id.desc())
                .offset(request.getOffset())
                .limit(request.getSize())
                .fetch();
    }

    private List<MyPost> findMyPosts(List<Long> ids, Member findMember) {

        return jpaQueryFactory.select(
                        new QMyPost(post.id, post.title, pressedLikeOnThisPost(findMember),
                                post.likes.size(), post.viewCount, post.createdAt,
                                post.thumbnailUrl, post.board.boardName,
                                companyNameEqual(),
                                recruitmentStartDateEqual(),
                                recruitmentEndDateEqual()))
                .from(post)
                .where(idsEqual(ids))
                .orderBy(post.id.desc())
                .fetch();
    }


    @Override
    public List<BoardPost> loadPostsByBoard(PostQueryRequest request) {

        List<Long> ids = findPostIdsByBoard(request);

        List<BoardPost> posts = findPostsByBoard(request, ids);

        setCommentCount(ids, posts);

        return posts;
    }

    private List<Long> findPostIdsByBoard(PostQueryRequest request) {
        return jpaQueryFactory.select(post.id)
                .from(post)
                .where(post.board.eq(request.getBoard()))
                .where(postIsActive())
                .where(viewOrderEqual(request.getViewOrder()))
                .where(postTypeEqual(request.getType()))
                .offset(request.getOffset())
                .limit(request.getSize())
                .orderBy(post.id.desc())
                .fetch();
    }

    private List<BoardPost> findPostsByBoard(PostQueryRequest request, List<Long> ids) {
        return jpaQueryFactory
                .select(new QBoardPost(post.id, post.title, post.createdAt, post.thumbnailUrl,
                        getMemberRole(request.getBoard()), post.likes.size(),
                        pressedLikeOnThisPost(request.getMember()), post.viewCount,
                        post.recruitment.companyName, post.recruitment.startDate,
                        post.recruitment.endDate))
                .from(post)
                .where(post.id.in(ids))
                .orderBy(post.id.desc())
                .fetch();

    }

    private Expression<String> getMemberRole(Board board) {
        return board.getBoardType().equals(ANONYMOUS) ? nullExpression()
                : post.member.role.stringValue();
    }


    private BooleanExpression idsEqual(List<Long> ids) {
        return post.id.in(ids);
    }



    private BooleanExpression viewOrderEqual(PostViewOrder viewOrder) {
        return viewOrder == popular ? postIsPopularPost() : null;
    }

    //TODO 인기글 선정 기준
    private BooleanExpression postIsPopularPost() {
        return post.commentMemberCount.goe(3)
                .or(post.viewCount.goe(20)
                        .or(post.likes.size().goe(3)));
    }

    private BooleanExpression postTypeEqual(PostType postType) {
        return post.postType.eq(postType);
    }


    private BooleanExpression postIsActiveAndWriterEqual(Member findMember) {
        return postIsActive().and(postWriterEqual(findMember));
    }



    @Override
    public List<SearchedPost> searchPostByKeyWord(PostQueryRequest request) {

        List<Long> ids = findPostIds(request);

        List<SearchedPost> result = searchPostsByPostIds(ids, request.getMember());

        setCommentCount(ids, result);

        return result;
    }

    private List<Long> findPostIds(PostQueryRequest request) {
        return jpaQueryFactory.select(post.id)
                .from(post)
                .where(post.board.university.eq(request.getMember().getUniversity()))
                .where(contentOrTitleEq(request.getKeyword()))
                .where(postIsActive())
                .orderBy(post.id.desc())
                .offset(request.getOffset())
                .limit(request.getSize())
                .fetch();
    }

    private BooleanExpression contentOrTitleEq(String keyword) {
        String[] keyWords = keyword.trim().split("\\s+");

        return Arrays.stream(keyWords)
                .map(word -> post.title.containsIgnoreCase(word)
                        .or(post.content.containsIgnoreCase(word)))
                .reduce(BooleanExpression::and)
                .orElse(null);
    }


    private List<SearchedPost> searchPostsByPostIds(List<Long> ids, Member member) {
        return jpaQueryFactory
                .select(new QSearchedPost(
                        post.id, post.title, post.likes.size(), pressedLikeOnThisPost(member),
                        post.viewCount, post.createdAt, post.thumbnailUrl, post.content))
                .from(post)
                .where(idsEqual(ids))
                .orderBy(post.id.desc())
                .fetch();
    }

    private void setCommentCount(List<Long> ids, List<? extends DefaultPost> result) {

        Map<Long, List<CommentCount>> map = findCommentCount(ids);

        result.forEach(post -> post.loadCommentCount(findCommentCounts(map, post)));
    }

    private Map<Long, List<CommentCount>> findCommentCount(List<Long> ids) {
        return jpaQueryFactory
                .select(new QCommentCount(comment.post.id))
                .from(comment)
                .innerJoin(comment.post, post)
                .where(comment.post.id.in(ids))
                .where(comment.isDeleted.isFalse())
                .fetch().stream().collect(groupingBy(CommentCount::getPostId));
    }

    private int findCommentCounts(Map<Long, List<CommentCount>> map, DefaultPost post) {
        return map.getOrDefault(post.getPostId(), emptyList()).size();
    }
}
