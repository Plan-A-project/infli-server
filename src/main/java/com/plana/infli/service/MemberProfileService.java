package com.plana.infli.service;

import com.plana.infli.domain.Member;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.web.dto.response.profile.MemberProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberProfileService {

    private final MemberRepository memberRepository;

    public MemberProfileResponse getMemberProfile(String email) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(email + " is not found"));

        return new MemberProfileResponse(member.getNickname(), member.getRole(), member.getEmail());
    }

}
