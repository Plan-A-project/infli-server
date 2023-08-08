package com.plana.infli.web.dto.request.setting.modify.password;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ModifyPasswordServiceRequest {

    private final String email;

    private final String currentPassword;

    private final String newPassword;

    private final String newPasswordConfirm;

    @Builder
    private ModifyPasswordServiceRequest(String email, String currentPassword, String newPassword,
            String newPasswordConfirm) {
        this.email = email;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.newPasswordConfirm = newPasswordConfirm;
    }
}
