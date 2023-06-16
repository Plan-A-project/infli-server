package com.plana.infli.service;

import static com.plana.infli.exception.custom.NotFoundException.MEMBER_NOT_FOUND;

import com.plana.infli.domain.Member;
import com.plana.infli.exception.custom.AuthenticationFailedException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberUtil {


    private final MemberRepository memberRepository;

    public Member getContextMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AuthenticationFailedException();
        }

        String nickname = authentication.getName();

        Member member = memberRepository.findActiveMemberByNickname(nickname);

        if (member == null) {
            throw new NotFoundException(MEMBER_NOT_FOUND);
        }

        return member;
    }
}
