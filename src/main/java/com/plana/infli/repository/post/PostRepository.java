package com.plana.infli.repository.post;

import com.plana.infli.domain.Post;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    Optional<Post> findPostById(Long id);

    List<Post> findAllByMemberId(Long memberId);
}
