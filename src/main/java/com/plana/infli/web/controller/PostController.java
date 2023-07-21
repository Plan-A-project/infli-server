package com.plana.infli.web.controller;

import com.plana.infli.service.PostService;
import com.plana.infli.web.dto.request.post.GatherPostCreateRq;
import com.plana.infli.web.dto.request.post.PostCreateRq;
import com.plana.infli.web.dto.request.post.search.controller.SearchPostsByKeywordRequest;
import com.plana.infli.web.dto.response.post.search.PostSearchResponse;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostService postService;

    @GetMapping("/first-post")
    public ResponseEntity isFirstPost(@AuthenticatedPrincipal String email) {
        return postService.isFistPost(email);
    }

    @PostMapping("/board/{boardId}/type/{postType}")
    public ResponseEntity initPost(@PathVariable Long boardId, @PathVariable String postType, @AuthenticatedPrincipal String email) {
        return postService.initPost(boardId, postType, email);
    }

    @PostMapping("/board/{boardId}/normal/{postId}")
    public ResponseEntity createNormalPost(@PathVariable Long boardId, @PathVariable Long postId, @AuthenticatedPrincipal String email,
                                           @RequestBody PostCreateRq requestDto) {
        return postService.createNormalPost(boardId, postId, email, requestDto);
    }

    @PostMapping("/board/{boardId}/gather/{postId}")
    public ResponseEntity createGatherPost(@PathVariable Long boardId, @PathVariable Long postId, @AuthenticatedPrincipal String email,
                                           @RequestBody GatherPostCreateRq requestDto) {
        return postService.createGatherPost(boardId, postId, email, requestDto);
    }

    @PostMapping("/board/{boardId}/notice/{postId}")
    public ResponseEntity createNoticePost(@PathVariable Long boardId, @PathVariable Long postId, @AuthenticatedPrincipal String email,
                                           @RequestBody PostCreateRq requestDto) {
        return postService.createNoticePost(boardId, postId, email, requestDto);
    }

    @DeleteMapping("/board/{boardId}/post/{postId}")
    public ResponseEntity deletePost(@PathVariable Long boardId, @PathVariable Long postId, @AuthenticatedPrincipal String email) {
        return postService.deletePost(boardId, postId, email);
    }

    @GetMapping("/board/{boardId}/post/{postId}")
    public ResponseEntity findPost(@PathVariable Long boardId, @PathVariable Long postId) {
        return postService.findPost(boardId, postId);
    }

    @GetMapping("/member/profile/my-post")
    public ResponseEntity findPost(@PathVariable Long boardId, @PathVariable Long postId, @AuthenticatedPrincipal String email) {
        return postService.findMyPost(boardId, postId, email);
    }

    @GetMapping("/posts/search")
    public ResponseEntity<PostSearchResponse> searchPosts(
            @Validated SearchPostsByKeywordRequest request, @AuthenticatedPrincipal String email) {

        return ResponseEntity.ok(postService.searchPosts(request.toServiceRequest(), email));
    }
}
