package com.plana.infli.service;

import com.plana.infli.domain.Member;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.web.dto.request.profile.NicknameModifyRequest;
import com.plana.infli.web.dto.response.profile.MemberProfileResponse;
import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberProfileService {

    private final MemberRepository memberRepository;

    public MemberProfileResponse getMemberProfile(String email) {
//        Member member = memberRepository.findByEmail(email)
//            .orElseThrow(() -> new UsernameNotFoundException(email + " is not found"));
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException());

        return new MemberProfileResponse(member.getNickname(), member.getRole(), member.getEmail());
    }

    @Transactional(readOnly = false)
    public boolean modifyNickname(NicknameModifyRequest nicknameModifyRequest) {
//        Member member = memberRepository.findByNickname(nicknameModifyRequest.getCurrentNickname())
//            .orElseThrow(() -> new UsernameNotFoundException(nicknameModifyRequest.getCurrentNickname() + "is not found"));
        Member member = memberRepository.findByEmail(nicknameModifyRequest.getEmail())
            .orElseThrow(() -> new IllegalArgumentException());

        member.changeNickname(nicknameModifyRequest.getAfterNickname());
        return true;
    }


}
