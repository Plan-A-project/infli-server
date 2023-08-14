package com.plana.infli.domain.embedded.member;

import static lombok.AccessLevel.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Embeddable
public class LoginCredentials {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Builder
    private LoginCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static LoginCredentials of(String username, String password) {
        return LoginCredentials.builder()
                .username(username)
                .password(password)
                .build();
    }

    public static LoginCredentials ofNewPassword(LoginCredentials credentials,
            String newEncryptedPassword) {

        return LoginCredentials.of(credentials.username, newEncryptedPassword);
    }
}
