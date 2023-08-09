package com.plana.infli.service;

import static com.plana.infli.domain.BoardType.*;
import static com.plana.infli.domain.BoardType.SubBoardType.*;
import static com.plana.infli.domain.Member.isAdmin;
import static com.plana.infli.domain.PostType.*;
import static com.plana.infli.domain.editor.MemberEditor.*;
import static com.plana.infli.domain.editor.PostEditor.delete;
import static com.plana.infli.domain.editor.PostEditor.edit;
import static com.plana.infli.domain.editor.PostEditor.increaseViewCount;
import static com.plana.infli.domain.embedded.post.Recruitment.*;
import static com.plana.infli.exception.custom.BadRequestException.*;
import static com.plana.infli.exception.custom.NotFoundException.*;
import static com.plana.infli.web.dto.request.post.create.recruitment.CreateRecruitmentPostServiceRequest.*;
import static com.plana.infli.web.dto.request.post.view.PostQueryRequest.*;
import static com.plana.infli.web.dto.response.post.my.MyPostsResponse.loadMyPostsResponse;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.BoardType;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.domain.Role;
import com.plana.infli.domain.embedded.member.MemberStatus;
import com.plana.infli.domain.embedded.post.Recruitment;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.aop.retry.Retry;
import com.plana.infli.utils.S3Uploader;
import com.plana.infli.web.dto.request.post.create.recruitment.CreateRecruitmentPostServiceRequest;
import com.plana.infli.web.dto.request.post.edit.recruitment.EditRecruitmentPostServiceRequest;
import com.plana.infli.web.dto.request.post.view.PostQueryRequest;
import com.plana.infli.web.dto.request.post.view.board.LoadPostsByBoardServiceRequest;
import com.plana.infli.web.dto.request.post.create.normal.CreateNormalPostServiceRequest;
import com.plana.infli.web.dto.request.post.edit.normal.EditNormalPostServiceRequest;
import com.plana.infli.web.dto.request.post.view.search.SearchPostsByKeywordServiceRequest;
import com.plana.infli.web.dto.response.post.board.BoardPost;
import com.plana.infli.web.dto.response.post.board.BoardPostsResponse;
import com.plana.infli.web.dto.response.post.image.PostImageUploadResponse;
import com.plana.infli.web.dto.response.post.my.MyPost;
import com.plana.infli.web.dto.response.post.my.MyPostsResponse;
import com.plana.infli.web.dto.response.post.search.SearchedPost;
import com.plana.infli.web.dto.response.post.search.SearchedPostsResponse;
import com.plana.infli.web.dto.response.post.single.SinglePostResponse;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    private final BoardRepository boardRepository;

    private final MemberRepository memberRepository;

    private final UniversityRepository universityRepository;

    private final S3Uploader s3Uploader;

    public boolean checkMemberAcceptedWritePolicy(String email) {
        MemberStatus memberStatus = findMemberBy(email).getStatus();

        return memberStatus.isPolicyAccepted();
    }

    @Transactional
    public void acceptWritePolicy(String email) {
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

        Post post = CreateNormalPostServiceRequest.toEntity(member, board, request);

        return postRepository.save(post).getId();
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

        checkIfInSameUniversity(member, board);

        checkIfAgreedOnWritePolicy(member);

        BoardType boardType = board.getBoardType();

        if (postType == RECRUITMENT) {
            throw new BadRequestException(POST_TYPE_NOT_ALLOWED);
        }

        checkWritePermission(member.getRole(), postType, boardType);
    }

    private void checkIfAgreedOnWritePolicy(Member member) {
        if (member.getStatus().isPolicyAccepted() == false) {
            throw new BadRequestException(WRITING_WITHOUT_POLICY_AGREEMENT_NOT_ALLOWED);
        }
    }

    private void checkWritePermission(Role role, PostType postType, BoardType boardType) {
        SubBoardType subBoardType = of(boardType, postType);

        if (subBoardType == null) {
            throw new BadRequestException(INVALID_BOARD_TYPE);
        }

        if (subBoardType.hasWritePermission(role) == false) {
            throw new AuthorizationFailedException();
        }
    }

    private void checkIfInSameUniversity(Member member, Board board) {
        if (member.getUniversity().equals(board.getUniversity()) == false) {
            throw new AuthorizationFailedException();
        }
    }

    @Transactional
    public Long createRecruitmentPost(CreateRecruitmentPostServiceRequest request) {

        Member member = findMemberWithUniversityBy(request.getEmail());

        Board board = findBoardWithUniversityBy(request.getBoardId());

        validateCreateRecruitmentPostRequest(member, board, request);

        Post post = toEntity(member, board, request, loadRecruitment(request));

        return postRepository.save(post).getId();
    }

    //TODO
    private void validateCreateRecruitmentPostRequest(Member member, Board board,
            CreateRecruitmentPostServiceRequest request) {

        if (request.getRecruitmentStartDate().isAfter(request.getRecruitmentEndDate())) {
            throw new BadRequestException(INVALID_RECRUITMENT_DATE);
        }

        checkIfInSameUniversity(member, board);

        checkIfAgreedOnWritePolicy(member);

        if (isRecruitmentBoard(board) == false) {
            throw new BadRequestException(BOARD_TYPE_IS_NOT_RECRUITMENT);
        }

        checkWritePermission(member.getRole(), RECRUITMENT, board.getBoardType());
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
    public PostImageUploadResponse uploadPostImages(Long postId, List<MultipartFile> multipartFiles, String email) {

        validateImages(multipartFiles);

        Member member = findMemberBy(email);

        Post post = findPostWithMemberBy(postId);

        checkThisMemberIsPostWriter(member, post);

        String directoryPath = "post/post_" + post.getId();

        String thumbnailImageURL = s3Uploader.uploadAsThumbnailImage(multipartFiles.get(0),
                directoryPath);

        List<String> originalImageUrls = new ArrayList<>();
        multipartFiles.forEach(multipartFile -> {
            String url = s3Uploader.uploadAsOriginalImage(multipartFile, directoryPath);
            originalImageUrls.add(url);
        });

        return PostImageUploadResponse.of(thumbnailImageURL, originalImageUrls);
    }

    private void checkThisMemberIsPostWriter(Member member, Post post) {
        if (post.getMember().equals(member) == false) {
            throw new AuthorizationFailedException();
        }
    }


    //TODO
    private  void validateImages(List<MultipartFile> files) {

        if (files.size() > 11) {
            throw new BadRequestException(MAX_IMAGES_EXCEEDED);
        }

        if (files.isEmpty()) {
            throw new BadRequestException(IMAGE_NOT_PROVIDED);
        }

        files.forEach(file -> {
            if (file.isEmpty()) {
                throw new BadRequestException(IMAGE_NOT_PROVIDED);
            }
        });
    }


    @Transactional
    public void editNormalPost(EditNormalPostServiceRequest request) {

        Member member = findMemberBy(request.getEmail());

        Post post = findPostWithMemberBy(request.getPostId());

        validateEditNormalPostRequest(post, member);

        edit(request, post);
    }

    private void validateEditNormalPostRequest(Post post, Member member) {

        if (isRecruitmentPost(post)) {
            throw new BadRequestException(POST_TYPE_NOT_ALLOWED);
        }

        checkThisMemberIsPostWriter(member, post);
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
            throw new BadRequestException(POST_TYPE_NOT_ALLOWED);
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

        increaseViewCount(post);

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

        return SearchedPostsResponse.of(posts, queryRequest);
    }


    public BoardPostsResponse loadPostsByBoard(LoadPostsByBoardServiceRequest request) {

        Member member = findMemberBy(request.getEmail());

        Board board = findBoardBy(request.getBoardId());

        checkIfInSameUniversity(member, board);

        validateTypes(request.getType(), board.getBoardType());

        PostQueryRequest queryRequest = postsByBoard(board, member, request, 20);

        List<BoardPost> posts = postRepository.loadPostsByBoard(queryRequest);

        return BoardPostsResponse.of(posts, queryRequest);
    }

    private void validateTypes(PostType postType, BoardType boardType) {
        SubBoardType subBoardType = of(boardType, postType);

        if (subBoardType == null) {
            throw new BadRequestException(INVALID_BOARD_TYPE);
        }
    }

    public boolean checkMemberHasWritePermission(Long boardId, String email, PostType postType) {

        Member member = findMemberWithUniversityBy(email);

        Board board = findBoardWithUniversityBy(boardId);

        checkIfInSameUniversity(member, board);

        checkWritePermission(member.getRole(), postType, board.getBoardType());

        return true;
    }



}
