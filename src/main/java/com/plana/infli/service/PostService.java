package com.plana.infli.service;

import static com.plana.infli.domain.BoardType.*;
import static com.plana.infli.domain.BoardType.SubBoardType.*;
import static com.plana.infli.domain.Member.isAdmin;
import static com.plana.infli.domain.Post.*;
import static com.plana.infli.domain.PostType.*;
import static com.plana.infli.domain.editor.member.MemberEditor.*;
import static com.plana.infli.domain.editor.post.PostEditor.edit;
import static com.plana.infli.domain.embeddable.Recruitment.*;
import static com.plana.infli.exception.custom.BadRequestException.*;
import static com.plana.infli.exception.custom.NotFoundException.*;
import static com.plana.infli.web.dto.request.post.create.recruitment.CreateRecruitmentPostServiceRequest.*;
import static com.plana.infli.web.dto.request.post.view.PostQueryRequest.*;
import static com.plana.infli.web.dto.request.post.create.normal.CreateNormalPostServiceRequest.*;
import static com.plana.infli.web.dto.response.post.my.MyPostsResponse.loadMyPostsResponse;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.BoardType;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.domain.Role;
import com.plana.infli.domain.embeddable.MemberStatus;
import com.plana.infli.domain.embeddable.Recruitment;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.aop.Retry;
import com.plana.infli.web.dto.request.post.create.recruitment.CreateRecruitmentPostServiceRequest;
import com.plana.infli.web.dto.request.post.edit.recruitment.EditRecruitmentPostServiceRequest;
import com.plana.infli.web.dto.request.post.view.PostQueryRequest;
import com.plana.infli.web.dto.request.post.view.board.LoadPostsByBoardServiceRequest;
import com.plana.infli.web.dto.request.post.create.normal.CreateNormalPostServiceRequest;
import com.plana.infli.web.dto.request.post.edit.normal.EditNormalPostServiceRequest;
import com.plana.infli.web.dto.request.post.view.search.SearchPostsByKeywordServiceRequest;
import com.plana.infli.web.dto.response.post.board.BoardPost;
import com.plana.infli.web.dto.response.post.board.BoardPostsResponse;
import com.plana.infli.web.dto.response.post.my.MyPost;
import com.plana.infli.web.dto.response.post.my.MyPostsResponse;
import com.plana.infli.web.dto.response.post.search.SearchedPost;
import com.plana.infli.web.dto.response.post.search.SearchedPostsResponse;
import com.plana.infli.web.dto.response.post.single.SinglePostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    private final BoardRepository boardRepository;

    private final MemberRepository memberRepository;

    private final UniversityRepository universityRepository;

    public boolean checkMemberAgreedOnWritePolicy(String email) {
        MemberStatus memberStatus = findMemberBy(email).getMemberStatus();

        return memberStatus.isPolicyAccepted();
    }

    @Transactional
    public void confirmWritePolicyAgreement(String email) {
        Member member = findMemberBy(email);

        acceptPolicy(member);
    }

    private Member findMemberBy(String email) {
        return memberRepository.findActiveMemberBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    @Transactional
    public Long createNormalPost(CreateNormalPostServiceRequest request) {

        Member member = findMemberWithUniversityBy(request.getEmail());

        Board board = findBoardWithUniversityBy(request.getBoardId());

        validateCreateNormalPostRequest(member, board, request.getPostType());

        return postRepository.save(toNormalPost(member, board, request)).getId();
    }

    private Member findMemberWithUniversityBy(String email) {
        return memberRepository.findActiveMemberWithUniversityBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    private Board findBoardWithUniversityBy(Long boardId) {
        return boardRepository.findActiveBoardWithUniversityBy(boardId)
                .orElseThrow(() -> new NotFoundException(BOARD_NOT_FOUND));
    }

    //TODO
    private void validateCreateNormalPostRequest(Member member, Board board, PostType postType) {

        checkIsInSameUniversity(member, board);

        BoardType boardType = board.getBoardType();

        if (postType == RECRUITMENT) {
            throw new BadRequestException(POST_TYPE_NOT_ALLOWED);
        }

        validateWriteRequest(member.getRole(), postType, boardType);
    }

    private void validateWriteRequest(Role role, PostType postType, BoardType boardType) {
        SubBoardType subBoardType = of(boardType, postType);

        if (subBoardType == null) {
            throw new BadRequestException(INVALID_BOARD_TYPE);
        }

        if (subBoardType.hasWritePermission(role) == false) {
            throw new AuthorizationFailedException();
        }
    }

    private void checkIsInSameUniversity(Member member, Board board) {
        if (member.getUniversity().equals(board.getUniversity()) == false) {
            throw new AuthorizationFailedException();
        }
    }

    @Transactional
    public Long createRecruitmentPost(CreateRecruitmentPostServiceRequest request) {

        Member member = findMemberWithUniversityBy(request.getEmail());

        Board board = findBoardWithUniversityBy(request.getBoardId());

        validateCreateRecruitmentPostRequest(member, board, request);

        Recruitment recruitment = loadRecruitment(request);

        return postRepository.save(toRecruitmentPost(member, board, request, recruitment)).getId();
    }

    //TODO
    private void validateCreateRecruitmentPostRequest(Member member, Board board,
            CreateRecruitmentPostServiceRequest request) {

        if (request.getRecruitmentStartDate().isAfter(request.getRecruitmentEndDate())) {
            throw new BadRequestException(INVALID_RECRUITMENT_DATE);
        }

        checkIsInSameUniversity(member, board);

        if (isRecruitmentBoard(board) == false) {
            throw new BadRequestException(BOARD_TYPE_IS_NOT_RECRUITMENT);
        }

        validateWriteRequest(member.getRole(), RECRUITMENT, board.getBoardType());
    }

    private boolean isRecruitmentBoard(Board board) {
        return board.getBoardType() == EMPLOYMENT || board.getBoardType() == ACTIVITY;
    }

    private Recruitment loadRecruitment(CreateRecruitmentPostServiceRequest request) {
        return create(request.getRecruitmentCompanyName(),
                request.getRecruitmentStartDate(),
                request.getRecruitmentEndDate());
    }

    @Transactional
    public void editNormalPost(EditNormalPostServiceRequest request) {

        Member member = findMemberBy(request.getEmail());

        Post post = findPostWithMemberBy(request.getPostId());

        validateEditNormalPostRequest(post, member);

        edit(request, post);

    }

    //TODO
    private void validateEditNormalPostRequest(Post post, Member member) {

        if (isRecruitmentPost(post)) {
            throw new BadRequestException("");
        }

        checkThisMemberIsPostWriter(member, post);
    }

    private void checkThisMemberIsPostWriter(Member member, Post post) {
        if (post.getMember().equals(member) == false) {
            throw new AuthorizationFailedException();
        }
    }

    private boolean isRecruitmentPost(Post post) {
        return post.getPostType() == RECRUITMENT;
    }


    @Transactional
    public void editRecruitmentPost(EditRecruitmentPostServiceRequest request) {

        Member member = findMemberBy(request.getEmail());

        Post post = findPostWithMemberBy(request.getPostId());

        validateEditRecruitmentPostRequest(post, member);

        edit(request, post);
    }

    private void validateEditRecruitmentPostRequest(Post post, Member member) {

        if (isRecruitmentPost(post) == false) {
            throw new BadRequestException("");
        }

        checkThisMemberIsPostWriter(member, post);
    }


    @Transactional
    public void deletePost(Long postId, String email) {

        Member member = findMemberBy(email);

        Post post = findPostWithMemberBy(postId);

        validateDeleteRequest(member, post);

        delete(post);
    }

    private Post findPostWithMemberBy(Long postId) {
        return postRepository.findActivePostWithMemberBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
    }

    private void validateDeleteRequest(Member member, Post post) {

        if (isAdmin(member)) {
            return;
        }

        checkThisMemberIsPostWriter(member, post);
    }

    @Transactional
    @Retry
    public SinglePostResponse loadSinglePost(Long postId, String email) {

        Member member = findMemberBy(email);

        Post post = postRepository.findActivePostWithOptimisticLock(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));

        checkMemberAndPostIsInSameUniversity(member, post);

        plusViewCount(post);

        PostQueryRequest request = singlePost(post, member);

        return postRepository.loadSinglePostResponse(request);
    }

    private void checkMemberAndPostIsInSameUniversity(Member member, Post post) {
        if (universityRepository.isMemberAndPostInSameUniversity(member, post) == false) {
            throw new AuthorizationFailedException();
        }
    }


    public MyPostsResponse loadMyPosts(String email, String page) {

        Member member = findMemberBy(email);

        PostQueryRequest request = myPosts(member, page, 20);

        List<MyPost> posts = postRepository.loadMyPosts(request);

        return loadMyPostsResponse(request, posts);
    }



    private Board findBoardBy(Long boardId) {
        return boardRepository.findActiveBoardBy(boardId)
                .orElseThrow(() -> new NotFoundException(BOARD_NOT_FOUND));
    }


    public SearchedPostsResponse searchPostsByKeyword(SearchPostsByKeywordServiceRequest request) {

        Member member = findMemberWithUniversityBy(request.getEmail());

        PostQueryRequest queryRequest = searchByKeyword(member, request.getKeyword(),
                request.getPage(), 20);

        List<SearchedPost> posts = postRepository.searchPostByKeyWord(queryRequest);


        return null;
    }


    public BoardPostsResponse loadPostsByBoard(LoadPostsByBoardServiceRequest request) {

        Member member = findMemberBy(request.getEmail());

        Board board = findBoardBy(request.getBoardId());

        checkIsInSameUniversity(member, board);

        validateTypes(request.getType(), board.getBoardType());

        PostQueryRequest queryRequest = postsByBoard(board, member, request, 20);

        List<BoardPost> posts = postRepository.loadPostsByBoard(queryRequest);

        return BoardPostsResponse.loadResponse(posts, queryRequest);
    }

    private void validateTypes(PostType postType, BoardType boardType) {
        SubBoardType subBoardType = of(boardType, postType);

        if (subBoardType == null) {
            throw new BadRequestException(INVALID_BOARD_TYPE);
        }
    }

    public void validateWriteRequest(Long boardId, String email,
            PostType postType) {

        Member member = findMemberWithUniversityBy(email);

        Board board = findBoardWithUniversityBy(boardId);

        checkIsInSameUniversity(member, board);

        validateWriteRequest(member.getRole(), postType, board.getBoardType());
    }
}
