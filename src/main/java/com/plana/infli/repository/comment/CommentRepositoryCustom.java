package com.plana.infli.repository.comment;

import com.plana.infli.web.dto.response.comment.postcomment.PostComment;
import java.util.List;

public interface CommentRepositoryCustom {

    void bulkDeleteByIds(List<Long> ids);

    List<PostComment> findCommentsInPostBy(Long id, Integer page);

    Long findCommentCountInPostBy(Long postId);

}
