package com.plana.infli.web.controller;

import com.plana.infli.service.CommentLikeService;
import com.plana.infli.web.dto.request.commentlike.cancel.controller.CancelCommentLikeRequest;
import com.plana.infli.web.dto.request.commentlike.create.controller.CreateCommentLikeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping("/commentLike")
    public void createCommentLike(@RequestBody @Validated CreateCommentLikeRequest request,
            @AuthenticationPrincipal String email) {

        commentLikeService.createCommentLike(request.toServiceRequest(), email);
    }

    @DeleteMapping("/commentLike")
    public void cancelCommentLike(@RequestBody @Validated CancelCommentLikeRequest request,
            @AuthenticationPrincipal String email) {

        commentLikeService.cancelCommentLike(request.toServiceRequest(), email);
    }
}
