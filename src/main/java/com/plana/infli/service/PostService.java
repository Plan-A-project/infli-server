package com.plana.infli.service;

import static com.plana.infli.domain.BoardType.*;
import static com.plana.infli.domain.Member.isAdmin;
import static com.plana.infli.domain.Post.*;
import static com.plana.infli.domain.PostType.*;
import static com.plana.infli.domain.editor.post.PostEditor.editPost;
import static com.plana.infli.exception.custom.BadRequestException.INVALID_REQUIRED_PARAM;
import static com.plana.infli.exception.custom.NotFoundException.*;
import static com.plana.infli.web.dto.response.post.my.MyPostsResponse.loadMyPostsResponse;
import static java.lang.Integer.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.BoardType;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.domain.University;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.post.edit.EditPostRequest.RecruitmentInfo;
import com.plana.infli.web.dto.request.post.view.board.LoadPostsByBoardServiceRequest;
import com.plana.infli.web.dto.request.post.initialize.PostInitializeServiceRequest;
import com.plana.infli.web.dto.request.post.edit.EditPostServiceRequest;
import com.plana.infli.web.dto.request.post.search.SearchPostsByKeywordServiceRequest;
import com.plana.infli.web.dto.response.post.BoardPostDTO;
import com.plana.infli.web.dto.response.post.PostsByBoardResponse;
import com.plana.infli.web.dto.response.post.my.MyPost;
import com.plana.infli.web.dto.response.post.my.MyPostsResponse;
import com.plana.infli.web.dto.response.post.search.SearchedPost;
import com.plana.infli.web.dto.response.post.search.SearchedPostsResponse;
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
    public Long createInitialPost(PostInitializeServiceRequest request, String email) {

        Member member = findMemberWithUniversity(email);

        Board board = findBoardWithUniversity(request);

        checkMemberAndBoardIsInSameUniversity(member, board);

        validateTypes(request.getPostType(), board.getBoardType());

        Post savedPost = postRepository.save(initializePost(member, board, request.getPostType()));

        return savedPost.getId();
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

    private Member findMemberWithUniversity(String email) {
        return memberRepository.findActiveMemberWithUniversityBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    private Board findBoardWithUniversity(PostInitializeServiceRequest request) {
        return boardRepository.findActiveBoardWithUniversityBy(request.getBoardId())
                .orElseThrow(() -> new NotFoundException(BOARD_NOT_FOUND));
    }

    private void checkMemberAndBoardIsInSameUniversity(Member member, Board board) {
        if (member.getUniversity().equals(board.getUniversity()) == false) {
            throw new AuthorizationFailedException();
        }
    }

    @Transactional
    public void edit(EditPostServiceRequest request, String email) {

        Member member = findMemberBy(email);

        Post post = findNotDeletedPost(request.getPostId());

        validateEditRequest(request, post, member);

        editPost(request, post);
    }

    private void validateEditRequest(EditPostServiceRequest request, Post post, Member member) {

        checkThisMemberIsPostWriter(member, post);

        if (post.getPostType() == RECRUITMENT) {
            validateRecruitmentRequest(request);
        }
    }

    private void validateRecruitmentRequest(EditPostServiceRequest request) {
        RecruitmentInfo recruitmentInfo = request.getRecruitmentInfo();

        if (recruitmentInfo == null || anyNullExists(recruitmentInfo)) {
            throw new BadRequestException(INVALID_REQUIRED_PARAM);
        }
    }

    private boolean anyNullExists(RecruitmentInfo recruitmentInfo) {
        return recruitmentInfo.getStartDate() == null || recruitmentInfo.getEndDate() == null
                || recruitmentInfo.getCompanyName() == null;
    }

    private Post findNotDeletedPost(Long postId) {
        return postRepository.findNotDeletedPostWithMemberBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
    }

    private void checkThisMemberIsPostWriter(Member member, Post post) {
        if (post.getMember().equals(member) == false) {
            throw new AuthorizationFailedException();
        }
    }


    @Transactional
    public void deletePost(Long postId, String email) {

        Member member = findMemberBy(email);

        Post post = findNotDeletedPost(postId);

        validateDeleteRequest(member, post);

        postRepository.delete(post);
    }

    private void validateDeleteRequest(Member member, Post post) {

        if (isAdmin(member)) {
            return;
        }

        checkThisMemberIsPostWriter(member, post);
    }

    @Transactional
    public SinglePostResponse findSinglePost(Long postId, String email) {

        Member member = memberRepository.findActiveMemberBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        Post post = postRepository.findActivePostWithBoardBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));

        checkMemberAndPostIsInSameUniversity(member, post);

        //TODO 동시성 고려 필요
        post.plusViewCount();

        return postRepository.loadSinglePostResponse(post, member);
    }

    private void checkMemberAndPostIsInSameUniversity(Member member, Post post) {
        if (universityRepository.isMemberAndPostInSameUniversity(member, post) == false) {
            throw new AuthorizationFailedException();
        }
    }


    public MyPostsResponse findMyPosts(String email, String page) {

        Member member = memberRepository.findActiveMemberBy(email)
                .orElseThrow((() -> new NotFoundException(MEMBER_NOT_FOUND)));

        List<MyPost> posts = postRepository.loadMyPosts(member, loadPage(page));

        return loadMyPostsResponse(loadPage(page), posts);
    }

    //TODO
    private int loadPage(String page) {
        try {
            return Math.max(1, parseInt(page));
        } catch (NumberFormatException e) {
            return 1;
        }
    }




    private Board findBoardBy(Long boardId) {
        return boardRepository.findActiveBoardBy(boardId)
                .orElseThrow(() -> new NotFoundException(BOARD_NOT_FOUND));
    }






    private int loadPage(Integer page) {
        return page != null ? Math.max(1, page) : 1;

    }



    public SearchedPostsResponse searchPosts(SearchPostsByKeywordServiceRequest request,
            String email) {

//        University university = universityRepository.findByMemberEmail(email)
//                .orElseThrow(() -> new NotFoundException(UNIVERSITY_NOT_FOUND));
//
//        KeywordSearch keywordSearch = createRequestEntity(request, university);
//
//        List<SearchedPost> posts = postRepository.searchPostByKeyWord(keywordSearch);


        return null;
    }

    private KeywordSearch createRequestEntity(SearchPostsByKeywordServiceRequest request,
            University university) {


        int page;
        try {
            page = Math.max(parseInt(request.getPage()), 1);
        } catch (NumberFormatException e) {
            page = 1;
        }

        return KeywordSearch.create(university, request.getKeyword(), page);
    }


    public PostsByBoardResponse loadPostsByBoard(Long boardId,
            LoadPostsByBoardServiceRequest request, String email) {

        Board board = findBoardBy(boardId);

        Member member = findMemberBy(email);

        checkMemberAndBoardIsInSameUniversity(member, board);

        request.validatePageRequest();

        validateTypes(request.getType(), board.getBoardType());

        List<BoardPostDTO> posts = postRepository.loadPostsByBoard(board, request);

        return PostsByBoardResponse.loadResponse(posts, request.getPage(), board);
    }




    @Getter
    public static class KeywordSearch {

        private static final int SIZE_PER_SEARCH = 50;

        private University university;

        private String keyword;

        private int page;

        private int size = SIZE_PER_SEARCH;

        @Builder
        private KeywordSearch(University university, String keyword, int page) {
            this.university = university;
            this.keyword = keyword;
            this.page = page;
        }

        public static KeywordSearch create(University university, String keyword,
                int page) {

            return KeywordSearch.builder()
                    .university(university)
                    .keyword(keyword)
                    .page(page)
                    .build();
        }

        public long loadOffset() {
            return (long) (page - 1) * size;
        }
    }


    //    public ResponseEntity<Long> initPost(Long boardId, String type, String email) {
