package com.plana.infli.service;

import static com.plana.infli.domain.Comment.*;
import static com.plana.infli.domain.editor.CommentEditor.editComment;
import static com.plana.infli.web.dto.response.comment.postcomment.PostCommentsResponse.createPostCommentsResponse;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.web.dto.request.comment.CreateCommentRequest;
import com.plana.infli.web.dto.request.comment.DeleteCommentRequest;
import com.plana.infli.web.dto.request.comment.EditCommentRequest;
import com.plana.infli.web.dto.request.comment.SearchCommentsInPostRequest;
import com.plana.infli.web.dto.response.comment.postcomment.PostComment;
import com.plana.infli.web.dto.response.comment.postcomment.PostCommentsResponse;
import java.util.List;
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
    public void createComment(CreateCommentRequest request) {

        Member member = memberUtil.getContextMember();
        Post post = postRepository.findPostById(request.getPostId());

        Long parentCommentId = request.getParentCommentId();
        Comment parentComment = parentCommentId != null ?
                commentRepository.findCommentById(parentCommentId) : null;

        Comment newComment = create(post, request.getContent(), member, parentComment);

        commentRepository.save(newComment);
    }

    @Transactional
    public void edit(EditCommentRequest request) {
        Comment comment = commentRepository.findCommentById(request.getCommentId());
        editComment(comment, request);
    }

    @Transactional
    public void delete(DeleteCommentRequest request) {
        List<Long> ids = request.getIds();
        commentRepository.bulkDeleteByIds(ids);
    }

    public PostCommentsResponse searchCommentsInPost(SearchCommentsInPostRequest request) {
        List<PostComment> postComments = commentRepository.findCommentsInPostBy(request.getId(),
                request.getPage());

        Long commentCount = commentRepository.findCommentCountInPostBy(request.getId());

        return createPostCommentsResponse(request, postComments, commentCount);
    }
}
