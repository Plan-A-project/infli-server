package com.plana.infli.web.controller;

import com.plana.infli.domain.PostType;
import com.plana.infli.service.PostService;
import com.plana.infli.web.dto.request.post.GatherPostCreateRq;
import com.plana.infli.web.dto.request.post.PostCreateRq;
import jakarta.servlet.ServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostService postService;

    @GetMapping("/first-post")
    public ResponseEntity isFirstPost(ServletRequest request) {
        return postService.isFistPost(request);
    }

    @PostMapping("/{boardId}/{postType}")
    public ResponseEntity initPost(@PathVariable Long boardId, @PathVariable String postType, ServletRequest request) {
        return postService.initPost(boardId, postType, request);
    }

    @PostMapping("/{boardId}/normal/{postId}")
    public ResponseEntity createNormalPost(@PathVariable Long boardId, @PathVariable Long postId, ServletRequest request,
                                           @RequestBody PostCreateRq requestDto) {
        return postService.createNormalPost(boardId, postId, request, requestDto);
    }

    @PostMapping("/{boardId}/gather/{postId}")
    public ResponseEntity createGatherPost(@PathVariable Long boardId, @PathVariable Long postId, ServletRequest request,
                                           @RequestBody GatherPostCreateRq requestDto) {
        return postService.createGatherPost(boardId, postId, request, requestDto);
    }

    @PostMapping("/{boardId}/notice/{postId}")
    public ResponseEntity createNoticePost(@PathVariable Long boardId, @PathVariable Long postId, ServletRequest request,
                                           @RequestBody PostCreateRq requestDto) {
        return postService.createNoticePost(boardId, postId, request, requestDto);
    }

    @DeleteMapping("/{boardId}/{postId}")
    public ResponseEntity deletePost(@PathVariable Long boardId, @PathVariable Long postId, ServletRequest request) {
        return postService.deletePost(boardId, postId, request);
    }
}
