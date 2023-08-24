package com.plana.infli.service;

import static com.plana.infli.domain.PostLike.*;
import static com.plana.infli.infra.exception.custom.BadRequestException.POST_LIKE_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.ConflictException.ALREADY_PRESSED_LIKE_ON_THIS_POST;
import static com.plana.infli.infra.exception.custom.NotFoundException.MEMBER_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.POST_NOT_FOUND;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostLike;
import com.plana.infli.infra.exception.custom.AuthorizationFailedException;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.postlike.PostLikeRepository;
import com.plana.infli.repository.university.UniversityRepository;
import java.util.List;
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

        if (postLikeRepository.existsByPostAndMember(post, member)) {
            throw new ConflictException(ALREADY_PRESSED_LIKE_ON_THIS_POST);
        }
    }


    @Transactional
    public void cancelPostLike(String username, Long postId) {

        Member member = findMemberBy(username);

        Post post = findPostBy(postId);

        List<PostLike> postLikes = findAllPostLikeBy(post, member);

        postLikeRepository.deleteAllInBatch(postLikes);
    }

    private List<PostLike> findAllPostLikeBy(Post post, Member member) {
        List<PostLike> postLikes = postLikeRepository.findAllByPostAndMember(post, member);

        if (postLikes.isEmpty()) {
            throw new BadRequestException(POST_LIKE_NOT_FOUND);
        }
        return postLikes;
    }
}
