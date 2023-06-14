package com.plana.infli.web.controller;

import com.plana.infli.service.MemberProfileService;
import com.plana.infli.web.dto.request.profile.MemberWithdrawalRequest;
import com.plana.infli.web.dto.request.profile.NicknameModifyRequest;
import com.plana.infli.web.dto.request.profile.PasswordConfirmRequest;
import com.plana.infli.web.dto.request.profile.PasswordModifyRequest;
import com.plana.infli.web.dto.response.profile.MemberProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member/profile")
@RequiredArgsConstructor
public class MemberProfileController {

    private final MemberProfileService memberProfileService;

    /**
     * 내 정보 조회
     * */
    @GetMapping("/{email}")
    public MemberProfileResponse profile(@PathVariable String email) {
        return memberProfileService.getMemberProfile(email);
    }

    /**
     * 닉네임 변경
     */
    @PostMapping("/nickname/modify")
    public boolean nicknameModify(@RequestBody NicknameModifyRequest nicknameModifyRequest){
        return memberProfileService.modifyNickname(nicknameModifyRequest);
    }

    /**
     * 비빌번호 변경/탈퇴하기 시 비밀번호 확인
     * */
    @PostMapping("/password/confirm")
    public boolean passwordConfirm(@RequestBody PasswordConfirmRequest passwordConfirmRequest){
        return memberProfileService.checkPassword(passwordConfirmRequest);
    }

    /**
     * 비밀번호 변경
     * */
    @PostMapping("/password/modify")
    public boolean passwordModify(@RequestBody PasswordModifyRequest passwordModifyRequest){
        return memberProfileService.modifyPassword(passwordModifyRequest);
    }

    /**
     * 탈퇴하기
     */
    @PostMapping("/withdrawal")
    public boolean memberWithdrawal(@RequestBody MemberWithdrawalRequest memberWithdrawalRequest){
        return memberProfileService.deleteMember(memberWithdrawalRequest);
    }
}
