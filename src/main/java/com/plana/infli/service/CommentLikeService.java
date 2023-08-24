package com.plana.infli.service;

import static com.plana.infli.domain.CommentLike.*;
import static com.plana.infli.infra.exception.custom.BadRequestException.COMMENT_LIKE_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.ConflictException.ALREADY_PRESSED_LIKE_ON_THIS_COMMENT;
import static com.plana.infli.infra.exception.custom.NotFoundException.COMMENT_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.MEMBER_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.POST_NOT_FOUND;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.CommentLike;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.infra.exception.custom.AuthorizationFailedException;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.commentlike.CommentLikeRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
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

    private final CommentRepository commentRepository;

    private final UniversityRepository universityRepository;

    @Transactional
    public void createCommentLike(String username, Long commentId) {

        Member member = findMemberBy(username);

        Comment comment = findCommentWithPostBy(commentId);

        validateCreateRequest(comment, member);

        commentLikeRepository.save(create(comment, member));
    }

    private Comment findCommentWithPostBy(Long commentId) {
        return commentRepository.findActiveCommentWithPostBy(commentId)
                .orElseThrow(() -> new NotFoundException(COMMENT_NOT_FOUND));
    }


    private Member findMemberBy(String username) {
        return memberRepository.findActiveMemberBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }


    private void validateCreateRequest(Comment comment, Member member) {

        Post post = comment.getPost();

        if (post.isDeleted()) {
            throw new NotFoundException(POST_NOT_FOUND);
        }

        if (universityRepository.isMemberAndPostInSameUniversity(member, post) == false) {
            throw new AuthorizationFailedException();
        }

        if (commentLikeRepository.existsByMemberAndComment(member, comment)) {
            throw new ConflictException(ALREADY_PRESSED_LIKE_ON_THIS_COMMENT);
        }
    }

    @Transactional
    public void cancelCommentLike(String username, Long commentId) {

        Member member = findMemberBy(username);

        Comment comment = findCommentWithPostBy(commentId);

        validateCancelCommentLikeRequest(comment);

        CommentLike commentLike = findCommentLikeBy(comment, member);

        commentLikeRepository.delete(commentLike);
    }

    private static void validateCancelCommentLikeRequest(Comment comment) {
        Post post = comment.getPost();

        if (post.isDeleted()) {
            throw new NotFoundException(POST_NOT_FOUND);
        }
    }

    private CommentLike findCommentLikeBy(Comment comment, Member member) {
        return commentLikeRepository.findByCommentAndMember(comment, member)
                .orElseThrow(() -> new BadRequestException(COMMENT_LIKE_NOT_FOUND));
    }

}
