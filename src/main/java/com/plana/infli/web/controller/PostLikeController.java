package com.plana.infli.web.controller;

import static org.springframework.http.HttpStatus.*;

import com.plana.infli.service.PostLikeService;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
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
    public void createPostLike(@AuthenticatedPrincipal String username,
            @PathVariable Long postId) {

        postLikeService.pressPostLike(username, postId);
    }

    @DeleteMapping("/posts/{postId}/likes")
    public void cancelPostLike(@AuthenticatedPrincipal String username,
            @PathVariable Long postId) {

        // TODO 동시성 고려 필요
        postLikeService.cancelPostLike(username, postId);
    }
}

