package com.plana.infli.web.dto.request.setting.validate.nickname;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ValidateNewNicknameServiceRequest {

    private final String username;

    private final String newNickname;

    @Builder
    public ValidateNewNicknameServiceRequest(String username, String newNickname) {
        this.username = username;
        this.newNickname = newNickname;
    }
}
