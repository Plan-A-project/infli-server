package com.plana.infli.repository.commentlike;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.CommentLike;
import com.plana.infli.domain.Member;
import java.util.Optional;

public interface CommentLikeRepositoryCustom {


    Optional<CommentLike> findByCommentAndMember(Comment comment, Member member);

}
