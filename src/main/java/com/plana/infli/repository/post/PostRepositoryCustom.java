package com.plana.infli.repository.post;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.service.PostService.KeywordSearch;
import com.plana.infli.web.dto.request.post.view.board.LoadPostsByBoardServiceRequest;
import com.plana.infli.web.dto.response.post.BoardPostDTO;
import com.plana.infli.web.dto.response.post.my.MyPost;
import com.plana.infli.web.dto.response.post.search.SearchedPost;
import com.plana.infli.web.dto.response.post.single.SinglePostResponse;
import java.util.List;
import java.util.Optional;

public interface PostRepositoryCustom {

    Optional<Post> findActivePostBy(Long id);

    // 글 수정기능에서만 쓰이는 메서드
    // 일반적인 글 조회 상황에서는 findActivePostBy(Long id) 메서드 사용하면 됨
    Optional<Post> findNotDeletedPostWithMemberBy(Long id);

    Optional<Post> findActivePostWithBoardBy(Long id);

    Optional<Post> findPessimisticLockActivePostWithBoardAndMemberBy(Long id);

//    List<SearchedPost> searchPostByKeyWord(KeywordSearch keywordSearch);

    Optional<Post> findActivePostWithMemberBy(Long id);

    SinglePostResponse loadSinglePostResponse(Post post, Member member);

    List<MyPost> loadMyPosts(Member member, int intPage);

//    List<NormalPost> findNormalPostsByBoard(Board board, int page, PostViewOrder viewOrder);
//
//    List<AnnouncementPost> loadAnnouncementPostsByBoard(Board board, int page, PostViewOrder viewOrder);

    List<BoardPostDTO> loadPostsByBoard(Board board, LoadPostsByBoardServiceRequest request);
}
