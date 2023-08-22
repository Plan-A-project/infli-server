package com.plana.infli.infra.security.filter;

import static org.springframework.util.StringUtils.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.infra.security.token.CustomAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class CustomLoginProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomLoginProcessingFilter() {
        super(new AntPathRequestMatcher("/api/login", "POST"));
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {

        if (request.getMethod().equals("POST") == false) {
            throw new BadCredentialsException("지원되지 않는 로그인 방식입니다.");
        }

        try {
            Login login = objectMapper.readValue(request.getReader(), Login.class);

            if (hasText(login.username) == false || hasText(login.password) == false) {
                throw new BadCredentialsException("아이디와 비밀번호는 공백일수 없습니다.");
            }
            CustomAuthenticationToken token = new CustomAuthenticationToken(login.getUsername(),
                    login.getPassword());

            return this.getAuthenticationManager().authenticate(token);

        } catch (IOException e) {
            throw new BadCredentialsException("로그인 형식에 맞지 않습니다.");
        }
    }

    @Getter
    @NoArgsConstructor
    public static class Login {

        private String username;

        private String password;

        @Builder
        private Login(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

}
