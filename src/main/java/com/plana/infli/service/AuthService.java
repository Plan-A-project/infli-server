package com.plana.infli.service;

import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.plana.infli.repository.redis.RedisDao;
import com.plana.infli.security.jwt.JwtManager;
import com.plana.infli.security.jwt.JwtProperties;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {

	private final RedisDao redisDao;

	private final JwtManager jwtManager;

	private final JwtProperties jwtProperties;

	private final UserDetailsService userDetailsService;

	public HttpHeaders reissue(String refreshToken) {
		String username = jwtManager.resolveRefreshToken(
			refreshToken.substring(jwtProperties.getTokenPrefix().length() + 1));

		if (!StringUtils.hasText(redisDao.getValues(username))) {
			throw new TokenExpiredException("토큰이 만료되었습니다.", Instant.now());
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		String accessToken = jwtManager.createAccessToken(userDetails);
		String newRefreshToken = jwtManager.createRefreshToken(userDetails);

		redisDao.deleteValues(username);
		redisDao.setValues(username, newRefreshToken);

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(jwtProperties.getAccessTokenHeaderName(),
			jwtProperties.getTokenPrefix() + " " + accessToken);
		httpHeaders.add(jwtProperties.getRefreshTokenHeaderName(),
			jwtProperties.getTokenPrefix() + " " + newRefreshToken);

		return httpHeaders;
	}

}
