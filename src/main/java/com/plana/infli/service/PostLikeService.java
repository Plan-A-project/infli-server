package com.plana.infli.service;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostLike;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.postlike.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ResponseEntity<Long> createPostLike(Long postId, String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(NotFoundException.MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));

        PostLike postLike = PostLike.builder()
                .post(post)
                .member(member)
                .build();

        post.plusLikeCount();
        postLikeRepository.save(postLike);

        return ResponseEntity.ok().body(postLike.getId());
    }


    @Transactional
    public ResponseEntity<Void> deletePostLike(Long postId, String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(NotFoundException.MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + postId));

        PostLike postLike = postLikeRepository.findByPostIdAndMemberId(post.getId(), member.getId());

        post.minusLikeCount();
        postLikeRepository.deleteById(postLike.getId());

        return ResponseEntity.ok().build();
    }

}
