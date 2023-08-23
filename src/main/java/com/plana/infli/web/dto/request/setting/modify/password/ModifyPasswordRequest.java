package com.plana.infli.web.dto.request.setting.modify.password;

import static com.plana.infli.web.controller.AuthController.PASSWORD_REGEX;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ModifyPasswordRequest {

    @NotEmpty(message = "기존 비밀번호를 입력해주세요")
    private String currentPassword;

    @NotNull(message = "새 비밀번호를 입력해주세요")
    @Pattern(regexp = PASSWORD_REGEX, message = "비밀번호는 영어, 숫자, 특수문자를 포함해서 8~20자리 이내로 입력해주세요.")
    private String newPassword;

    @NotEmpty(message = "새 비밀번호 확인을 입력해주세요")
    private String newPasswordConfirm;

    @Builder
    private ModifyPasswordRequest(String currentPassword,
            String newPassword, String newPasswordConfirm) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.newPasswordConfirm = newPasswordConfirm;
    }

    public ModifyPasswordServiceRequest toServiceRequest(String username) {
        return ModifyPasswordServiceRequest.builder()
                .username(username)
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .newPasswordConfirm(newPasswordConfirm)
                .build();
    }

}
