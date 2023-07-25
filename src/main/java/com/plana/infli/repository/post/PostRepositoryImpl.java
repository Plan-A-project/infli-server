package com.plana.infli.repository.post;

import static com.plana.infli.domain.Board.*;
import static com.plana.infli.domain.BoardType.*;
import static com.plana.infli.domain.QBoard.*;
import static com.plana.infli.domain.QComment.*;
import static com.plana.infli.domain.QMember.*;
import static com.plana.infli.domain.QPost.*;
import static com.plana.infli.web.dto.request.post.view.PostViewOrder.popular;
import static com.querydsl.core.types.dsl.Expressions.*;
import static com.querydsl.core.types.dsl.Expressions.nullExpression;
import static java.util.Collections.*;
import static java.util.Optional.*;
import static java.util.stream.Collectors.groupingBy;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.service.PostService.KeywordSearch;
import com.plana.infli.web.dto.request.post.view.PostViewOrder;
import com.plana.infli.web.dto.request.post.view.board.LoadPostsByBoardServiceRequest;
import com.plana.infli.web.dto.response.post.BoardPostDTO;
import com.plana.infli.web.dto.response.post.QBoardPostDTO;
import com.plana.infli.web.dto.response.post.board.normal.NormalPost;
import com.plana.infli.web.dto.response.post.board.normal.QNormalPost;
import com.plana.infli.web.dto.response.post.my.MyPost;
import com.plana.infli.web.dto.response.post.my.QMyPost;
import com.plana.infli.web.dto.response.post.search.CommentCount;
import com.plana.infli.web.dto.response.post.search.QCommentCount;
import com.plana.infli.web.dto.response.post.search.QSearchedPost;
import com.plana.infli.web.dto.response.post.search.SearchedPost;
import com.plana.infli.web.dto.response.post.single.QSinglePostResponse;
import com.plana.infli.web.dto.response.post.single.SinglePostResponse;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
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
        return post.isDeleted.isFalse().and(post.isPublished.isTrue());
    }

    private BooleanExpression postIdEqual(Long id) {
        return post.id.eq(id);
    }

    @Override
    public Optional<Post> findNotDeletedPostWithMemberBy(Long id) {
        return ofNullable(jpaQueryFactory
                .selectFrom(post)
                .innerJoin(post.member, member).fetchJoin()
                .where(post.isDeleted.isFalse())
                .where(postIdEqual(id))
                .fetchOne());
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
    public SinglePostResponse loadSinglePostResponse(Post findPost, Member findMember) {

        boolean isAnonymousBoard = isAnonymousBoard(findPost.getBoard());

        return jpaQueryFactory.select(new QSinglePostResponse(
                        post.board.boardName, post.board.id, post.postType.stringValue(),
                        nicknameEq(isAnonymousBoard), post.id, post.title, post.content, post.createdAt,
                        post.member.eq(findMember), isAdmin(findMember), post.viewCount, post.likes.size(),
                        post.thumbnailUrl))
                .from(post)
                .where(postEqual(findPost))
                .fetchOne();
    }

    private BooleanExpression postEqual(Post findPost) {
        return post.eq(findPost);
    }

    private Expression<String> nicknameEq(boolean isAnonymousBoard) {
        return isAnonymousBoard ? nullExpression() : post.member.nickname;
    }

    private Expression<Boolean> isAdmin(Member member) {
        return Member.isAdmin(member) ? asBoolean(true) : asBoolean(false);
    }

    @Override
    public List<MyPost> loadMyPosts(Member findMember, int intPage) {

//        List<Long> ids = findMyPostIds(findMember, intPage);
//
//        List<MyPost> posts = findMyPosts(ids);
//
//        setCommentCount(ids, posts);

        return null;
    }

    @Override
    public List<BoardPostDTO> loadPostsByBoard(Board findBoard,
            LoadPostsByBoardServiceRequest request) {

        List<Long> ids = findPostIdsByBoard(findBoard, request);

        List<BoardPostDTO> posts = findPostsByBoard(findBoard, ids);

        setCommentCount(ids, posts);

        return null;
    }

    private List<Long> findPostIdsByBoard(Board findBoard, LoadPostsByBoardServiceRequest request) {
        return jpaQueryFactory.select(post.id)
                .from(post)
                .where(post.board.eq(findBoard))
                .where(postIsActive())
                .where(viewOrderEqual(request.getOrder()))
                .where(postTypeEqual(request.getType()))
                .orderBy(post.id.desc())
                .offset(getOffset(request.getPage()))
                .limit(20)
                .fetch();
    }


    private List<BoardPostDTO> findPostsByBoard(Board findBoard, List<Long> ids) {
        return jpaQueryFactory.select(
                        new QBoardPostDTO(post.id, post.title, post.createdAt, post.thumbnailUrl,
                                getMemberRole(findBoard), post.likes.size(), post.viewCount,
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

//    @Override
//    public List<NormalPost> findNormalPostsByBoard(Board findBoard, int page,
//            PostViewOrder viewOrder) {
//
//        List<Long> ids = findPostIdsByBoard(findBoard, page, viewOrder);
//
//        List<NormalPost> posts = loadNormalBoardPostsBy(ids);
//
//        setCommentCount(ids, posts);
//
//        return posts;
//    }
//
//    @Override
//
//    public List<AnnouncementPost> loadAnnouncementPostsByBoard(Board board, int page,
//            PostViewOrder viewOrder) {
//        return null;
//    }
//
//    private List<Long> findPostIdsByBoard(Board findBoard, int page, PostViewOrder viewOrder) {
//
//        return jpaQueryFactory.select(post.id)
//                .from(post)
//                .where(boardEqual(findBoard))
//                .where(postIsActive())
//                .where(viewOrderEqual(viewOrder))
//                .where(postTypeEqual(NORMAL))
//                .offset(getOffset(page))
//                .limit(20)
//                .orderBy(post.id.desc())
//                .fetch();
//    }

    private List<NormalPost> loadNormalBoardPostsBy(List<Long> ids) {
        return jpaQueryFactory.select(
                        new QNormalPost(post.id, post.title, post.likes.size(), post.viewCount,
                                post.createdAt, post.thumbnailUrl))
                .from(post)
                .where(idsEqual(ids))
                .orderBy(post.id.desc())
                .fetch();
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

    private BooleanExpression boardEqual(Board findBoard) {
        return post.board.eq(findBoard);
    }

    private List<Long> findMyPostIds(Member findMember, int intPage) {
        return jpaQueryFactory.select(post.id)
                .from(post)
                .where(postIsActiveAndWriterEqual(findMember))
                .orderBy(post.id.desc())
                .offset(getOffset(intPage))
                .limit(20)
                .fetch();
    }

    private BooleanExpression postIsActiveAndWriterEqual(Member findMember) {
        return postIsActive().and(postWriterEqual(findMember));
    }


    private BooleanExpression postWriterEqual(Member findMember) {
        return post.member.eq(findMember);
    }

    private List<MyPost> findMyPosts(List<Long> ids) {
        return jpaQueryFactory.select(
                        new QMyPost(post.id, post.title, post.likes.size(), post.viewCount, post.createdAt,
                                post.thumbnailUrl, post.board.boardName))
                .from(post)
                .where(idsEqual(ids))
                .orderBy(post.id.desc())
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

//    @Override
//    public List<SearchedPost> searchPostByKeyWord(KeywordSearch request) {
//
//        List<Long> ids = findPostIds(request);
//
//        List<SearchedPost> result = searchPostsByIds(ids);
//
//        setCommentCount(ids, result);
//
//        return result;
//    }

    // TODO where post.isPublished.isTrue 전부 사용하도록 바꿔야됨
    private List<Long> findPostIds(KeywordSearch request) {
        return jpaQueryFactory.select(post.id)
                .from(post)
                .where(post.board.university.eq(request.getUniversity()))
                .where(contentOrTitleEq(request.getKeyword()))
                .where(postIsActive())
                .orderBy(post.id.desc())
                .offset(request.loadOffset())
                .limit(request.getSize())
                .fetch();
    }


    private List<SearchedPost> searchPostsByIds(List<Long> ids) {
        return jpaQueryFactory.select(new QSearchedPost(
                        post.id, post.title, post.likes.size(), post.viewCount
                        , post.createdAt, post.thumbnailUrl, post.content))
                .from(post)
                .where(idsEqual(ids))
                .orderBy(post.id.desc())
                .fetch();
    }

    private void setCommentCount(List<Long> ids, List<BoardPostDTO> result) {

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

    private int findCommentCounts(Map<Long, List<CommentCount>> map, BoardPostDTO post) {
        return map.getOrDefault(post.getPostId(), emptyList()).size();
    }

    private long getOffset(int page) {
        return (page - 1) * 20L;
    }
}
