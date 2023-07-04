package com.plana.infli.web.controller;

import static org.springframework.http.ResponseEntity.*;

import com.plana.infli.service.CommentService;
import com.plana.infli.web.dto.request.comment.create.controller.CreateCommentRequest;
import com.plana.infli.web.dto.request.comment.delete.controller.DeleteCommentRequest;
import com.plana.infli.web.dto.request.comment.edit.controller.EditCommentRequest;
import com.plana.infli.web.dto.request.comment.view.post.controller.LoadCommentsInPostRequest;
import com.plana.infli.web.dto.response.comment.create.CreateCommentResponse;
import com.plana.infli.web.dto.response.comment.edit.EditCommentResponse;
import com.plana.infli.web.dto.response.comment.view.BestCommentResponse;
import com.plana.infli.web.dto.response.comment.view.mycomment.MyCommentsResponse;
import com.plana.infli.web.dto.response.comment.view.post.PostCommentsResponse;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;


    //TODO role 에 따라 댓글 작성 권한
    @PostMapping("/comments")
    public ResponseEntity<CreateCommentResponse> write(
            @Validated @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal String email) {

        return ok(commentService.createComment(request.toServiceRequest(), email));
    }

    @PatchMapping("/comments")
    public ResponseEntity<EditCommentResponse> edit(
            @Validated @RequestBody EditCommentRequest request,
            @AuthenticationPrincipal String email) {

        return ok(commentService.editContent(request.toServiceRequest(), email));
    }

    @DeleteMapping("/comments")
    public ResponseEntity<Void> delete(
            @Validated @RequestBody DeleteCommentRequest request,
            @AuthenticationPrincipal String email) {

        commentService.delete(request.toServiceRequest(), email);
        return ok().build();
    }


    @GetMapping("/posts/comments")
    public ResponseEntity<PostCommentsResponse> loadCommentsInPost(
            @Validated LoadCommentsInPostRequest request,
            @AuthenticationPrincipal String email) {

        return ok(commentService.loadCommentsInPost(
                request.toServiceRequest(), email));
    }

    @GetMapping("/posts/{postId}/comments/best")
    public ResponseEntity<BestCommentResponse> loadBestCommentInPost(@PathVariable Long postId) {

        @Nullable BestCommentResponse response = commentService.loadBestCommentInPost(postId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{postId}/comments/count")
    public ResponseEntity<Long> findCommentCountInPost(@PathVariable Long postId) {
        return ok(commentService.findActiveCommentsCountInPost(postId));
    }

    @GetMapping("/members/comments")
    public ResponseEntity<MyCommentsResponse> loadMyComments(@RequestParam Integer page,
            @AuthenticationPrincipal String email) {
        return ok(commentService.loadMyComments(page, email));
    }

    @GetMapping("/members/comments/count")
    public ResponseEntity<Long> loadMyCommentsCount(
            @AuthenticationPrincipal String email) {

        return ok(commentService.findCommentsCountByMember(email));
    }
}
