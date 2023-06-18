package com.plana.infli.repository.comment;

import static com.plana.infli.domain.QComment.*;

import com.plana.infli.domain.QComment;
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
}
