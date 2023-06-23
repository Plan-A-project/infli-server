package com.plana.infli.repository.post;

import com.plana.infli.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Post findPostById(Long id);
}
