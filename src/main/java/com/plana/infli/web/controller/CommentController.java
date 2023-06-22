package com.plana.infli.web.controller;

import com.plana.infli.service.CommentService;
import com.plana.infli.service.validator.comment.CreateCommentValidator;
import com.plana.infli.service.validator.comment.DeleteCommentValidator;
import com.plana.infli.service.validator.comment.EditCommentValidator;
import com.plana.infli.web.dto.request.comment.CreateCommentRequest;
import com.plana.infli.web.dto.request.comment.DeleteCommentRequest;
import com.plana.infli.web.dto.request.comment.EditCommentRequest;
import com.plana.infli.web.dto.request.comment.SearchCommentsInPostRequest;
import com.plana.infli.web.dto.response.comment.postcomment.PostCommentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private final CreateCommentValidator createCommentValidator;

    private final EditCommentValidator editCommentValidator;

    private final DeleteCommentValidator deleteCommentValidator;

    @InitBinder("createCommentRequest")
    public void createCommentBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(createCommentValidator);
    }

    @InitBinder("editCommentRequest")
    public void editCommentBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(editCommentValidator);
    }

    @InitBinder("deleteCommentRequest")
    public void deleteCommentBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(deleteCommentValidator);
    }

    @PostMapping("/api/comments")
    public ResponseEntity<Void> write(@Validated @RequestBody CreateCommentRequest request) {
        commentService.createComment(request);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/comments")
    public ResponseEntity<Void> edit(@Validated @RequestBody EditCommentRequest request) {

        commentService.edit(request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/comments")
    public ResponseEntity<Void> delete(@Validated @RequestBody DeleteCommentRequest request) {
        commentService.delete(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/comments")
    public ResponseEntity<PostCommentsResponse> searchCommentsInPost(@Validated
    SearchCommentsInPostRequest request) {

        PostCommentsResponse response = commentService.searchCommentsInPost(request);

        return ResponseEntity.ok(response);
    }

}
