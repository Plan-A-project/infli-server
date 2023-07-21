package com.plana.infli.service;

import static com.plana.infli.domain.Board.isAnonymousBoard;
import static com.plana.infli.domain.Comment.*;
import static com.plana.infli.domain.Member.isAdmin;
import static com.plana.infli.domain.Member.checkIsLoggedIn;
import static com.plana.infli.domain.editor.comment.CommentContentEditor.editComment;
import static com.plana.infli.exception.custom.BadRequestException.CHILD_COMMENTS_NOT_ALLOWED;
import static com.plana.infli.exception.custom.BadRequestException.MAX_COMMENT_SIZE_EXCEEDED;
import static com.plana.infli.exception.custom.BadRequestException.PARENT_COMMENT_IS_DELETED;
import static com.plana.infli.exception.custom.BadRequestException.PARENT_COMMENT_IS_NOT_FOUND;
import static com.plana.infli.exception.custom.NotFoundException.*;
import static com.plana.infli.web.dto.response.comment.view.mycomment.MyCommentsResponse.loadMyCommentsResponse;
import static com.plana.infli.web.dto.response.comment.view.post.PostCommentsResponse.loadPostCommentsResponse;
import static java.lang.Math.*;
import static org.springframework.data.domain.PageRequest.of;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.Role;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.comment.create.service.CreateCommentServiceRequest;
import com.plana.infli.web.dto.request.comment.delete.service.DeleteCommentServiceRequest;
import com.plana.infli.web.dto.request.comment.edit.service.EditCommentServiceRequest;
import com.plana.infli.web.dto.request.comment.view.post.service.LoadCommentsInPostServiceRequest;
import com.plana.infli.web.dto.response.comment.create.CreateCommentResponse;
import com.plana.infli.web.dto.response.comment.edit.EditCommentResponse;
import com.plana.infli.web.dto.response.comment.view.BestCommentResponse;
import com.plana.infli.web.dto.response.comment.view.mycomment.MyComment;
import com.plana.infli.web.dto.response.comment.view.mycomment.MyCommentsResponse;
import com.plana.infli.web.dto.response.comment.view.post.PostComment;
import com.plana.infli.web.dto.response.comment.view.post.PostCommentsResponse;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    private final EntityManager em;

    // 특정 글에 작성된 댓글 목록 조회시 한 페이지당 조회되길 원하는 댓글 갯수
    private static final Integer COMMENT_SIZE_PER_PAGE = 100;

    @Transactional
    public CreateCommentResponse createComment(CreateCommentServiceRequest request, String email) {

        // 최대 허용 댓글 길이는 500자 이다
        validateContentLength(request.getContent());

        //TODO
        // 인증하려는 객체가 없는 경우 401 에러를 발생한다
        checkIsLoggedIn(email);

        // 댓글을 작성할 회원이 존재하지 않거나, 삭제된 경우 예외 발생
        Member member = findMemberWithUniversityJoined(email);

        throwExceptionIfMemberIsUncertified(member);

        // 댓글이 작성될 글이 존재하지 않거나, 삭제된 경우 예외 발생
        Post post = findPostWithBoardAndMemberJoined(request);

        // 회원은 소속된 대학이 아닌 다른 대학에서 작성된 글에 댓글을 달 수 없다.
        // 따라서 회원과 글이 동일한 대학에 존재하는지 검증 진행
        isPostAndMemberInSameUniversity(member, post);

        //TODO
        // 회원이 작성하려는 댓글이 대댓글인 경우, 부모 댓글에 대한 검증 진행
        // 대댓글이 아닌 일반 댓글인 경우 부모 댓글의 값은 null 이다
        @Nullable Comment parentComment = validateParentCommentIfExists(request, post);

        // 식별자 번호 조회, 없는 경우 새로운 번호를 생성한다
        Integer identifierNumber = loadIdentifierNumber(post, member);

        Comment savedComment = commentRepository.save(create(post, request.getContent(), member,
                parentComment, identifierNumber));

        return CreateCommentResponse.of(savedComment, post, member);
    }

    private void validateContentLength(String content) {
        if (content.length() > 500) {
            throw new BadRequestException(MAX_COMMENT_SIZE_EXCEEDED);
        }
    }

    private Member findMemberWithUniversityJoined(String email) {
        return memberRepository.findActiveMemberWithUniversityBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    private Post findPostWithBoardAndMemberJoined(CreateCommentServiceRequest request) {
        return postRepository.findPessimisticLockActivePostWithBoardAndMemberBy(request.getPostId())
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
    }

    private static void throwExceptionIfMemberIsUncertified(Member member) {
        if (member.getRole().equals(Role.UNCERTIFIED)) {
            throw new AuthorizationFailedException();
        }
    }


    private void isPostAndMemberInSameUniversity(Member member, Post post) {
        if (post.getBoard().isDeleted()) {
            throw new NotFoundException(POST_NOT_FOUND);
        }

        if (!universityRepository.isMemberAndPostInSameUniversity(member, post)) {
            throw new AuthorizationFailedException();
        }
    }

    @Nullable
    private Comment validateParentCommentIfExists(CreateCommentServiceRequest request, Post post) {

        if (request.getParentCommentId() == null) {
            return null;
        }

        // 존재하지 않는 댓글에 대댓글을 작성할 수 없다
        Comment parentComment = commentRepository.findWithPostById(request.getParentCommentId())
                .orElseThrow(() -> new BadRequestException(PARENT_COMMENT_IS_NOT_FOUND));

        // 삭제된 댓글에 대댓글을 작성할 수 없다
        if (parentComment.isDeleted()) {
            throw new BadRequestException(PARENT_COMMENT_IS_DELETED);
        }

        // 대댓글을 작성할 글의 번호와, 부모 댓글의 글 번호가 서로 다른 경우
        if (parentComment.getPost().getId().equals(post.getId()) == false) {
            throw new NotFoundException(COMMENT_NOT_FOUND);
        }

        // 대댓글에 대댓글을 작성할수 없다
        if (parentComment.getParentComment() != null) {
            throw new BadRequestException(CHILD_COMMENTS_NOT_ALLOWED);
        }

        return parentComment;
    }

    private Integer loadIdentifierNumber(Post post, Member member) {

        // 글 작성자가 자신의 글에 댓글을 작성하려는 경우
        if (post.getMember().getId().equals(member.getId())) {

            // 식별자 0번을 부여한다
            return 0;
        }

        Integer identifierNumber = commentRepository.findIdentifierNumberBy(post, member);

        // 식별자 번호가 없는 경우
        // 즉, 해당 회원이 이 글에 댓글을 한번도 단 적이 없는 경우
        if (identifierNumber == null) {

            // 새로운 식별자 번호를 생성한후 부여한다

            // 이 글에 작성된 댓글들에게 부여된 가장 최근 식별자 번호
            return post.increaseCount();
        }

        // 식별자 번호가 존재하는 경우 기존 번호 그대로 다시 부여한다
        return identifierNumber;
    }


    @Transactional
    public EditCommentResponse editContent(EditCommentServiceRequest request, String email) {

        // 인증하려는 객체가 없는 경우 401 에러를 발생한다
        //TODO
        checkIsLoggedIn(email);

        // 최대 허용 댓글 길이는 500자 이다
        validateContentLength(request.getContent());

        // 댓글 수정할 회원이 존재하지 않거나, 삭제된 경우 예외 발생
        Member member = findMember(email);

        //TODO 설명 맘에 안듬
        // 수정하고 싶은 댓글이 작성된 글이 존재하지 않거나, 삭제된 경우 예외 발생
        Post post = postRepository.findActivePostWithBoardBy(request.getPostId())
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));

        // 수정할 댓글이 존재하지 않거나, 삭제된 경우 예외 발생
        Comment comment = commentRepository.findActiveCommentWithMemberAndPostBy(
                        request.getCommentId())
                .orElseThrow(() -> new NotFoundException(COMMENT_NOT_FOUND));

        // 댓글 수정 요청에 대한 검증 진행
        validateEditRequest(comment, member, post);

        // 댓글 수정 진행
        Comment editedComment = editComment(comment, request.getContent());

        return EditCommentResponse.of(editedComment, comment.getPost(), member);
    }

    private Member findMember(String email) {
        return memberRepository.findActiveMemberBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    private void validateEditRequest(Comment comment, Member member, Post post) {

        // 삭제된 게시판에 댓글을 작성할 수 없다
        if (post.getBoard().isDeleted()) {
            throw new NotFoundException(BOARD_NOT_FOUND);
        }


        // 클라이언트에서 전송한 글 ID 번호와, 수정할 댓글이 작성된 글의 번호는 동일해야 한다
        if (comment.getPost().getId().equals(post.getId()) == false) {
            throw new NotFoundException(COMMENT_NOT_FOUND);
        }

        // 댓글 작성자만 해당 댓글을 수정 할 수 있다
        if (comment.getMember().getId().equals(member.getId()) == false) {
            throw new AuthorizationFailedException();
        }
    }

    @Transactional
    public void delete(DeleteCommentServiceRequest request, String email) {

        // 인증하려는 객체가 없는 경우 401 에러를 발생한다
        //TODO 설명 맘에 안듬
        checkIsLoggedIn(email);

        // 댓글 삭제 요청을 한 회원이 존재하지 않거나, 삭제된 경우 예외 발생
        Member member = findMember(email);

        // 삭제할 댓글 ID 목록
        List<Long> ids = request.getIds();

        // 삭제할 댓글에 대한 검증 진행
        validateDeleteRequest(member, ids);
        commentRepository.deleteAllByIdsInBatch(ids);

        em.clear();
    }

    private void validateDeleteRequest(Member member, List<Long> ids) {

        // 삭제되지 않은 댓글들 DB 에서 조회
        List<Comment> activeComments = commentRepository.findActiveCommentWithMemberByIdsIn(ids);

        // Ex) 5개 댓글에 대한 조회 요청을 했으나, 실제 DB 에서 4개만 조회된 경우
        if (ids.size() != activeComments.size()) {
            throw new NotFoundException(COMMENT_NOT_FOUND);
        }


        activeComments.forEach(comment -> {

            // 관리자는 어떤 댓글도 삭제할수 있다
            if (isAdmin(member)) {
                return;
            }

            // 댓글 작성자 본인과 관리자만 해당 댓글을 삭제할 수 있다
            if (comment.getMember().equals(member) == false) {
                throw new AuthorizationFailedException();
            }
        });
    }

    public PostCommentsResponse loadCommentsInPost(LoadCommentsInPostServiceRequest request,
            String email) {

        // 댓글 조회를 요청한 회원
        Member member = findMember(email);

        // 존재하지 않거나, 삭제된 글에 작성된 댓글을 조회할수 없다
        Post post = postRepository.findActivePostBy(request.getId())
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));

        PageRequest pageRequest = createPageRequest(request);

        // 해당 글에 작성된 댓글 목록
        List<PostComment> comments = commentRepository.findCommentsInPost(post, member,
                pageRequest);

        return loadPostCommentsResponse(post.getId(), isAnonymousBoard(post.getBoard()),
                pageRequest, isAdmin(member), comments);
    }

    private PageRequest createPageRequest(LoadCommentsInPostServiceRequest request) {

        int page;

        try {
            page = Integer.parseInt(request.getPage());
        } catch (NumberFormatException e) {
            page = 1;
        }

        return of(request.getPage() == null ? 1 : max(1, page), COMMENT_SIZE_PER_PAGE);
    }

    public MyCommentsResponse loadMyComments(Integer page, String email) {

        checkIsLoggedIn(email);

        // 자신이 작성한 댓글 목록을 보고싶은 회원
        Member member = findMember(email);

        PageRequest pageRequest = of(page, 20);

        List<MyComment> comments = commentRepository.findMyComments(member, pageRequest);

        return loadMyCommentsResponse(pageRequest, comments);
    }

    public Long findActiveCommentsCountInPost(Long postId) {

        // 존재하지 않거나 삭제된 글에 작성된 댓글의 갯수를 조회할수 없다
        Post post = postRepository.findActivePostBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));

        return commentRepository.findActiveCommentsCountIn(post);
    }

    public BestCommentResponse loadBestCommentInPost(Long postId, String email) {

        // 존재하지 않거나 삭제된 글에 작성된 베스트 댓글을 조회할수 없다
        Post post = postRepository.findActivePostBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));

        Member member = findMember(email);

        isPostAndMemberInSameUniversity(member, post);

        // 해당 글의 베스트 댓글 조회
        // 베스트 댓글 선정 기준
        // 1. 좋아요 10개 이상 받은 댓글들
        // 2. 그 댓글들 중에서 가장 좋아요가 높은 한개의 댓글이 베스트 댓글로 선정됨
        // 위의 조건을 만족하는 댓글이 없는 경우 null 이 반환
        @Nullable BestCommentResponse response = commentRepository.findBestCommentIn(post);

        return response;
    }

    public Long findCommentsCountByMember(String email) {

        checkIsLoggedIn(email);

        // 자신이 작성한 총 댓글 갯수를 보고싶은 회원
        Member member = findMember(email);

        return commentRepository.findActiveCommentsCountBy(member);
    }
}
