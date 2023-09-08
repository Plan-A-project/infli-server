package com.plana.infli.web.dto.response.profile;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.type.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MyProfileResponse {

    private final String nickname;

    private final String username;

    private final Role role;

    private final String thumbnailUrl;

    private final String originalUrl;

    @Builder
    private MyProfileResponse(String nickname, String username, Role role,
            String thumbnailUrl, String originalUrl) {

        this.nickname = nickname;
        this.username = username;
        this.role = role;
        this.thumbnailUrl = thumbnailUrl;
        this.originalUrl = originalUrl;
    }

    public static MyProfileResponse of(Member member) {
        return MyProfileResponse.builder()
                .nickname(member.getBasicCredentials().getNickname())
                .username(member.getLoginCredentials().getUsername())
                .role(member.getRole())
                .thumbnailUrl(member.getProfileImage().getThumbnailUrl())
                .originalUrl(member.getProfileImage().getOriginalUrl())
                .build();
    }
}
