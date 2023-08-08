package com.plana.infli.web.dto.request.setting.modify.password;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ModifyPasswordRequest {

    private String currentPassword;

    @NotEmpty(message = "새 비밀번호를 입력해주세요")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{6,16}$",
        message = "비밀번호는 6~16자리수여야 합니다. 영문 대소문자, 숫자, 특수문자를 1개 이상 포함해야 합니다.")
    private String newPassword;

    @NotEmpty(message = "새 비밀번호 확인을 입력해주세요")
    private String newPasswordConfirm;

    @Builder
    public ModifyPasswordRequest(String currentPassword, String newPassword,
            String newPasswordConfirm) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.newPasswordConfirm = newPasswordConfirm;
    }

    public ModifyPasswordServiceRequest toServiceRequest(String email) {
        return ModifyPasswordServiceRequest.builder()
                .email(email)
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .newPasswordConfirm(newPasswordConfirm)
                .build();
    }
}
