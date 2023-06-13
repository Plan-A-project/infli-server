package com.plana.infli.web.controller;

import com.plana.infli.service.MemberProfileService;
import com.plana.infli.web.dto.response.profile.MemberProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

}
