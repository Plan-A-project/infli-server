package com.plana.infli.web.controller;

import static org.springframework.http.HttpStatus.*;

import com.plana.infli.service.PostLikeService;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/posts/{postId}/likes")
    @ResponseStatus(CREATED)
    @Operation(summary = "글 좋아요 누르기")
    public void createPostLike(@AuthenticatedPrincipal String username,
            @PathVariable Long postId) {

        postLikeService.pressPostLike(username, postId);
    }

    @DeleteMapping("/posts/{postId}/likes")
    @Operation(summary = "좋아요 누른 글 좋아요 취소")
    public void cancelPostLike(@AuthenticatedPrincipal String username,
            @PathVariable Long postId) {

        postLikeService.cancelPostLike(username, postId);
    }
}

