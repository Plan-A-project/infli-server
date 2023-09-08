package com.plana.infli.infra.security.handler;

import static jakarta.servlet.http.HttpServletResponse.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.domain.type.Role;
import com.plana.infli.infra.security.service.CustomUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper om;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomUser customUser = (CustomUser) authentication.getPrincipal();

        String nickName = customUser.getNickName();
        Role role = customUser.getRole();

        String json = om.writeValueAsString(LoginSuccessResponse.builder()
                .nickname(nickName)
                .role(role)
                .build());

        response.setStatus(SC_OK);
        response.setCharacterEncoding(UTF_8.name());
        response.setContentType(APPLICATION_JSON_VALUE);
        response.getWriter().print(json);
        response.setStatus(SC_OK);
    }

    @Getter
    private static class LoginSuccessResponse {

        private final String nickname;

        private final Role role;

        @Builder
        private LoginSuccessResponse(String nickname, Role role) {
            this.nickname = nickname;
            this.role = role;
        }

    }
}
