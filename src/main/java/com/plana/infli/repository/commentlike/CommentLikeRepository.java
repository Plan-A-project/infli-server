package com.plana.infli.repository.commentlike;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.CommentLike;
import com.plana.infli.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long>, CommentLikeRepositoryCustom {

    boolean existsByMemberAndComment(Member member, Comment comment);

//    Optional<CommentLike> findByCommentAndMember(Comment comment, Member member);

    @EntityGraph(attributePaths = {"comment", "member"})
    Optional<CommentLike> findWithCommentAndMemberById(Long id);
}












