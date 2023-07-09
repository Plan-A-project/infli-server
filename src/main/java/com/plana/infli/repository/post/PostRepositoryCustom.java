package com.plana.infli.repository.post;

import com.plana.infli.domain.Post;
import java.util.Optional;

public interface PostRepositoryCustom {

    Optional<Post> findActivePostBy(Long id);

    Optional<Post> findActivePostWithBoardAndMemberBy(Long id);

    Optional<Post> findActivePostWithBoardBy(Long id);

    Optional<Post> findPessimisticLockActivePostWithBoardAndMemberBy(Long id);

    Post findWithOptimisticLock(Post post);

}
