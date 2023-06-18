package com.plana.infli.web.controller;

import com.plana.infli.service.CommentService;
import com.plana.infli.service.validator.comment.CreateCommentValidator;
import com.plana.infli.service.validator.comment.EditCommentValidator;
import com.plana.infli.web.dto.request.comment.CreateCommentRequest;
import com.plana.infli.web.dto.request.comment.EditCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
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


    @InitBinder("createCommentRequest")
    public void createCommentBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(createCommentValidator);
    }

    @InitBinder("editCommentRequest")
    public void editCommentBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(editCommentValidator);
    }

    @PostMapping("/api/comments")

    public ResponseEntity<Void> saveNewComment(
            @Validated @RequestBody CreateCommentRequest request) {
        commentService.saveNewComment(request);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/comments")
    public ResponseEntity<Void> edit(@Validated @RequestBody EditCommentRequest request) {

        commentService.edit(request);

        return ResponseEntity.ok().build();
    }

}
