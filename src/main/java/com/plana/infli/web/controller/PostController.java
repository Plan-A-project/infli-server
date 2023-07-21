package com.plana.infli.web.controller;

import com.plana.infli.service.PostService;
import com.plana.infli.web.dto.request.post.GatherPostCreateRq;
import com.plana.infli.web.dto.request.post.PostCreateRq;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostService postService;

    @GetMapping("/first-post")
    @Operation(summary = "글 작성 최초 여부 검사")
   public ResponseEntity isFirstPost(@AuthenticatedPrincipal String email) {
        return postService.isFistPost(email);
    }

    @PostMapping("/board/{boardId}/type/{postType}")
    @Operation(summary = "글 작성 시작")
    public ResponseEntity initPost(@PathVariable Long boardId, @PathVariable String postType, @AuthenticatedPrincipal String email) {
        return postService.initPost(boardId, postType, email);
    }

    @PostMapping("/board/{boardId}/normal/{postId}")
    @Operation(summary = "글 작성 완료(일반글)")
    public ResponseEntity createNormalPost(@PathVariable Long boardId, @PathVariable Long postId, @AuthenticatedPrincipal String email,
                                           @RequestBody PostCreateRq requestDto) {
        return postService.createNormalPost(boardId, postId, email, requestDto);
    }

    @PostMapping("/board/{boardId}/gather/{postId}")
    @Operation(summary = "글 작성 완료(모집글)")
    public ResponseEntity createGatherPost(@PathVariable Long boardId, @PathVariable Long postId, @AuthenticatedPrincipal String email,
                                           @RequestBody GatherPostCreateRq requestDto) {
        return postService.createGatherPost(boardId, postId, email, requestDto);
    }

    @PostMapping("/board/{boardId}/notice/{postId}")
    @Operation(summary = "글 작성 완료(공지글)")
    public ResponseEntity createNoticePost(@PathVariable Long boardId, @PathVariable Long postId, @AuthenticatedPrincipal String email,
                                           @RequestBody PostCreateRq requestDto) {
        return postService.createNoticePost(boardId, postId, email, requestDto);
    }

    @DeleteMapping("/board/{boardId}/post/{postId}")
    @Operation(summary = "내가 쓴 글 삭제")
    public ResponseEntity deletePost(@PathVariable Long boardId, @PathVariable Long postId, @AuthenticatedPrincipal String email) {
        return postService.deletePost(boardId, postId, email);
    }

    @GetMapping("/board/{boardId}/post/{postId}")
    @Operation(summary = "글 조회")
    public ResponseEntity findPost(@PathVariable Long boardId, @PathVariable Long postId, @AuthenticatedPrincipal String email) {
        return postService.findPost(boardId, postId, email);
    }

    @GetMapping("/member/profile/my-post")
    @Operation(summary = "내가 쓴 글 조회")
    public ResponseEntity findMyPost(@AuthenticatedPrincipal String email) {
        return postService.findMyPost(email);
    }

    @GetMapping("/board/{boardId}/posts")
    @Operation(summary = "게시판 글 목록 조회")
    public ResponseEntity findPostList(@PathVariable Long boardId) {
        return postService.findPostList(boardId);
    }
}
