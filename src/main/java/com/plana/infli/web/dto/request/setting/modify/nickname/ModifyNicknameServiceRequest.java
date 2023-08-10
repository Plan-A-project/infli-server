package com.plana.infli.web.dto.request.setting.modify.nickname;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ModifyNicknameServiceRequest {

    private final String username;

    private final String newNickname;

    @Builder
    private ModifyNicknameServiceRequest(String username, String newNickname) {

        this.username = username;
        this.newNickname = newNickname;
    }
}
