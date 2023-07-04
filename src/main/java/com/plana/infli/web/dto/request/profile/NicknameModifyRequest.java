package com.plana.infli.web.dto.request.profile;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NicknameModifyRequest {

    private String email;

    @Pattern(regexp = "^[ㄱ-ㅎ가-힣A-Za-z0-9-_]{2,8}$",
        message = "닉네임은 2~8자리여야 합니다. 한글, 영어, 숫자 조합 가능.")
    private String afterNickname;
}
