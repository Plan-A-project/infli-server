package com.plana.infli.web.controller;

import static com.plana.infli.web.dto.response.ApiResponse.*;

import com.plana.infli.service.PostLikeService;
import com.plana.infli.web.dto.response.ApiResponse;
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

    @PostMapping("/likes/posts/{postId}")
    public ApiResponse<Void> createPostLike(@AuthenticatedPrincipal String username,
            @PathVariable Long postId) {

        postLikeService.createPostLike(username, postId);
        return created();
    }

    @DeleteMapping("/likes/posts/{postId}")
    public ApiResponse<String> cancelPostLike(@AuthenticatedPrincipal String username,
            @PathVariable Long postId) {

        // TODO 동시성 고려 필요
        postLikeService.cancelPostLike(username, postId);
        return ApiResponse.ok("좋아요 취소 완료");
    }
}

