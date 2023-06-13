package com.plana.infli.web.dto.response.profile;

import com.plana.infli.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberProfileResponse {
    private String nickname;
    private Role role;
    private String email;

}
