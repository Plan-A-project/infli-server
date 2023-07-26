package com.plana.infli.service;

import static com.plana.infli.domain.BoardType.*;
import static com.plana.infli.domain.Member.isAdmin;
import static com.plana.infli.domain.PostType.*;
import static com.plana.infli.exception.custom.NotFoundException.*;
import static com.plana.infli.web.dto.request.post.view.PostQueryRequest.*;
import static com.plana.infli.web.dto.request.post.create.CreatePostServiceRequest.*;
import static com.plana.infli.web.dto.response.post.my.MyPostsResponse.loadMyPostsResponse;
import static java.lang.Integer.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.BoardType;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.domain.University;
import com.plana.infli.domain.editor.post.PostEditor;
import com.plana.infli.domain.embeddable.Recruitment;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.post.view.PostQueryRequest;
import com.plana.infli.web.dto.request.post.create.CreatePostServiceRequest.CreateRecruitmentServiceRequest;
import com.plana.infli.web.dto.request.post.edit.EditPostServiceRequest.EditRecruitmentServiceRequest;
import com.plana.infli.web.dto.request.post.view.board.LoadPostsByBoardServiceRequest;
import com.plana.infli.web.dto.request.post.create.CreatePostServiceRequest;
import com.plana.infli.web.dto.request.post.edit.EditPostServiceRequest;
import com.plana.infli.web.dto.request.post.view.search.SearchPostsByKeywordServiceRequest;
import com.plana.infli.web.dto.response.post.board.BoardPost;
import com.plana.infli.web.dto.response.post.board.BoardPostsResponse;
import com.plana.infli.web.dto.response.post.my.MyPost;
import com.plana.infli.web.dto.response.post.my.MyPostsResponse;
import com.plana.infli.web.dto.response.post.search.SearchedPost;
import com.plana.infli.web.dto.response.post.search.SearchedPostsResponse;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
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
        Member member = findMemberBy(email);
        return postRepository.existsByMember(member);
    }

    private Member findMemberBy(String email) {
        return memberRepository.findActiveMemberBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }


    @Transactional
    public void confirmWritePolicyAgreement(String email) {
        Member member = findMemberBy(email);
        member.agreedOnPostWritePolicy();
    }

    @Transactional
    public Long createPost(CreatePostServiceRequest request) {

        Member member = findMemberWithUniversity(request.getEmail());

        Board board = findBoardWithUniversity(request);

        checkMemberAndBoardIsInSameUniversity(member, board);

        validateTypes(request.getPostType(), board.getBoardType());

        @Nullable Recruitment recruitment = loadRecruitmentIfExists(request.getRecruitment());

        Post post = of(member, board, request, recruitment);

        return postRepository.save(post).getId();
    }

    private Member findMemberWithUniversity(String email) {
        return memberRepository.findActiveMemberWithUniversityBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    private Board findBoardWithUniversity(CreatePostServiceRequest request) {
        return boardRepository.findActiveBoardWithUniversityBy(request.getBoardId())
                .orElseThrow(() -> new NotFoundException(BOARD_NOT_FOUND));
    }

    private void checkMemberAndBoardIsInSameUniversity(Member member, Board board) {
        if (member.getUniversity().equals(board.getUniversity()) == false) {
            throw new AuthorizationFailedException();
        }
    }

    private Recruitment loadRecruitmentIfExists(CreateRecruitmentServiceRequest recruitmentRequest) {

        return recruitmentRequest != null ? Recruitment.create(recruitmentRequest.getCompanyName(),
                recruitmentRequest.getStartDate(), recruitmentRequest.getEndDate())
                : null;
    }


    @Transactional
    public void editPost(EditPostServiceRequest request) {

        Member member = findMemberBy(request.getEmail());

        Post post = findPostBy(request.getPostId());

        validateEditRequest(request, post, member);

        PostEditor.editPost(request, post);
    }

    private Post findPostBy(Long postId) {
        return postRepository.findActivePostBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
    }

    //TODO
    private void validateEditRequest(EditPostServiceRequest request, Post post, Member member) {

        checkThisMemberIsPostWriter(member, post);

        EditRecruitmentServiceRequest recruitment = request.getRecruitment();

        if (recruitment == null) {
            return;
        }

        if (postTypeIsNotRecruitment(post)) {
            throw new BadRequestException("");
        }
    }

    private void checkThisMemberIsPostWriter(Member member, Post post) {
        if (post.getMember().equals(member) == false) {
            throw new AuthorizationFailedException();
        }
    }

    private boolean postTypeIsNotRecruitment(Post post) {
        return post.getPostType() != RECRUITMENT;
    }

    @Transactional
    public void deletePost(Long postId, String email) {

        Member member = findMemberBy(email);

        Post post = findPostWithMemberBy(postId);

        validateDeleteRequest(member, post);

        postRepository.delete(post);
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
    public SinglePostResponse loadSinglePost(Long postId, String email) {

        Member member = findMemberBy(email);

        Post post = findPostWithBoardBy(postId);

        checkMemberAndPostIsInSameUniversity(member, post);

        //TODO 동시성 고려 필요
        post.plusViewCount();

        PostQueryRequest request = singlePost(post, member);

        return postRepository.loadSinglePostResponse(request);
    }

    private Post findPostWithBoardBy(Long postId) {
        return postRepository.findActivePostWithBoardBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
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

        Member member = findMemberWithUniversity(request.getEmail());

        PostQueryRequest queryRequest = searchByKeyword(member, request.getKeyword(),
                request.getPage(), 20);

        List<SearchedPost> posts = postRepository.searchPostByKeyWord(queryRequest);


        return null;
    }


    public BoardPostsResponse loadPostsByBoard(LoadPostsByBoardServiceRequest request) {

        Member member = findMemberBy(request.getEmail());

        Board board = findBoardBy(request.getBoardId());

        checkMemberAndBoardIsInSameUniversity(member, board);

        validateTypes(request.getType(), board.getBoardType());

        PostQueryRequest queryRequest = postsByBoard(board, member, request, 20);

        List<BoardPost> posts = postRepository.loadPostsByBoard(queryRequest);

        return BoardPostsResponse.loadResponse(posts, queryRequest);
    }

    //TODO
    private void validateTypes(PostType postType, BoardType boardType) {

        if (postType == NORMAL) {
            return;
        }

        if (postType == ANNOUNCEMENT && boardType == CAMPUS_LIFE) {
            return;
        }

        if (postType == RECRUITMENT && (boardType == ACTIVITY || boardType == EMPLOYMENT)) {
            return;
        }

        throw new BadRequestException("");
    }

}