//
//        Member member = findMember(email);
//
//        Board board = boardRepository.findById(boardId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 게시판이 없습니다. id=" + boardId));
//
//        Post post = builder()
//                .title(null)
//                .content(null)
//                .type(of(type))
//                .isPublished(false)
//                .build();
//
//        post.setBoard(board);
//        post.setMember(member);
//        postRepository.save(post);
//
//        return ResponseEntity.ok().body(post.getId());
//    }

//    @Transactional
//    public ResponseEntity<Long> createNormalPost(Long boardId, Long postId, String email, PostCreateRq requestDto) {
//
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
//
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));
//
//        if (post.getBoard().getId() != boardId) {
//            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
//        }
//        if (!post.getPostType().equals(NORMAL)) {
//            throw new IllegalArgumentException("해당 게시글은 일반글이 아닙니다");
//        }
//        if (!post.getMember().equals(member)) {
//            throw new IllegalArgumentException("잘못된 접근입니다");
//        }
//
//        post.publish(requestDto);
//        return ResponseEntity.ok().body(post.getId());
//    }

//    @Transactional
//    public ResponseEntity<Long> createGatherPost(Long boardId, Long postId, String email, GatherPostCreateRq requestDto) {
//
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
//
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));
//
//        if (post.getBoard().getId() != boardId) {
//            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
//        }
//        if (!post.getPostType().equals(RECRUITMENT)) {
//            throw new IllegalArgumentException("해당 게시글은 모집글이 아닙니다");
//        }
//        if (!post.getMember().equals(member)) {
//            throw new IllegalArgumentException("잘못된 접근입니다");
//        }
//
//        post.publish(requestDto);
//        return ResponseEntity.ok().body(post.getId());
//    }
//
//    @Transactional
//    public ResponseEntity<Long> createNoticePost(Long boardId, Long postId, String email, PostCreateRq requestDto) {
//
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
//
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));
//
//        if (post.getBoard().getId() != boardId) {
//            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
//        }
//        if (!post.getPostType().equals(NOTICE)) {
//            throw new IllegalArgumentException("해당 게시글은 공지글이 아닙니다");
//        }
//        if (!post.getMember().equals(member)) {
//            throw new IllegalArgumentException("잘못된 접근입니다");
//        }
//
//        post.publish(requestDto);
//        return ResponseEntity.ok().body(post.getId());
//    }

}
