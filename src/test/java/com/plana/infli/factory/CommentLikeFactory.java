package com.plana.infli.factory;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.CommentLike;
import com.plana.infli.domain.Member;
import com.plana.infli.repository.commentlike.CommentLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CommentLikeFactory {

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    public CommentLike createCommentLike(Member member, Comment comment) {

        return commentLikeRepository.save(CommentLike.create(comment, member));
    }

}
