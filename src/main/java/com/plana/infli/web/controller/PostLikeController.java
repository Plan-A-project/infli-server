package com.plana.infli.web.controller;

import com.plana.infli.service.PostLikeService;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/post/{postId}/post-like")
    public ResponseEntity<Long> createPostLike(@PathVariable Long postId, @AuthenticatedPrincipal String email) {
        return postLikeService.createPostLike(postId, email);
    }

    @DeleteMapping("/post/{postId}/post-like")
    public ResponseEntity<Void> deletePostLike(@PathVariable Long postId, @AuthenticatedPrincipal String email) {
        return postLikeService.deletePostLike(postId, email);
    }
}
