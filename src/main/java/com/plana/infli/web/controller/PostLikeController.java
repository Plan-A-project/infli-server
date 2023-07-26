package com.plana.infli.web.controller;

import com.plana.infli.service.PostLikeService;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/posts/likes/{postId}")
    public ResponseEntity<String> createPostLike(@PathVariable Long postId,
            @AuthenticatedPrincipal String email) {
        postLikeService.createPostLike(postId, email);
        return ResponseEntity.ok("좋아요 생성 완료");
    }

    @DeleteMapping("/posts/likes/{postId}")
    public ResponseEntity<String> cancelPostLike(@PathVariable Long postId,
            @AuthenticatedPrincipal String email) {

        //TODO 동시성 고려 필요
        postLikeService.cancelPostLike(postId, email);
        return ResponseEntity.ok("좋아요 취소 완료");
    }
}
