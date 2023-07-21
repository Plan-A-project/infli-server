package com.plana.infli.repository.post;

import static com.plana.infli.domain.QBoard.*;
import static com.plana.infli.domain.QComment.*;
import static com.plana.infli.domain.QPost.*;
import static java.util.Collections.*;
import static java.util.Optional.*;
import static java.util.stream.Collectors.groupingBy;

import com.plana.infli.domain.Post;
import com.plana.infli.service.PostService.KeywordSearch;
import com.plana.infli.web.dto.response.post.search.CommentCount;
import com.plana.infli.web.dto.response.post.search.QCommentCount;
import com.plana.infli.web.dto.response.post.search.QSearchedPost;
import com.plana.infli.web.dto.response.post.search.SearchedPost;
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
                .where(post.isDeleted.isFalse())
                .where(post.id.eq(id))
                .fetchOne());
    }


    @Override
    public Optional<Post> findActivePostWithBoardBy(Long id) {
        return ofNullable(jpaQueryFactory.selectFrom(post)
                .innerJoin(post.board, board).fetchJoin()
                .where(post.isDeleted.isFalse())
                .where(post.id.eq(id))
                .fetchOne());
    }

    @Override
    public Optional<Post> findPessimisticLockActivePostWithBoardAndMemberBy(Long id) {
        return ofNullable(jpaQueryFactory.selectFrom(post)
                .innerJoin(post.board, board).fetchJoin()
                .where(post.isDeleted.isFalse())
                .where(post.id.eq(id))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }


    @Override
    public List<SearchedPost> searchPostByKeyWord(KeywordSearch request) {

        List<Long> ids = findPostIds(request);

        List<SearchedPost> result = searchPostsByIds(ids);

        setTotalComments(ids, result);

        return result;
    }

    private List<Long> findPostIds(KeywordSearch request) {
        return jpaQueryFactory.select(post.id)
                .from(post)
                .where(post.board.university.eq(request.getUniversity()))
                .where(contentOrTitleEq(request.getKeyword()))
                .orderBy(post.id.desc())
                .offset(request.loadOffset())
                .limit(request.getSize())
                .fetch();
    }

    private BooleanExpression contentOrTitleEq(String keyword) {
        String[] keyWords = keyword.trim().split("\\s+");

        return Arrays.stream(keyWords)
                .map(word -> post.title.containsIgnoreCase(word)
                        .or(post.main.containsIgnoreCase(word)))
                .reduce(BooleanExpression::and)
                .orElse(null);
    }

    private List<SearchedPost> searchPostsByIds(List<Long> ids) {
        return jpaQueryFactory.select(new QSearchedPost(post.id, post.title, post.main,
                        post.createdAt, post.viewCount))
                .from(post)
                .where(post.id.in(ids))
                .orderBy(post.id.desc())
                .fetch();
    }

    private void setTotalComments(List<Long> ids, List<SearchedPost> result) {
        Map<Long, List<CommentCount>> map = findCommentCount(ids);

        result.forEach(post -> post.setCommentsCounts(findCommentCounts(map, post)));
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

    private int findCommentCounts(Map<Long, List<CommentCount>> map, SearchedPost post) {
        return map.getOrDefault(post.getId(), emptyList()).size();
    }




}
