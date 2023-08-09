package com.plana.infli.repository.post;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    // 삭제된 글 포함하여 조회
    // 테스트 케이스용 메서드
    Optional<Post> findPostById(Long id);
}
