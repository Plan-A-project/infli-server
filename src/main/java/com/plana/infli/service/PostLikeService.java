package com.plana.infli.service;

import static com.plana.infli.domain.PostLike.*;
import static com.plana.infli.exception.custom.BadRequestException.POST_LIKE_NOT_FOUND;
import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostLike;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.postlike.PostLikeRepository;
import com.plana.infli.repository.university.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostRepository postRepository;

    private final PostLikeRepository postLikeRepository;

    private final MemberRepository memberRepository;

    private final UniversityRepository universityRepository;

    @Transactional
    public void createPostLike(Long postId, String email) {

        Member member = memberRepository.findActiveMemberBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        Post post = postRepository.findActivePostBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));

        validateCreateRequest(member, post);

        postLikeRepository.save(create(post, member));
    }

    private void validateCreateRequest(Member member, Post post) {
        if (universityRepository.isMemberAndPostInSameUniversity(member, post) == false) {
            throw new AuthorizationFailedException();
        }
    }


    @Transactional
    public void cancelPostLike(Long postId, String email) {
        Member member = memberRepository.findActiveMemberBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        Post post = postRepository.findActivePostBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));

        PostLike postLike = postLikeRepository.findByPostAndMember(post, member)
                .orElseThrow(() -> new BadRequestException(POST_LIKE_NOT_FOUND));

        postLikeRepository.delete(postLike);
    }
}
