package com.plana.infli.web.controller;

import com.plana.infli.service.MemberProfileService;
import com.plana.infli.web.dto.request.profile.MemberWithdrawalRequest;
import com.plana.infli.web.dto.request.profile.NicknameModifyRequest;
import com.plana.infli.web.dto.request.profile.PasswordConfirmRequest;
import com.plana.infli.web.dto.request.profile.PasswordModifyRequest;
import com.plana.infli.web.dto.response.profile.MemberProfileResponse;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/member/profile")
@RequiredArgsConstructor
public class MemberProfileController {

    private final MemberProfileService memberProfileService;

    /**
     * 내 정보 조회
     * */
    @Operation(description = "회원 정보 조회")
    @GetMapping
    public MemberProfileResponse profile(@AuthenticatedPrincipal String email) {
        return memberProfileService.getMemberProfile(email);
    }

    /**
     * 닉네임 변경
     */
    @Operation(description = "회원 닉네임 변경")
    @PostMapping("/nickname/modify")
    public Boolean nicknameModify(@RequestBody @Validated NicknameModifyRequest nicknameModifyRequest){
        return memberProfileService.modifyNickname(nicknameModifyRequest);
    }

    /**
     * 비빌번호 변경/탈퇴하기 시 비밀번호 확인
     * */
    @Operation(description = "비밀번호 확인 - 비밀번호 변경/탈퇴 시")
    @PostMapping("/password/confirm")
    public boolean passwordConfirm(@RequestBody @Validated PasswordConfirmRequest passwordConfirmRequest){
        return memberProfileService.checkPassword(passwordConfirmRequest);
    }

    /**
     * 비밀번호 변경
     * */
    @Operation(description = "회원 비밀번호 변경")
    @PostMapping("/password/modify")
    public boolean passwordModify(@RequestBody @Validated PasswordModifyRequest passwordModifyRequest){
        return memberProfileService.modifyPassword(passwordModifyRequest);
    }

    /**
     * 프로필 사진 변경
     * */
    @Operation(description = "회원 프로필 사진 변경")
    @PostMapping("/image/modify")
    public String profileImageModify(@RequestParam("file") MultipartFile profileImage, @AuthenticatedPrincipal String email){
        return memberProfileService.modifyProfileImage(email, profileImage, "member");
    }

    /**
     * 탈퇴하기
     */
    @Operation(description = "회원 탈퇴")
    @PostMapping("/withdrawal")
    public Boolean memberWithdrawal(@RequestBody MemberWithdrawalRequest memberWithdrawalRequest){
        return memberProfileService.deleteMember(memberWithdrawalRequest);
    }
}
