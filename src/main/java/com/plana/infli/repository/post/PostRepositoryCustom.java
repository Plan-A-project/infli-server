package com.plana.infli.repository.post;

import com.plana.infli.domain.Post;
import com.plana.infli.domain.University;
import com.plana.infli.service.PostService.KeywordSearch;
import com.plana.infli.web.dto.response.post.search.SearchedPost;
import java.util.List;
import java.util.Optional;

public interface PostRepositoryCustom {

    Optional<Post> findActivePostBy(Long id);


    Optional<Post> findActivePostWithBoardBy(Long id);

    Optional<Post> findPessimisticLockActivePostWithBoardAndMemberBy(Long id);

    List<SearchedPost> searchPostByKeyWord(KeywordSearch keywordSearch);
}
