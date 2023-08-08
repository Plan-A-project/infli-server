package com.plana.infli.web.dto.request.setting.modify.nickname;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ModifyNicknameServiceRequest {

    private final String email;

    private final String newNickname;

    @Builder
    private ModifyNicknameServiceRequest(String email, String newNickname) {

        this.email = email;
        this.newNickname = newNickname;
    }
}
