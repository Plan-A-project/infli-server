package com.plana.infli.web.controller;

import com.plana.infli.service.SettingService;
import com.plana.infli.web.dto.request.setting.unregister.UnregisterMemberRequest;
import com.plana.infli.web.dto.request.setting.modify.nickname.ModifyNicknameRequest;
import com.plana.infli.web.dto.request.setting.validate.nickname.ValidateNewNicknameRequest;
import com.plana.infli.web.dto.request.setting.validate.password.AuthenticatePasswordRequest;
import com.plana.infli.web.dto.request.setting.modify.password.ModifyPasswordRequest;
import com.plana.infli.web.dto.response.profile.MyProfileResponse;
import com.plana.infli.web.dto.response.profile.MyProfileToUnregisterResponse;
import com.plana.infli.web.dto.response.profile.image.ChangeProfileImageResponse;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    /**
     * 내 정보 조회
     * */
    @Operation(description = "회원 정보 조회")
    @GetMapping("/profiles")
    public MyProfileResponse loadMyProfile(@AuthenticatedPrincipal String email) {
        return settingService.loadMyProfile(email);
    }


    /**
     * 닉네임 변경
     */
    @Operation(description = "사용 가능한 닉네임인지 여부 확인")
    @GetMapping("/validate/nickname")
    public boolean isAvailableNickname(@AuthenticatedPrincipal String email,
            @RequestBody @Validated ValidateNewNicknameRequest request) {
        return settingService.isAvailableNewNickname(request.toServiceRequest(email));
    }

    @Operation(description = "회원 닉네임 변경")
    @PostMapping("/modify/nickname")
    public void changeNickname(@AuthenticatedPrincipal String email,
            @RequestBody @Validated ModifyNicknameRequest request) {
        settingService.changeNickname(request.toServiceRequest(email));
    }


    /**
     * 비빌번호 변경 또는 탈퇴시 비밀번호 확인
     */
    @Operation(description = "비밀번호 확인 - 비밀번호 변경/탈퇴 시")
    @PostMapping("/validate/password")
    public void authenticatePassword(@AuthenticatedPrincipal String email,
            @RequestBody @Validated AuthenticatePasswordRequest request) {

        settingService.authenticatePassword(request.toServiceRequest(email));
    }

    /**
     * 비밀번호 변경
     */
    @Operation(description = "회원 비밀번호 변경")
    @PostMapping("/modify/password")
    public void modifyPassword(@AuthenticatedPrincipal String email,
            @RequestBody @Validated ModifyPasswordRequest request) {

        settingService.changePassword(request.toServiceRequest(email));
    }

    /**
     * 프로필 사진 변경
     */
    @Operation(description = "회원 프로필 사진 변경")
    @PostMapping("/modify/profileImage")
    public ChangeProfileImageResponse changeProfileImage(@AuthenticatedPrincipal String email,
            @RequestParam("file") MultipartFile profileImage) {
        return settingService.changeProfileImage(email, profileImage);
    }

    /**
     * 탈퇴하기
     */
    @Operation(description = "탈퇴를 요청한 회원의 이름과 이메일 조회")
    @GetMapping("/unregister")
    public MyProfileToUnregisterResponse loadProfileToUnregister(@AuthenticatedPrincipal String email) {
        return settingService.loadProfileToUnregister(email);
    }

    @Operation(description = "회원 탈퇴")
    @PostMapping("/unregister")
    public void unregister(@AuthenticatedPrincipal String email,
            @RequestBody @Validated UnregisterMemberRequest request) {
        settingService.unregisterMember(request.toServiceRequest(email));
    }
}
