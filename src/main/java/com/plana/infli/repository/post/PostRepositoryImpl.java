package com.plana.infli.repository.post;

import static com.plana.infli.domain.QBoard.*;
import static com.plana.infli.domain.QPost.*;
import static java.util.Optional.*;

import com.plana.infli.domain.Post;
import com.plana.infli.domain.QBoard;
import com.plana.infli.domain.QPost;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
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

    //TODO
    // board fetch join 필요성 확인
    @Override
    public Optional<Post> findActivePostWithBoardAndMemberBy(Long id) {
        return ofNullable(jpaQueryFactory.selectFrom(post)
                .innerJoin(post.board, board).fetchJoin()
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
    public Post findWithOptimisticLock(Post findPost) {
        return jpaQueryFactory.selectFrom(post)
                .where(post.eq(findPost))
                .fetchOne();
    }

}
