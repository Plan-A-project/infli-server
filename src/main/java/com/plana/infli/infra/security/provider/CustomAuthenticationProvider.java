package com.plana.infli.infra.security.provider;

import com.plana.infli.infra.security.service.CustomUser;
import com.plana.infli.infra.security.service.CustomUserDetailService;
import com.plana.infli.infra.security.token.CustomAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder encoder;

    private final CustomUserDetailService userDetailService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        CustomUser customUser = (CustomUser) userDetailService.loadUserByUsername(username);

        if (encoder.matches(password, customUser.getPassword())) {
            return new CustomAuthenticationToken(customUser, null, customUser.getAuthorities());
        }

        throw new BadCredentialsException("아이디 또는 비밀번호를 잘못 입력했습니다.");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(CustomAuthenticationToken.class);
    }
}
