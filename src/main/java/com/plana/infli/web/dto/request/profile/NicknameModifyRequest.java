package com.plana.infli.web.dto.request.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NicknameModifyRequest {

    private String email;
    private String afterNickname;
}
