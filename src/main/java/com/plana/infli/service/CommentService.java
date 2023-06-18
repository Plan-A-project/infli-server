package com.plana.infli.service;

import static com.plana.infli.domain.editor.CommentEditor.editComment;
import static com.plana.infli.web.dto.request.comment.CreateCommentRequest.createComment;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.web.dto.request.comment.CreateCommentRequest;
import com.plana.infli.web.dto.request.comment.EditCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final PostRepository postRepository;

    private final MemberUtil memberUtil;

    @Transactional
    public void saveNewComment(CreateCommentRequest request) {
        commentRepository.save(createNewComment(request));
    }

    private Comment createNewComment(CreateCommentRequest request) {

        Member member = memberUtil.getContextMember();
        Post post = postRepository.findPostById(request.getPostId());

        Long parentCommentId = request.getParentCommentId();

        Comment parentComment = parentCommentId != null ?
                commentRepository.findCommentById(parentCommentId) : null;

       return createComment(post, member, parentComment, request.getContent());
    }

    @Transactional
    public void edit(EditCommentRequest request) {
        Comment comment = commentRepository.findCommentById(request.getCommentId());
        editComment(comment, request);
    }
}
