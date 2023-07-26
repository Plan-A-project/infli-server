package com.plana.infli.service;

import static com.plana.infli.domain.CommentLike.*;
import static com.plana.infli.exception.custom.BadRequestException.COMMENT_LIKE_NOT_FOUND;
import static com.plana.infli.exception.custom.ConflictException.ALREADY_PRESSED_LIKE_ON_THIS_COMMENT;
import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.CommentLike;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.exception.custom.AuthenticationFailedException;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.ConflictException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.commentlike.CommentLikeRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.commentlike.cancel.service.CancelCommentLikeServiceRequest;
import com.plana.infli.web.dto.request.commentlike.create.service.CreateCommentLikeServiceRequest;
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
    public Long createCommentLike(CreateCommentLikeServiceRequest request, String email) {

        checkIsLoggedIn(email);

        // 좋아요를 누를 회원이 존재하지 않거나 삭제된 경우 예외가 발생된다
        Member member = memberRepository.findActiveMemberBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        // 좋아요를 누를 댓글이 작성된 글이 존재하지 않거나 삭제된 경우 예외가 발생된다
        Post post = postRepository.findActivePostBy(request.getPostId())
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));

        // 좋아요를 누를 댓글이 존재하지 않거나 삭제된 경우 예외가 발생된다
        Comment comment = commentRepository.findActiveCommentWithMemberAndPostBy(
                        request.getCommentId())
                .orElseThrow(() -> new NotFoundException(COMMENT_NOT_FOUND));

        // 좋아요 생성 요청에 대한 검증 진행
        validateCreateCommentLikeRequest(post, comment, member);

        CommentLike savedCommentLike = commentLikeRepository.save(create(comment, member));

        return savedCommentLike.getId();
    }

    private void checkIsLoggedIn(String email) {
        if (email == null) {
            throw new AuthenticationFailedException();
        }
    }

    private void validateCreateCommentLikeRequest(Post post, Comment comment,
            Member member) {

        // 클라이언트가 전송한 글 ID 번호와, 좋아요를 누를 댓글의 글 번호는 일치해야 한다
        if (comment.getPost().equals(post) == false) {
            throw new NotFoundException(COMMENT_NOT_FOUND);
        }

        if (universityRepository.isMemberAndPostInSameUniversity(member, post) == false) {
            throw new AuthorizationFailedException();
        }
        // 이미 해당 댓글에 회원이 좋아요를 누른경우 예외가 발생한다
        if (commentLikeRepository.existsByMemberAndComment(member, comment)) {
            throw new ConflictException(ALREADY_PRESSED_LIKE_ON_THIS_COMMENT);
        }
    }

    @Transactional
    public void cancelCommentLike(CancelCommentLikeServiceRequest request, String email) {

        checkIsLoggedIn(email);

        // 좋아요를 취소를 할 회원이 존재하지 않거나 삭제된 경우 예외가 발생된다
        Member member = memberRepository.findActiveMemberBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        // 좋아요를 누른 댓글이 작성된 글이 존재하지 않거나 삭제된 경우 예외가 발생된다
        Post post = postRepository.findActivePostBy(request.getPostId())
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));

        // 좋아요 취소를 할 댓글이 존재하지 않거나 삭제된 경우 예외가 발생된다
        Comment comment = commentRepository.findActiveCommentWithMemberAndPostBy(
                        request.getCommentId())
                .orElseThrow(() -> new NotFoundException(COMMENT_NOT_FOUND));

        // 좋아요 취소 요청에 대한 검증 진행

        // 클라이언트가 전송한 글 ID 번호와, 좋아요 취소할 댓글의 글 번호는 일치해야 한다
        if (comment.getPost().equals(post) == false) {
            throw new NotFoundException(COMMENT_NOT_FOUND);
        }

        // 좋아요를 누르지 않은 댓글에 좋아요 취소 요청을 할수 없다
        CommentLike commentLike = commentLikeRepository.findByCommentAndMember(comment, member)
                .orElseThrow(() -> new BadRequestException(COMMENT_LIKE_NOT_FOUND));

        commentLikeRepository.delete(commentLike);
    }
}
