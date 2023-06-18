package com.plana.infli.repository.comment;

import com.plana.infli.domain.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {


    Comment findCommentById(Long id);

    @EntityGraph(attributePaths = {"post"})
    Comment findWithPostById(Long id);

    @EntityGraph(attributePaths = {"member, post"})
    Comment findWithMemberAndPostById(Long id);

    @EntityGraph(attributePaths = "{member}")
    Comment findWithMemberById(Long id);

}
