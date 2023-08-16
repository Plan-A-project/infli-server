package com.plana.infli.repository.post;

import com.plana.infli.domain.Post;
import com.plana.infli.web.dto.request.post.view.PostQueryRequest;
import com.plana.infli.web.dto.response.post.board.BoardPost;
import com.plana.infli.web.dto.response.post.my.MyPost;
import com.plana.infli.web.dto.response.post.search.SearchedPost;
import com.plana.infli.web.dto.response.post.single.SinglePostResponse;
import java.util.List;
import java.util.Optional;

public interface PostRepositoryCustom {

    Optional<Post> findActivePostBy(Long id);

    Optional<Post> findActivePostWithBoardBy(Long id);

    List<SearchedPost> searchPostByKeyWord(PostQueryRequest request);

    Optional<Post> findActivePostWithMemberBy(Long id);

    SinglePostResponse loadSinglePostResponse(PostQueryRequest request);

    List<MyPost> loadMyPosts(PostQueryRequest request);

    List<BoardPost> loadPostsByBoard(PostQueryRequest request);

    Optional<Post> findActivePostWithOptimisticLock(Long postId);

    //테스트 케이스용
    Optional<Post> findActivePostWithBoardAndMemberBy(Long id);
}
