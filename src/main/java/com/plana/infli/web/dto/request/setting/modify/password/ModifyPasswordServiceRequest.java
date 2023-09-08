package com.plana.infli.web.dto.request.setting.modify.password;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ModifyPasswordServiceRequest {

    private final String username;

    private final String currentPassword;

    private final String newPassword;

    private final String newPasswordConfirm;

    @Builder
    private ModifyPasswordServiceRequest(String username, String currentPassword,
            String newPassword, String newPasswordConfirm) {

        this.username = username;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.newPasswordConfirm = newPasswordConfirm;
    }
}
