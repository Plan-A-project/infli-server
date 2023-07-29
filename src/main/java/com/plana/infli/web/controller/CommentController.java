package com.plana.infli.web.controller;

import static org.springframework.http.ResponseEntity.*;

import com.plana.infli.domain.Comment;
import com.plana.infli.service.CommentService;
import com.plana.infli.web.dto.request.comment.create.controller.CreateCommentRequest;
import com.plana.infli.web.dto.request.comment.delete.controller.DeleteCommentRequest;
import com.plana.infli.web.dto.request.comment.edit.controller.EditCommentRequest;
import com.plana.infli.web.dto.request.comment.view.post.controller.LoadCommentsInPostRequest;
import com.plana.infli.web.dto.response.comment.create.CreateCommentResponse;
import com.plana.infli.web.dto.response.comment.view.BestCommentResponse;
import com.plana.infli.web.dto.response.comment.view.mycomment.MyCommentsResponse;
import com.plana.infli.web.dto.response.comment.view.post.PostCommentsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
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


    @PostMapping("/comments")
    @Operation(summary = "댓글 또는 대댓글 생성")
    @ApiResponse(responseCode = "200", description = "댓글 생성 완료",
            content = @Content(schema = @Schema(implementation = Comment.class)))
    @ApiResponse(responseCode = "400", description = "요청값이 잘못된 상태")
    @ApiResponse(responseCode = "404", description = "사용자 또는 댓글과 글을 찾을수 없음")
    @ApiResponse(responseCode = "403", description = "미인증 회원인 경우 댓글 작성 권한 없음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public CreateCommentResponse write(@Validated @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal String email) {

        return commentService.createComment(request.toServiceRequest(email));
    }

    @PatchMapping("/comments")
    @Operation(summary = "댓글 또는 대댓글 내용 수정")
    @ApiResponse(responseCode = "200", description = "댓글 수정 완료")
    @ApiResponse(responseCode = "400", description = "요청값이 잘못된 상태")
    @ApiResponse(responseCode = "404", description = "사용자 또는 댓글과 글을 찾을수 없음")
    @ApiResponse(responseCode = "403", description = "타인의 댓글 수정할 수 없음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public void edit(@Validated @RequestBody EditCommentRequest request,
            @AuthenticationPrincipal String email) {

        commentService.editContent(request.toServiceRequest(email));
    }

    @DeleteMapping("/comments")
    @Operation(summary = "댓글 또는 대댓글 삭제")
    @ApiResponse(responseCode = "200", description = "댓글 삭제 완료")
    @ApiResponse(responseCode = "400", description = "요청값이 잘못된 상태")
    @ApiResponse(responseCode = "404", description = "사용자 또는 댓글과 글을 찾을수 없음")
    @ApiResponse(responseCode = "403", description = "타인의 댓글 삭제할 수 없음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public void delete(@Validated @RequestBody DeleteCommentRequest request,
            @AuthenticationPrincipal String email) {

        commentService.delete(request.toServiceRequest(email));
    }

    @GetMapping("/posts/comments")
    @Operation(summary = "특정 글에 작성된 댓글과 대댓글 목록 조회")
    @ApiResponse(responseCode = "200", description = "정상적으로 조회 완료")
    @ApiResponse(responseCode = "400", description = "요청값이 잘못된 상태")
    @ApiResponse(responseCode = "404", description = "사용자 또는 댓글과 글을 찾을수 없음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public PostCommentsResponse loadCommentsInPost(@Validated LoadCommentsInPostRequest request,
            @AuthenticationPrincipal String email) {

        return commentService.loadCommentsInPost(request.toServiceRequest(email));
    }

    @GetMapping("/posts/{postId}/comments/best")
    @Operation(summary = "특정 글에 작성된 댓글들중 베스트 댓글 조회")
    @ApiResponse(responseCode = "200", description = "정상적으로 조회 완료")
    @ApiResponse(responseCode = "400", description = "요청값이 잘못된 상태")
    @ApiResponse(responseCode = "404", description = "사용자 또는 댓글과 글을 찾을수 없음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public BestCommentResponse loadBestCommentInPost(@PathVariable Long postId,
            @AuthenticationPrincipal String email) {

        return commentService.loadBestCommentInPost(postId, email);
    }

    @GetMapping("/posts/{postId}/comments/count")
    @Operation(summary = "특정 글에 작성된 총 댓글과 대댓글 갯수 조회")
    @ApiResponse(responseCode = "200", description = "정상적으로 조회 완료")
    @ApiResponse(responseCode = "400", description = "요청값이 잘못된 상태")
    @ApiResponse(responseCode = "404", description = "사용자 또는 댓글과 글을 찾을수 없음")
    public Long findCommentCountInPost(@PathVariable Long postId) {
        return commentService.findActiveCommentsCountInPost(postId);
    }

    @GetMapping("/members/comments")
    @Operation(summary = "내가 작성한 댓글 목록 조회")
    @ApiResponse(responseCode = "200", description = "정상적으로 조회 완료")
    @ApiResponse(responseCode = "400", description = "요청값이 잘못된 상태")
    @ApiResponse(responseCode = "404", description = "사용자 또는 댓글과 글을 찾을수 없음")
    public MyCommentsResponse loadMyComments(Integer page,
            @AuthenticationPrincipal String email) {
        return commentService.loadMyComments(page, email);
    }

    @GetMapping("/members/comments/count")
    @Operation(summary = "내가 총 작성한 댓글 갯수 조회")
    @ApiResponse(responseCode = "200", description = "정상적으로 조회 완료")
    @ApiResponse(responseCode = "400", description = "요청값이 잘못된 상태")
    @ApiResponse(responseCode = "404", description = "사용자 또는 댓글과 글을 찾을수 없음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public Long loadMyCommentsCount(
            @AuthenticationPrincipal String email) {

        return commentService.findCommentsCountByMember(email);
    }
}
