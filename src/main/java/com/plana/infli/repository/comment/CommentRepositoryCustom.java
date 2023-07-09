package com.plana.infli.repository.comment;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.web.dto.response.comment.view.mycomment.MyComment;
import com.plana.infli.web.dto.response.comment.view.BestCommentResponse;
import com.plana.infli.web.dto.response.comment.view.post.PostComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;

public interface CommentRepositoryCustom {


    Long findActiveCommentsCountIn(Post post);

    void deleteAllByIdsInBatch(List<Long> ids);

    Optional<Comment> findActiveCommentWithMemberAndPostBy(Long commentId);


    List<Comment> findActiveCommentWithMemberByIdsIn(List<Long> ids);

    Integer findIdentifierNumberBy(Post post, Member member);

    Integer findLatestIdentifierNumberBy(Post post);


    List<PostComment> findCommentsInPost(Post post, Member member,
            PageRequest pageRequest);

    // 테스트 케이스용 method
    Long findAllActiveCommentCount();


    BestCommentResponse findBestCommentIn(Post post);

    List<MyComment> findMyComments(Member member, PageRequest pageRequest);

    Long findActiveCommentsCountBy(Member member);

    List<Comment> findAllOrderByIdAsc();
}
