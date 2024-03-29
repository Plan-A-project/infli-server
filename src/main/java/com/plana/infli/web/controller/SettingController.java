package com.plana.infli.web.controller;

import com.plana.infli.service.SettingService;
import com.plana.infli.web.dto.request.setting.modify.nickname.ModifyNicknameRequest;
import com.plana.infli.web.dto.request.setting.modify.password.ModifyPasswordRequest;
import com.plana.infli.web.dto.request.setting.verify.password.VerifyPasswordRequest;
import com.plana.infli.web.dto.response.profile.MyProfileResponse;
import com.plana.infli.web.dto.response.profile.MyProfileToUnregisterResponse;
import com.plana.infli.web.dto.response.profile.image.ChangeProfileImageResponse;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/setting")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping("/profile")
    @Operation(summary = "회원 정보 조회")
    public MyProfileResponse loadMyProfile(@AuthenticatedPrincipal String username) {
        return settingService.loadMyProfile(username);
    }

    @GetMapping("/nickname/{nickname}")
    @Operation(summary = "사용 가능한 새로운 닉네임인지 여부 확인")
    public String checkIsAvailableNickname(@PathVariable String nickname) {
        return settingService.checkIsAvailableNewNickname(nickname);
    }

    @PostMapping("/nickname")
    @Operation(summary = "회원 닉네임 변경")
    public void changeNickname(@AuthenticatedPrincipal String username,
            @RequestBody @Validated ModifyNicknameRequest request) {
        settingService.changeNickname(request.toServiceRequest(username));
    }

    @PostMapping("/password/verification")
    @Operation(summary = "기존 비밀번호 검증")
    public String verifyCurrentPassword(@AuthenticatedPrincipal String username,
            @RequestBody @Validated VerifyPasswordRequest request) {

        return settingService.verifyCurrentPassword(request.toServiceRequest(username));
    }

    @PostMapping("/password")
    @Operation(summary = "비밀번호 변경")
    public String modifyPassword(@AuthenticatedPrincipal String username,
            @RequestBody @Validated ModifyPasswordRequest request) {

        settingService.changePassword(request.toServiceRequest(username));
        return "비밀번호 변경 완료";
    }

    @PostMapping("/profile/image")
    @Operation(summary = "회원 프로필 사진 변경")
    public ChangeProfileImageResponse changeProfileImage(@AuthenticatedPrincipal String username,
            @RequestParam MultipartFile file) {
        return settingService.changeProfileImage(username, file);
    }

    @GetMapping("/unregister")
    @Operation(summary = "탈퇴를 요청한 회원의 프로필 정보 조회")
    public MyProfileToUnregisterResponse loadProfileToUnregister(
            @AuthenticatedPrincipal String username) {

        return settingService.loadProfileToUnregister(username);
    }

    @PostMapping("/unregister")
    @Operation(summary = "회원 탈퇴")
    public String unregister(@AuthenticatedPrincipal String username, @RequestBody String password) {

        settingService.unregisterMember(username, password);
        return "탈퇴 완료";
    }
}
