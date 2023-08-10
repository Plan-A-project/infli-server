package com.plana.infli.security.jwt;

import java.util.Date;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

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
			.withClaim("username", userDetails.getUsername())
			.withClaim("roles", userDetails.getAuthorities().stream()
				.map(Object::toString).toList())
			.withIssuedAt(new Date())
			.withExpiresAt(
				new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpirationSeconds() * 1000))
			.sign(algorithm);
	}

	public String createRefreshToken(UserDetails userDetails) {
		return JWT.create()
			.withIssuedAt(new Date())
			.withClaim("username", userDetails.getUsername())
			.withExpiresAt(
				new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpirationSeconds() * 1000))
			.sign(algorithm);
	}

	public UsernamePasswordAuthenticationToken resolveAccessToken(String accessToken) {
		DecodedJWT decodedJwt;
		decodedJwt = jwtVerifier.verify(accessToken);

		String username = decodedJwt.getClaim("username").asString();
		List<SimpleGrantedAuthority> roles = decodedJwt.getClaim("roles")
			.asList(String.class)
			.stream()
			.map(SimpleGrantedAuthority::new)
			.toList();

		return UsernamePasswordAuthenticationToken.authenticated(username, null, roles);
	}

	public String resolveRefreshToken(String refreshToken) {
		DecodedJWT decodedJwt;
		decodedJwt = jwtVerifier.verify(refreshToken);

		return decodedJwt.getClaim("username").asString();
	}
}
