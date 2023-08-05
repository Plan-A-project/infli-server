package com.plana.infli.service;

import static com.plana.infli.domain.CommentLike.*;
import static com.plana.infli.exception.custom.BadRequestException.COMMENT_LIKE_NOT_FOUND;
import static com.plana.infli.exception.custom.ConflictException.ALREADY_PRESSED_LIKE_ON_THIS_COMMENT;
import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.CommentLike;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.ConflictException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.commentlike.CommentLikeRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.commentlike.cancel.CancelCommentLikeServiceRequest;
import com.plana.infli.web.dto.request.commentlike.create.CreateCommentLikeServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;

    private final MemberRepository memberRepository;

    private final PostRepository postRepository;

    private final CommentRepository commentRepository;

    private final UniversityRepository universityRepository;

    @Transactional
    public Long createCommentLike(CreateCommentLikeServiceRequest request) {

        Member member = findMemberBy(request.getEmail());

        Post post = findPostBy(request.getPostId());

        Comment comment = findCommentWithPostBy(request.getCommentId());

        validateCreateRequest(comment, post, member);

        return commentLikeRepository.save(create(comment, member)).getId();
    }

    private Comment findCommentWithPostBy(Long commentId) {
        return commentRepository.findActiveCommentWithPostBy(commentId)
                .orElseThrow(() -> new NotFoundException(COMMENT_NOT_FOUND));
    }

    private Post findPostBy(Long postId) {
        return postRepository.findActivePostBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
    }

    private Member findMemberBy(String email) {
        return memberRepository.findActiveMemberBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }


    private void validateCreateRequest(Comment comment, Post post, Member member) {

        checkCommentIsInThisPost(comment, post);

        if (universityRepository.isMemberAndPostInSameUniversity(member, post) == false) {
            throw new AuthorizationFailedException();
        }

        if (commentLikeRepository.existsByMemberAndComment(member, comment)) {
            throw new ConflictException(ALREADY_PRESSED_LIKE_ON_THIS_COMMENT);
        }
    }

    private void checkCommentIsInThisPost(Comment comment, Post post) {
        if (comment.getPost().equals(post) == false) {
            throw new NotFoundException(COMMENT_NOT_FOUND);
        }
    }

    @Transactional
    public void cancelCommentLike(CancelCommentLikeServiceRequest request) {

        Member member = findMemberBy(request.getEmail());

        Post post = findPostBy(request.getPostId());

        Comment comment = findCommentWithPostBy(request.getCommentId());

        checkCommentIsInThisPost(comment, post);

        CommentLike commentLike = findCommentLikeBy(comment, member);

        commentLikeRepository.delete(commentLike);
    }

    private CommentLike findCommentLikeBy(Comment comment, Member member) {
        return commentLikeRepository.findByCommentAndMember(comment, member)
                .orElseThrow(() -> new BadRequestException(COMMENT_LIKE_NOT_FOUND));
    }

}
