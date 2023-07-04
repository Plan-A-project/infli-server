package com.plana.infli.service;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.post.ImageRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.web.dto.request.post.GatherPostCreateRq;
import com.plana.infli.web.dto.request.post.PostCreateRq;
import jakarta.servlet.ServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final ImageRepository imageRepository;

    public ResponseEntity initPost(Long boardId, String type, ServletRequest request) {

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
        //post.setMember
        postRepository.save(post);

        return ResponseEntity.ok().body(post.getId());
    }

    @Transactional
    public ResponseEntity createNormalPost(Long boardId, Long postId, ServletRequest request, PostCreateRq requestDto) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));

        if (post.getBoard().getId() != boardId) {
            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
        }
        if (!post.getType().equals(PostType.NORMAL)) {
            throw new IllegalArgumentException("해당 게시글은 일반글이 아닙니다");
        }

        post.publish(requestDto);
        return ResponseEntity.ok().body(post.getId());
    }

    @Transactional
    public ResponseEntity createGatherPost(Long boardId, Long postId, ServletRequest request, GatherPostCreateRq requestDto) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));

        if (post.getBoard().getId() != boardId) {
            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
        }
        if (!post.getType().equals(PostType.GATHER)) {
            throw new IllegalArgumentException("해당 게시글은 모집글이 아닙니다");
        }

        post.publish(requestDto);
        return ResponseEntity.ok().body(post.getId());
    }

    @Transactional
    public ResponseEntity createNoticePost(Long boardId, Long postId, ServletRequest request, PostCreateRq requestDto) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));

        if (post.getBoard().getId() != boardId) {
            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
        }
        if (!post.getType().equals(PostType.NOTICE)) {
            throw new IllegalArgumentException("해당 게시글은 공지글이 아닙니다");
        }

        post.publish(requestDto);
        return ResponseEntity.ok().body(post.getId());
    }

    public ResponseEntity isFistPost(ServletRequest request) {
        return null;
    }

    @Transactional
    public ResponseEntity deletePost(Long boardId, Long postId, ServletRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));

        if (post.getBoard().getId() != boardId) {
            throw new IllegalArgumentException("해당 게시글은 해당 게시판의 글이 아닙니다");
        }

        postRepository.deleteById(postId);
        return ResponseEntity.ok().body(post.getId());
    }
}
