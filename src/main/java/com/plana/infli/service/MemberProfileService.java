package com.plana.infli.service;

import com.plana.infli.domain.Member;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.web.dto.request.profile.MemberWithdrawalRequest;
import com.plana.infli.web.dto.request.profile.NicknameModifyRequest;
import com.plana.infli.web.dto.request.profile.PasswordConfirmRequest;
import com.plana.infli.web.dto.request.profile.PasswordModifyRequest;
import com.plana.infli.web.dto.response.profile.MemberProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Transactional(readOnly = false)
    public boolean modifyNickname(NicknameModifyRequest nicknameModifyRequest) {
        Member member = memberRepository.findByEmail(nicknameModifyRequest.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException(nicknameModifyRequest.getEmail() + "is not found"));

        member.changeNickname(nicknameModifyRequest.getAfterNickname());
        return true;
    }

    public boolean checkPassword(PasswordConfirmRequest passwordConfirmRequest) {
        Member member = memberRepository.findByEmail(passwordConfirmRequest.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException(
                passwordConfirmRequest.getEmail() + " is not found"));

        if (member.getPassword().equals(new BCryptPasswordEncoder().encode(passwordConfirmRequest.getPassword()))) {
            return true;
        } else {
            return false;
        }
    }

    @Transactional(readOnly = false)
    public boolean modifyPassword(PasswordModifyRequest passwordModifyRequest) {
        String encodeAfterPassword = new BCryptPasswordEncoder().encode(passwordModifyRequest.getNewPassword());

        Member member = memberRepository.findByEmail(passwordModifyRequest.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException(passwordModifyRequest.getEmail() + " is not found"));

        member.changePassword(encodeAfterPassword);
        return true;
    }

    @Transactional(readOnly = false)
    public boolean deleteMember(MemberWithdrawalRequest memberWithdrawalRequest){
        Member member = memberRepository.findByEmail(memberWithdrawalRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException(
                memberWithdrawalRequest.getEmail() + " is not found"));

        member.deleteMember();
        return true;
    }


}
