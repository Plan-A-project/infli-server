package com.plana.infli.service;

import static com.plana.infli.domain.Comment.*;
import static com.plana.infli.domain.Member.isAdmin;
import static com.plana.infli.domain.editor.CommentEditor.*;
import static com.plana.infli.domain.editor.PostEditor.increaseCommentMemberCount;
import static com.plana.infli.domain.type.VerificationStatus.*;
import static com.plana.infli.infra.exception.custom.BadRequestException.CHILD_COMMENTS_NOT_ALLOWED;
import static com.plana.infli.infra.exception.custom.BadRequestException.EDIT_COMMENT_IN_DELETED_POST_NOT_ALLOWED;
import static com.plana.infli.infra.exception.custom.BadRequestException.MAX_COMMENT_SIZE_EXCEEDED;
import static com.plana.infli.infra.exception.custom.NotFoundException.COMMENT_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.MEMBER_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.POST_NOT_FOUND;
import static com.plana.infli.web.dto.request.comment.view.CommentQueryRequest.*;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.infra.exception.custom.AuthorizationFailedException;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.aop.retry.Retry;
import com.plana.infli.web.dto.request.comment.create.CreateCommentServiceRequest;
import com.plana.infli.web.dto.request.comment.edit.EditCommentServiceRequest;
import com.plana.infli.web.dto.request.comment.view.CommentQueryRequest;
import com.plana.infli.web.dto.response.comment.create.CreateCommentResponse;
import com.plana.infli.web.dto.response.comment.view.BestCommentResponse;
import com.plana.infli.web.dto.response.comment.view.mycomment.MyComment;
import com.plana.infli.web.dto.response.comment.view.mycomment.MyCommentsResponse;
import com.plana.infli.web.dto.response.comment.view.post.PostComment;
import com.plana.infli.web.dto.response.comment.view.post.PostCommentsResponse;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;

    private final PostRepository postRepository;

    private final MemberRepository memberRepository;

    private final UniversityRepository universityRepository;

    // 특정 글에 작성된 댓글 목록 조회시 한 페이지당 조회되길 원하는 댓글 갯수
    private static final Integer COMMENT_SIZE_PER_PAGE = 100;

    @Transactional
    @Retry
    public CreateCommentResponse createComment(CreateCommentServiceRequest request) {

        validateContentLength(request.getContent());

        Member member = findMemberBy(request.getUsername());

        checkWritePermission(member);

        Post post = findPostWithLockBy(request.getPostId());

        checkPostAndMemberInSameUniversity(member, post);

        @Nullable Comment parentComment = findParentCommentIfExists(request.getParentCommentId());

        validateParentComment(parentComment, post);

        int identifierNumber = generateIdentifierNumber(post, member);

        Comment comment = create(post, request.getContent(), member, parentComment,
                identifierNumber);

        return CreateCommentResponse.of(commentRepository.save(comment));
    }

    private Post findPostWithLockBy(Long postId) {
        return postRepository.findActivePostWithOptimisticLock(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
    }


    private void validateContentLength(String content) {
        if (content.length() > 500) {
            throw new BadRequestException(MAX_COMMENT_SIZE_EXCEEDED);
        }
    }

    private void checkWritePermission(Member member) {
        if (member.getVerificationStatus() == SUCCESS) {
            return;
        }
        throw new AuthorizationFailedException();
    }


    private void checkPostAndMemberInSameUniversity(Member member, Post post) {
        if (universityRepository.isMemberAndPostInSameUniversity(member, post) == false) {
            throw new AuthorizationFailedException();
        }
    }

    private Comment findParentCommentIfExists(Long commentId) {
        return commentId != null ? findCommentWithPostBy(commentId) : null;
    }

    //TODO
    private void validateParentComment(Comment parentComment, Post post) {

        if (parentComment == null) {
            return;
        }

        // 대댓글을 작성할 글의 번호와, 부모 댓글의 글 번호가 서로 다른 경우
        if (parentComment.getPost().equals(post) == false) {
            throw new NotFoundException(COMMENT_NOT_FOUND);
        }

        // 대댓글에 대댓글을 작성할수 없다
        if (parentComment.getParentComment() != null) {
            throw new BadRequestException(CHILD_COMMENTS_NOT_ALLOWED);
        }
    }

    private Comment findCommentWithPostBy(Long commentId) {
        return commentRepository.findActiveCommentWithPostBy(commentId)
                .orElseThrow(() -> new NotFoundException(COMMENT_NOT_FOUND));
    }

    private int generateIdentifierNumber(Post post, Member member) {

        // 글 작성자가 자신의 글에 댓글을 작성하려는 경우
        if (post.getMember().equals(member)) {
            // 식별자 0번을 부여한다
            return 0;
        }

        Integer identifierNumber = commentRepository.findIdentifierNumberBy(post, member);

        // 식별자 번호가 없는 경우
        // 즉, 해당 회원이 이 글에 댓글을 한번도 단 적이 없는 경우
        if (identifierNumber == null) {

            // 새로운 식별자 번호를 생성한후 부여한다
            // 이 글에 작성된 댓글들에게 부여된 가장 최근 식별자 번호
            return increaseCommentMemberCount(post);
        }

        // 식별자 번호가 존재하는 경우 기존 번호 그대로 다시 부여한다
        return identifierNumber;
    }


    @Transactional
    public void editCommentContent(EditCommentServiceRequest request) {

        validateContentLength(request.getContent());

        Member member = findMemberBy(request.getUsername());

        Comment comment = findCommentWithMemberAndPostBy(request.getCommentId());

        validateEditRequest(comment, member);

        editContent(comment, request.getContent());
    }

    private Comment findCommentWithMemberAndPostBy(Long commentId) {
        return commentRepository.findActiveCommentWithMemberAndPostBy(commentId)
                .orElseThrow(() -> new NotFoundException(COMMENT_NOT_FOUND));
    }


    private Member findMemberBy(String username) {
        return memberRepository.findActiveMemberBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    private void validateEditRequest(Comment comment, Member member) {

        Post post = comment.getPost();

        if (post.isDeleted()) {
            throw new BadRequestException(EDIT_COMMENT_IN_DELETED_POST_NOT_ALLOWED);
        }

        // 댓글 작성자만 해당 댓글을 수정 할 수 있다
        if (comment.getMember().equals(member) == false) {
            throw new AuthorizationFailedException();
        }
    }

    @Transactional
    public void deleteComment(String username, Long commentId) {

        // 댓글 삭제 요청을 한 회원이 존재하지 않거나, 삭제된 경우 예외 발생
        Member member = findMemberBy(username);

        Comment comment = findCommentWithMemberBy(commentId);

        // 삭제할 댓글에 대한 검증 진행
        validateDeleteRequest(member, comment);

        delete(comment);
    }

    private Comment findCommentWithMemberBy(Long commentId) {
        return commentRepository.findActiveCommentWithMemberBy(commentId)
                .orElseThrow(() -> new NotFoundException(COMMENT_NOT_FOUND));
    }

    private void validateDeleteRequest(Member member, Comment comment) {

        // 관리자는 어떤 댓글도 삭제할수 있다
        if (isAdmin(member)) {
            return;
        }

        // 댓글 작성자 본인과 관리자만 해당 댓글을 삭제할 수 있다
        if (comment.getMember().equals(member) == false) {
            throw new AuthorizationFailedException();
        }
    }

    public PostCommentsResponse loadCommentsInPost(String username, Long postId, Integer page) {

        Member member = findMemberBy(username);

        Post post = findPostWithMemberBy(postId);

        CommentQueryRequest request = CommentQueryRequest.of(
                post, member, COMMENT_SIZE_PER_PAGE, page);

        List<PostComment> comments = commentRepository.findCommentsInPost(request);

        return PostCommentsResponse.of(comments, request);
    }

    private Post findPostWithMemberBy(Long postId) {
        return postRepository.findActivePostWithMemberBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
    }

    public MyCommentsResponse loadMyComments(int page, String email) {

        // 자신이 작성한 댓글 목록을 보고싶은 회원
        Member member = findMemberBy(email);

        CommentQueryRequest request = myComments(member, 20, page);


        List<MyComment> comments = commentRepository.findMyComments(request);

        return MyCommentsResponse.of(request, comments);
    }

    public Long findActiveCommentsCountInPost(Long postId) {

        // 존재하지 않거나 삭제된 글에 작성된 댓글의 갯수를 조회할수 없다
        Post post = findPostBy(postId);

        return commentRepository.findActiveCommentsCountIn(post);
    }

    public BestCommentResponse loadBestCommentInPost(Long postId, String email) {

        Post post = findPostBy(postId);

        Member member = findMemberBy(email);

        checkPostAndMemberInSameUniversity(member, post);

        // 해당 글의 베스트 댓글 조회
        // 베스트 댓글 선정 기준
        // 1. 좋아요 10개 이상 받은 댓글들
        // 2. 그 댓글들 중에서 가장 좋아요가 높은 한개의 댓글이 베스트 댓글로 선정됨
        // 위의 조건을 만족하는 댓글이 없는 경우 null 이 반환
        @Nullable BestCommentResponse response = commentRepository.findBestCommentIn(post);

        return response;
    }

    private Post findPostBy(Long postId) {
        return postRepository.findActivePostBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
    }

    public Long findCommentsCountByMember(String username) {

        Member member = findMemberBy(username);

        return commentRepository.findActiveCommentsCountBy(member);
    }
}
