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
    public void pressPostLike(String username, Long postId) {

        Member member = findMemberBy(username);

        Post post = findPostBy(postId);

        validatePressPostLikeRequest(member, post);

        postLikeRepository.save(create(post, member));
    }

    private Member findMemberBy(String username) {
        return memberRepository.findActiveMemberBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    private Post findPostBy(Long postId) {
        return postRepository.findActivePostBy(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
    }

    private void validatePressPostLikeRequest(Member member, Post post) {
        if (universityRepository.isMemberAndPostInSameUniversity(member, post) == false) {
            throw new AuthorizationFailedException();
        }
    }


    @Transactional
    public void cancelPostLike(String username, Long postId) {

        Member member = findMemberBy(username);

        Post post = findPostBy(postId);

        PostLike postLike = findPostLikeBy(post, member);

        postLikeRepository.delete(postLike);
    }

    private PostLike findPostLikeBy(Post post, Member member) {
        return postLikeRepository.findByPostAndMember(post, member)
                .orElseThrow(() -> new BadRequestException(POST_LIKE_NOT_FOUND));
    }
}
