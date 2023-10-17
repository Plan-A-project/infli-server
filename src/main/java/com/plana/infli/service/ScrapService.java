package com.plana.infli.service;

import static com.plana.infli.domain.Scrap.*;
import static com.plana.infli.infra.exception.custom.ConflictException.SCRAP_ALREADY_EXISTS;
import static com.plana.infli.infra.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.Scrap;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.scrap.ScrapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScrapService {

    private final MemberRepository memberRepository;

    private final ScrapRepository scrapRepository;

    private final PostRepository postRepository;

    public void createScrap(String username, Long postId) {

        Member member = findMemberBy(username);

        Post post = findPostBy(postId);

        checkScrapDuplicate(post, member);

        Scrap scrap = create(post, member);

        scrapRepository.save(scrap);
    }

    public void cancelScrap(String username, Long postId) {

    }

    private void checkScrapDuplicate(Post post, Member member) {
        if (scrapRepository.existsByPostAndMember(post, member)) {
            throw new ConflictException(SCRAP_ALREADY_EXISTS);
        }
    }


    private Member findMemberBy(String username) {
        return memberRepository.findActiveMemberBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }


    private Post findPostBy(Long postId) {
        return postRepository.findPostById(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));
    }
}
