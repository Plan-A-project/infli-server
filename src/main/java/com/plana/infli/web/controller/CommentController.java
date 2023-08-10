package com.plana.infli.web.controller;

import com.plana.infli.domain.Comment;
import com.plana.infli.service.CommentService;
import com.plana.infli.web.dto.request.comment.create.CreateCommentRequest;
import com.plana.infli.web.dto.request.comment.edit.EditCommentRequest;
import com.plana.infli.web.dto.request.comment.view.post.LoadCommentsInPostRequest;
import com.plana.infli.web.dto.response.comment.create.CreateCommentResponse;
import com.plana.infli.web.dto.response.comment.view.BestCommentResponse;
import com.plana.infli.web.dto.response.comment.view.mycomment.MyCommentsResponse;
import com.plana.infli.web.dto.response.comment.view.post.PostCommentsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;


    @PostMapping("/comments")
    @Operation(summary = "댓글 또는 대댓글 생성")
    public CreateCommentResponse write(@Validated @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal String username) {

        return commentService.createComment(request.toServiceRequest(username));
    }

    @PatchMapping("/comments")
    @Operation(summary = "댓글 또는 대댓글 내용 수정")
    public void edit(@AuthenticationPrincipal String username,
            @Validated @RequestBody EditCommentRequest request) {

        commentService.editCommentContent(request.toServiceRequest(username));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 또는 대댓글 삭제")
    public void delete(@AuthenticationPrincipal String username, @PathVariable Long commentId) {
        commentService.deleteComment(username, commentId);
    }

    @GetMapping("/posts/comments")
    @Operation(summary = "특정 글에 작성된 댓글과 대댓글 목록 조회")
    public PostCommentsResponse loadCommentsInPost(@AuthenticationPrincipal String username,
            @Validated LoadCommentsInPostRequest request) {

        return commentService.loadCommentsInPost(request.toServiceRequest(username));
    }

    @GetMapping("/posts/{postId}/comments/best")
    @Operation(summary = "특정 글에 작성된 댓글들중 베스트 댓글 조회")
    public BestCommentResponse loadBestCommentInPost(@PathVariable Long postId,
            @AuthenticationPrincipal String username) {
        return commentService.loadBestCommentInPost(postId, username);
    }

    @GetMapping("/posts/{postId}/comments/count")
    @Operation(summary = "특정 글에 작성된 총 댓글과 대댓글 갯수 조회")
    public Long findCommentCountInPost(@PathVariable Long postId) {
        return commentService.findActiveCommentsCountInPost(postId);
    }

    @GetMapping("/members/comments")
    @Operation(summary = "내가 작성한 댓글 목록 조회")
    public MyCommentsResponse loadMyComments(@AuthenticationPrincipal String username,
            Integer page) {

        return commentService.loadMyComments(page, username);
    }

    @GetMapping("/members/comments/count")
    @Operation(summary = "내가 총 작성한 댓글 갯수 조회")
    public Long loadMyCommentsCount(@AuthenticationPrincipal String username) {

        return commentService.findCommentsCountByMember(username);
    }
}
