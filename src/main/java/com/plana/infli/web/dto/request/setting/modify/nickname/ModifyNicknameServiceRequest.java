package com.plana.infli.web.dto.request.setting.modify.nickname;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ModifyNicknameServiceRequest {

    private final String username;

    private final String nickname;

    @Builder
    private ModifyNicknameServiceRequest(String username, String nickname) {
        this.username = username;
        this.nickname = nickname;
    }
}
