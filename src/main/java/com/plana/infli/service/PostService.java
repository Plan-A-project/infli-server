package com.plana.infli.service;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.web.dto.request.post.GatherPostCreateRq;
import com.plana.infli.web.dto.request.post.PostCreateRq;
import com.plana.infli.web.dto.response.post.PostFindResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public ResponseEntity<Long> initPost(Long boardId, String type, String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(NotFoundException.MEMBER_NOT_FOUND));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시판이 없습니다. id=" + boardId));

        Post post = Post.builder()
                .title(null)
                .main(null)
                .type(PostType.of(type))
                .isPublished(false)
                .build();

        post.setBoard(board);
        post.setMember(member);
        postRepository.save(post);

        return ResponseEntity.ok().body(post.getId());
    }

    @Transactional
    public ResponseEntity<Long> createNormalPost(Long boardId, Long postId, String email, PostCreateRq requestDto) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(NotFoundException.MEMBER_NOT_FOUND));

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
    public ResponseEntity<Long> createGatherPost(Long boardId, Long postId, String email, GatherPostCreateRq requestDto) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(NotFoundException.MEMBER_NOT_FOUND));

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
    public ResponseEntity<Long> createNoticePost(Long boardId, Long postId, String email, PostCreateRq requestDto) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(NotFoundException.MEMBER_NOT_FOUND));

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

    public ResponseEntity<Boolean> isFistPost(String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(NotFoundException.MEMBER_NOT_FOUND));

        List<Post> posts = postRepository.findAllByMemberId(member.getId());
        if (posts.isEmpty()) {
            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.ok().body(false);
    }

    @Transactional
    public ResponseEntity<Void> deletePost(Long boardId, Long postId, String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(NotFoundException.MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));

        if (post.getBoard().getId() != boardId) {
            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
        }
        if (!post.getMember().equals(member)) {
            throw new IllegalArgumentException("잘못된 접근입니다");
        }

        postRepository.deleteById(postId);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<PostFindResponse> findPost(Long boardId, Long postId, String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(NotFoundException.MEMBER_NOT_FOUND));

        Post post = postRepository.findByIdAndIsPublishedTrue(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));

        if (post.getBoard().getId() != boardId) {
            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
        }

        // 글 작성자 본인이 아닐 경우 조회수 증가
        if (!post.getMember().equals(member)) {
            post.plusViewCount();
        }
        PostFindResponse postFindResponse = new PostFindResponse(post);

        return ResponseEntity.ok(postFindResponse);
    }

    public ResponseEntity<List<PostFindResponse>> findMyPost(String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(NotFoundException.MEMBER_NOT_FOUND));

        List<Post> posts = postRepository.findAllByMemberIdAndIsDeletedFalseAndIsPublishedTrueOrderByCreatedAtDesc(member.getId());

        List<PostFindResponse> postFindResponses = posts.stream().map(PostFindResponse::new).toList();

        return ResponseEntity.ok(postFindResponses);
    }

    public ResponseEntity<List<PostFindResponse>> findPostList(Long boardId) {

        List<Post> posts = postRepository.findAllByBoardIdAndIsDeletedFalseAndIsPublishedTrueOrderByCreatedAtDesc(boardId);

        List<PostFindResponse> postFindResponses = posts.stream().map(PostFindResponse::new).toList();

        return ResponseEntity.ok(postFindResponses);
    }
}
