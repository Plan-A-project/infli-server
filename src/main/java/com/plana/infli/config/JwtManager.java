package com.plana.infli.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtManager {
	private final String secret;
	private final Integer accessTokenExpirationSeconds;
	private final Integer refreshTokenExpirationSeconds;

	public JwtManager(@Value("${jwt.properties.secret}") String secret,
		@Value("${jwt.properties.access-token-expiration-seconds}") Integer accessTokenExpirationSeconds,
		@Value("${jwt.properties.refresh-token-expiration-seconds}") Integer refreshTokenExpirationSeconds) {

		this.secret = secret;
		this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
		this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
	}

	public String generateAccessToken(UserDetails userDetails) {

		return JWT.create()
			.withSubject(userDetails.getUsername())
			.withArrayClaim("USER_ROLE",
				userDetails.getAuthorities().stream().map(Object::toString).toArray(String[]::new))
			.withExpiresAt(new Date(System.currentTimeMillis() + ((long)accessTokenExpirationSeconds * 1000L)))
			.sign(Algorithm.HMAC512(secret.getBytes()));
	}

	public String generateRefreshToken(UserDetails userDetails) {
		return JWT.create()
			.withSubject(userDetails.getUsername())
			.withExpiresAt(new Date(System.currentTimeMillis() + ((long)refreshTokenExpirationSeconds * 1000L)))
			.sign(Algorithm.HMAC512(secret.getBytes()));
	}

	public Authentication getAuthentication(String token) {
		if (!StringUtils.hasText(token)) {
			return null;
		}

		DecodedJWT decodedJwt = decode(token);

		String email = decodedJwt.getSubject();
		List<SimpleGrantedAuthority> userRoles =
			Arrays.stream(decodedJwt.getClaim("USER_ROLE")
					.asArray(SimpleGrantedAuthority.class))
				.toList();

		return new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
	}

	private DecodedJWT decode(String token) {
		return JWT.require(Algorithm.HMAC512(secret))
			.build()
			.verify(token);
	}
}