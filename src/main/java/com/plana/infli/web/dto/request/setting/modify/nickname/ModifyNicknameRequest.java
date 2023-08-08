package com.plana.infli.web.dto.request.setting.modify.nickname;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ModifyNicknameRequest {

    private String email;

    @Pattern(regexp = "^[ㄱ-ㅎ가-힣A-Za-z0-9-_]{2,8}$",
        message = "닉네임은 2~8자리여야 합니다. 한글, 영어, 숫자 조합 가능.")
    private String newNickname;

    @Builder
    private ModifyNicknameRequest(String email, String newNickname) {
        this.email = email;
        this.newNickname = newNickname;
    }

    public ModifyNicknameServiceRequest toServiceRequest(String email) {
        return ModifyNicknameServiceRequest.builder()
                .email(email)
                .newNickname(newNickname)
                .build();
    }

}
