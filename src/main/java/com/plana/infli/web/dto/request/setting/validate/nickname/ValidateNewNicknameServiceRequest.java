package com.plana.infli.web.dto.request.setting.validate.nickname;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ValidateNewNicknameServiceRequest {

    private final String email;

    private final String newNickname;

    @Builder
    public ValidateNewNicknameServiceRequest(String email, String newNickname) {
        this.email = email;
        this.newNickname = newNickname;
    }
}
