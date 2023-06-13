package com.plana.infli.web.controller;

import com.plana.infli.service.MemberProfileService;
import com.plana.infli.web.dto.request.profile.NicknameModifyRequest;
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

}
