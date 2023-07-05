package com.plana.infli.factory;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.service.CommentService;
import com.plana.infli.web.dto.request.comment.create.service.CreateCommentServiceRequest;
import com.plana.infli.web.dto.response.comment.create.CreateCommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentFactory {

    private final CommentService commentService;

    private final CommentRepository commentRepository;

    public Comment createComment(Member member, Post post) {

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .parentCommentId(null)
                .postId(post.getId())
                .content("내용입니다")
                .build();

        CreateCommentResponse response = commentService.createComment(request, member.getEmail());

        return commentRepository.findById(response.getCommentId()).get();
    }

    public Comment createChildComment(Member member, Post post, Comment parentComment) {

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .parentCommentId(parentComment.getId())
                .postId(post.getId())
                .content("내용입니다")
                .build();

        CreateCommentResponse response = commentService.createComment(request, member.getEmail());

        return commentRepository.findById(response.getCommentId()).get();
    }
}
