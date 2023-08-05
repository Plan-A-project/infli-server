package com.plana.infli.web.controller;

import static org.springframework.http.ResponseEntity.*;

import com.plana.infli.service.CommentLikeService;
import com.plana.infli.web.dto.request.commentlike.cancel.CancelCommentLikeRequest;
import com.plana.infli.web.dto.request.commentlike.create.CreateCommentLikeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/comments/likes")
    @Operation(description = "특정 댓글에 좋아요 누르기")
    @ApiResponse(responseCode = "200", description = "정상적으로 좋아요 누르기 완료")
    @ApiResponse(responseCode = "404", description = "좋아요를 누를 사용자 또는 글이나 댓글이 존재하지 않음")
    @ApiResponse(responseCode = "403", description = "좋아요를 누를 댓글이 나의 대학 게시판에 작성된 댓글이 아닌 경우")
    @ApiResponse(responseCode = "409", description = "이미 해당 댓글에 좋아요를 눌렀음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public ResponseEntity<Long> createCommentLike(@RequestBody @Validated CreateCommentLikeRequest request,
            @AuthenticationPrincipal String email) {

        return ok(commentLikeService.createCommentLike(request.toServiceRequest(email)));
    }

    @DeleteMapping("/comments/likes")
    @ApiResponse(description = "좋아요 누른 댓글 좋아요 취소")
    @ApiResponse(responseCode = "200", description = "취소 완료")
    @ApiResponse(responseCode = "404", description = "좋아요 취소를 누를 사용자 또는 글이나 댓글이 존재하지 않음")
    @ApiResponse(responseCode = "400", description = "해당 댓글에 좋아요를 누른적이 없음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public void cancelCommentLike(@RequestBody @Validated CancelCommentLikeRequest request,
            @AuthenticationPrincipal String email) {

        commentLikeService.cancelCommentLike(request.toServiceRequest(email));

    }
}
