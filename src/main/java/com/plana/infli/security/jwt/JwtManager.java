package com.plana.infli.security.jwt;

import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtManager {

	private final JwtProperties jwtProperties;

	private Algorithm algorithm;
	private JWTVerifier jwtVerifier;

	@PostConstruct
	public void init() {
		algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
		jwtVerifier = JWT.require(algorithm).build();
	}

	public String createAccessToken(UserDetails userDetails) {
		return JWT.create()
			.withClaim("email", userDetails.getUsername())
			.withClaim("role", userDetails.getAuthorities().stream()
				.map(Object::toString).toList())
			.withIssuedAt(new Date())
			.withExpiresAt(
				new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpirationSeconds() * 1000))
			.sign(algorithm);
	}

	public String createRefreshToken(UserDetails userDetails) {
		return JWT.create()
			.withIssuedAt(new Date())
			.withClaim("email", userDetails.getUsername())
			.withExpiresAt(
				new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpirationSeconds() * 1000))
			.sign(algorithm);
	}
}
