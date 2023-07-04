package com.plana.infli.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class JwtProperties {

	@Value("${jwt.properties.secret}")
	private String secret;
	@Value("${jwt.properties.access-token-expiration-seconds}")
	private Long accessTokenExpirationSeconds;
	@Value("${jwt.properties.token-prefix}")
	private String tokenPrefix;
	@Value("${jwt.properties.access-token-header-name}")
	private String accessTokenHeaderName;
	@Value("${jwt.properties.refresh-token-header-name}")
	private String refreshTokenHeaderName;
	@Value("${jwt.properties.refresh-token-expiration-seconds}")
	private Long refreshTokenExpirationSeconds;
}
