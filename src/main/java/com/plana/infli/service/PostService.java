package com.plana.infli.service;

import static com.plana.infli.domain.type.BoardType.*;
import static com.plana.infli.domain.Member.isAdmin;
import static com.plana.infli.domain.type.PostType.*;
import static com.plana.infli.domain.editor.PostEditor.delete;
import static com.plana.infli.domain.editor.PostEditor.edit;
import static com.plana.infli.domain.editor.PostEditor.increaseViewCount;
import static com.plana.infli.domain.embedded.post.Recruitment.*;
import static com.plana.infli.infra.exception.custom.BadRequestException.BOARD_TYPE_IS_NOT_RECRUITMENT;
import static com.plana.infli.infra.exception.custom.BadRequestException.IMAGE_NOT_PROVIDED;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_BOARD_TYPE;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_RECRUITMENT_DATE;
import static com.plana.infli.infra.exception.custom.BadRequestException.MAX_IMAGES_EXCEEDED;
import static com.plana.infli.infra.exception.custom.BadRequestException.POST_TYPE_NOT_ALLOWED;
import static com.plana.infli.infra.exception.custom.BadRequestException.WRITING_WITHOUT_POLICY_AGREEMENT_NOT_ALLOWED;
import static com.plana.infli.infra.exception.custom.NotFoundException.BOARD_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.MEMBER_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.POST_NOT_FOUND;
import static com.plana.infli.web.dto.request.post.view.PostQueryRequest.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.type.BoardType;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.type.Role;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.type.PostType;
import com.plana.infli.domain.embedded.post.Recruitment;
import com.plana.infli.infra.exception.custom.AuthorizationFailedException;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.aop.retry.Retry;
import com.plana.infli.service.utils.S3Uploader;
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
import jakarta.annotation.Nullable;
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



    @Transactional
    //TODO 이메일 인증 받은 사람만 글 작성 가능하도록  추후 변경 필요
    public Long createNormalPost(CreateNormalPostServiceRequest request) {

        Member member = findMemberWithUniversityBy(request.getUsername());

        Board board = findBoardWithUniversityBy(request.getBoardId());

        validateCreateNormalPostRequest(member, board, request.getPostType());

        Post post = CreateNormalPostServiceRequest.toEntity(member, board, request);

        return postRepository.save(post).getId();
    }

    private Member findMemberWithUniversityBy(String username) {
        return memberRepository.findActiveMemberWithUniversityBy(username)
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
        if (member.getBasicCredentials().isPolicyAccepted() == false) {
            throw new BadRequestException(WRITING_WITHOUT_POLICY_AGREEMENT_NOT_ALLOWED);
        }
    }

    private void checkWritePermission(Role role, PostType postType, BoardType boardType) {
        SubBoardType subBoardType = SubBoardType.of(boardType, postType);

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

        Member member = findMemberWithUniversityBy(request.getUsername());

        Board board = findBoardWithUniversityBy(request.getBoardId());

        validateCreateRecruitmentPostRequest(member, board, request);

        Post post = request.toEntity(member, board, loadRecruitment(request));

        return postRepository.save(post).getId();
    }

    //TODO
    private void validateCreateRecruitmentPostRequest(Member member, Board board, CreateRecruitmentPostServiceRequest request) {

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
    public PostImageUploadResponse uploadPostImages(Long postId, List<MultipartFile> multipartFiles, String username) {

        validateImages(multipartFiles);

        Member member = findMemberBy(username);

        Post post = findPostWithMemberBy(postId);

        checkThisMemberIsPostWriter(member, post);

        String directoryPath = "posts/" + post.getId();

        String thumbnailImageURL = s3Uploader.uploadAsThumbnailImage(multipartFiles.get(0),
                directoryPath);

        List<String> originalImageUrls = new ArrayList<>();
        multipartFiles.forEach(multipartFile -> {
            String url = s3Uploader.uploadAsOriginalImage(multipartFile, directoryPath);
            originalImageUrls.add(url);
        });

        return PostImageUploadResponse.of(thumbnailImageURL, originalImageUrls);
    }

    private Member findMemberBy(String username) {
        return memberRepository.findActiveMemberBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    private void checkThisMemberIsPostWriter(Member member, Post post) {
        if (post.getMember().equals(member) == false) {
            throw new AuthorizationFailedException();
        }
    }


    //TODO
    private  void validateImages(List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            throw new BadRequestException(IMAGE_NOT_PROVIDED);
        }

        if (files.size() > 11) {
            throw new BadRequestException(MAX_IMAGES_EXCEEDED);
        }

        files.forEach(file -> {
            if (file.isEmpty()) {
                throw new BadRequestException(IMAGE_NOT_PROVIDED);
            }
        });
    }


    @Transactional
    public void editNormalPost(EditNormalPostServiceRequest request) {

        Member member = findMemberBy(request.getUsername());

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

        Member member = findMemberBy(request.getUsername());

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
    public void deletePost(Long postId, String username) {

        Member member = findMemberBy(username);

        Post post = findPostWithMemberBy(postId);

        validateDeleteRequest(member, post);

        delete(post);
    }

    private Post findPostWithMemberBy(Long postId) {
        return postRepository.findActivePostWithMemberBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
    }

    @Transactional
    @Retry
    public SinglePostResponse loadSinglePost(Long postId, String username) {

        Member member = findMemberBy(username);

        Post post = findPostWithLockBy(postId);

        checkMemberAndPostIsInSameUniversity(member, post);

        increaseViewCount(post);

        PostQueryRequest request = singlePost(post, member);

        return postRepository.loadSinglePostResponse(request);
    }

    private void validateDeleteRequest(Member member, Post post) {

        if (isAdmin(member)) {
            return;
        }
        checkThisMemberIsPostWriter(member, post);
    }

    private Post findPostWithLockBy(Long postId) {
        return postRepository.findActivePostWithOptimisticLock(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
    }

    private void checkMemberAndPostIsInSameUniversity(Member member, Post post) {
        if (universityRepository.isMemberAndPostInSameUniversity(member, post) == false) {
            throw new AuthorizationFailedException();
        }
    }


    public MyPostsResponse loadMyPosts(String username, Integer page) {

        Member member = findMemberBy(username);

        PostQueryRequest request = myPosts(member, page, 20);

        List<MyPost> posts = postRepository.loadMyPosts(request);

        return MyPostsResponse.of(request, posts);
    }



    private Board findBoardBy(Long boardId) {
        return boardRepository.findActiveBoardBy(boardId)
                .orElseThrow(() -> new NotFoundException(BOARD_NOT_FOUND));
    }


    public SearchedPostsResponse searchPostsByKeyword(SearchPostsByKeywordServiceRequest request) {

        Member member = findMemberWithUniversityBy(request.getUsername());

        PostQueryRequest queryRequest = searchByKeyword(member, request, 20);

        List<SearchedPost> posts = postRepository.searchPostByKeyWord(queryRequest);

        return SearchedPostsResponse.of(posts, queryRequest);
    }


    public BoardPostsResponse loadPostsByBoard(LoadPostsByBoardServiceRequest request) {

        Board board = findBoardBy(request.getBoardId());

        Member member = findMemberBy(request.getUsername());

        checkIfInSameUniversity(member, board);

        validateTypes(request.getType(), board.getBoardType());

        PostQueryRequest queryRequest = postsByBoard(board, member, request);

        List<BoardPost> posts = postRepository.loadPostsByBoard(queryRequest);

        return BoardPostsResponse.of(posts, queryRequest);
    }

    private void validateTypes(PostType postType, BoardType boardType) {
        SubBoardType subBoardType = SubBoardType.of(boardType, postType);

        if (subBoardType == null) {
            throw new BadRequestException(INVALID_BOARD_TYPE);
        }
    }

    public boolean checkMemberHasWritePermission(Long boardId, String username, PostType postType) {

        Member member = findMemberWithUniversityBy(username);

        Board board = findBoardWithUniversityBy(boardId);

        checkIfInSameUniversity(member, board);

        checkWritePermission(member.getRole(), postType, board.getBoardType());

        return true;
    }
}
