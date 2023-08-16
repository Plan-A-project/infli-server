package com.plana.infli.repository.comment;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.web.dto.request.comment.view.CommentQueryRequest;
import com.plana.infli.web.dto.response.comment.view.mycomment.MyComment;
import com.plana.infli.web.dto.response.comment.view.BestCommentResponse;
import com.plana.infli.web.dto.response.comment.view.post.PostComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;

public interface CommentRepositoryCustom {

    Long findActiveCommentsCountIn(Post post);

    Optional<Comment> findActiveCommentWithMemberAndPostBy(Long commentId);

    Integer findIdentifierNumberBy(Post post, Member member);

    List<PostComment> findCommentsInPost(CommentQueryRequest request);

    // 테스트 케이스용 method
    Long findAllActiveCommentCount();

    BestCommentResponse findBestCommentIn(Post post);

    List<MyComment> findMyComments(CommentQueryRequest request);

    Long findActiveCommentsCountBy(Member member);

    Optional<Comment> findActiveCommentWithPostBy(Long commentId);

    Optional<Comment> findActiveCommentWithMemberBy(Long commentId);

}
