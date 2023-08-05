package com.plana.infli.factory;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.service.CommentService;
import com.plana.infli.web.dto.request.comment.create.CreateCommentServiceRequest;
import com.plana.infli.web.dto.response.comment.create.CreateCommentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentFactory {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    public Comment createComment(Member member, Post post) {

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .parentCommentId(null)
                .postId(post.getId())
                .content("내용입니다")
                .build();

        CreateCommentResponse response = commentService.createComment(request);

        return commentRepository.findById(response.getCommentId()).get();
    }

    public Comment createChildComment(Member member, Post post, Comment parentComment) {

        CreateCommentServiceRequest request = CreateCommentServiceRequest.builder()
                .email(member.getEmail())
                .parentCommentId(parentComment.getId())
                .postId(post.getId())
                .content("내용입니다")
                .build();

        CreateCommentResponse response = commentService.createComment(request);

        return commentRepository.findById(response.getCommentId()).get();
    }
}
