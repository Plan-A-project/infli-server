package com.plana.infli.web.dto.request.setting.modify.nickname;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ModifyNicknameRequest {

    @NotEmpty(message = "변경할 새 닉네임을 입력해주세요")
    private String nickname;

    @Builder
    private ModifyNicknameRequest(String nickname) {
        this.nickname = nickname;
    }

    public ModifyNicknameServiceRequest toServiceRequest(String username) {
        return ModifyNicknameServiceRequest.builder()
                .username(username)
                .nickname(nickname)
                .build();
    }
}
