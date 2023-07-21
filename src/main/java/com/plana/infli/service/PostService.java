package com.plana.infli.service;

import static com.plana.infli.exception.custom.NotFoundException.*;
import static java.lang.Integer.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.domain.University;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.ImageRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.post.GatherPostCreateRq;
import com.plana.infli.web.dto.request.post.PostCreateRq;
import com.plana.infli.web.dto.request.post.search.service.SearchPostsByKeywordServiceRequest;
import com.plana.infli.web.dto.response.post.search.PostSearchResponse;
import com.plana.infli.web.dto.response.post.search.SearchedPost;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    private final ImageRepository imageRepository;

    private final UniversityRepository universityRepository;

    public ResponseEntity initPost(Long boardId, String type, String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시판이 없습니다. id=" + boardId));

        Post post = Post.builder()
                .title(null)
                .main(null)
                .type(PostType.of(type))
                .isPublished(false)
                .viewCount(0)
                .build();

        post.setBoard(board);
        post.setMember(member);
        postRepository.save(post);

        return ResponseEntity.ok().body(post.getId());
    }

    @Transactional
    public ResponseEntity createNormalPost(Long boardId, Long postId, String email,
            PostCreateRq requestDto) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));

        if (post.getBoard().getId() != boardId) {
            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
        }
        if (!post.getType().equals(PostType.NORMAL)) {
            throw new IllegalArgumentException("해당 게시글은 일반글이 아닙니다");
        }
        if (!post.getMember().equals(member)) {
            throw new IllegalArgumentException("잘못된 접근입니다");
        }

        post.publish(requestDto);
        return ResponseEntity.ok().body(post.getId());
    }

    @Transactional
    public ResponseEntity createGatherPost(Long boardId, Long postId, String email,
            GatherPostCreateRq requestDto) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));

        if (post.getBoard().getId() != boardId) {
            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
        }
        if (!post.getType().equals(PostType.GATHER)) {
            throw new IllegalArgumentException("해당 게시글은 모집글이 아닙니다");
        }
        if (!post.getMember().equals(member)) {
            throw new IllegalArgumentException("잘못된 접근입니다");
        }

        post.publish(requestDto);
        return ResponseEntity.ok().body(post.getId());
    }

    @Transactional
    public ResponseEntity createNoticePost(Long boardId, Long postId, String email,
            PostCreateRq requestDto) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));

        if (post.getBoard().getId() != boardId) {
            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
        }
        if (!post.getType().equals(PostType.NOTICE)) {
            throw new IllegalArgumentException("해당 게시글은 공지글이 아닙니다");
        }
        if (!post.getMember().equals(member)) {
            throw new IllegalArgumentException("잘못된 접근입니다");
        }

        post.publish(requestDto);
        return ResponseEntity.ok().body(post.getId());
    }

    public ResponseEntity isFistPost(String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        List<Post> posts = postRepository.findAllByMemberId(member.getId());
        if (posts.isEmpty()) {
            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.ok().body(false);
    }

    @Transactional
    public ResponseEntity deletePost(Long boardId, Long postId, String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));

        if (post.getBoard().getId() != boardId) {
            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
        }
        if (!post.getMember().equals(member)) {
            throw new IllegalArgumentException("잘못된 접근입니다");
        }

        postRepository.deleteById(postId);
        return ResponseEntity.ok().body(post.getId());
    }

    public ResponseEntity findPost(Long boardId, Long postId) {
        return null;
    }

    public ResponseEntity findMyPost(Long boardId, Long postId, String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        return null;
    }

    public PostSearchResponse searchPosts(SearchPostsByKeywordServiceRequest request,
            String email) {

        University university = universityRepository.findByMemberEmail(email)
                .orElseThrow(() -> new NotFoundException(UNIVERSITY_NOT_FOUND));

        KeywordSearch keywordSearch = createRequestEntity(request, university);

        List<SearchedPost> posts = postRepository.searchPostByKeyWord(keywordSearch);


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
}
