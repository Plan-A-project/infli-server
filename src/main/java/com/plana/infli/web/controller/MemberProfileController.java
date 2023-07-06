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
    public ResponseEntity<MemberProfileResponse> profile(@AuthenticatedPrincipal String email) {
        return ResponseEntity.ok().body(memberProfileService.getMemberProfile(email));
    }

    /**
     * 닉네임 변경
     */
    @Operation(description = "회원 닉네임 변경")
    @PostMapping("/nickname/modify")
    public ResponseEntity<Boolean> nicknameModify(@RequestBody @Validated NicknameModifyRequest nicknameModifyRequest){
        return ResponseEntity.ok().body(memberProfileService.modifyNickname(nicknameModifyRequest));
    }

    /**
     * 비빌번호 변경/탈퇴하기 시 비밀번호 확인
     * */
    @Operation(description = "비밀번호 확인 - 비밀번호 변경/탈퇴 시")
    @PostMapping("/password/confirm")
    public ResponseEntity<Boolean> passwordConfirm(@RequestBody @Validated PasswordConfirmRequest passwordConfirmRequest){
        return ResponseEntity.ok().body(memberProfileService.checkPassword(passwordConfirmRequest));
    }

    /**
     * 비밀번호 변경
     * */
    @Operation(description = "회원 비밀번호 변경")
    @PostMapping("/password/modify")
    public ResponseEntity<Boolean> passwordModify(@RequestBody @Validated PasswordModifyRequest passwordModifyRequest){
        return ResponseEntity.ok().body(memberProfileService.modifyPassword(passwordModifyRequest));
    }

    /**
     * 프로필 사진 변경
     * */
    @Operation(description = "회원 프로필 사진 변경")
    @PostMapping("/image/modify")
    public ResponseEntity<String> profileImageModify(@RequestParam("file") MultipartFile profileImage){
        return ResponseEntity.ok().body(memberProfileService.modifyProfileImage(profileImage, "member"));
    }

    /**
     * 탈퇴하기
     */
    @Operation(description = "회원 탈퇴")
    @PostMapping("/withdrawal")
    public ResponseEntity<Boolean> memberWithdrawal(@RequestBody MemberWithdrawalRequest memberWithdrawalRequest){
        return ResponseEntity.ok().body(memberProfileService.deleteMember(memberWithdrawalRequest));
    }
}
