package com.plana.infli.web.controller;

import static org.springframework.http.HttpStatus.*;

import com.plana.infli.service.CommentLikeService;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping("/comments/{commentId}/likes")
    @Operation(description = "특정 댓글에 좋아요 누르기")
    @ResponseStatus(CREATED)
    public void createCommentLike(@AuthenticatedPrincipal String username,
            @PathVariable Long commentId) {

        commentLikeService.createCommentLike(username, commentId);
    }

    @DeleteMapping("/comments/{commentId}/likes")
    @ApiResponse(description = "좋아요 누른 댓글 좋아요 취소")
    public void cancelCommentLike(@AuthenticatedPrincipal String username,
            @PathVariable Long commentId) {

        commentLikeService.cancelCommentLike(username, commentId);
    }
}
