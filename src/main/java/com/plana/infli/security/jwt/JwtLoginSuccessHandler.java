package com.plana.infli.security.jwt;

import java.io.IOException;
import java.time.Duration;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.plana.infli.repository.redis.RedisDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final JwtProperties jwtProperties;
	private final JwtManager jwtManager;
	private final RedisDao redisDao;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		log.info("login success");
		UserDetails userDetails = (UserDetails)authentication.getPrincipal();
		String accessToken = jwtManager.createAccessToken(userDetails);
		String refreshToken = jwtManager.createRefreshToken(userDetails);

		redisDao.deleteValues(userDetails.getUsername());
		redisDao.setValues(userDetails.getUsername(), refreshToken, Duration.ofSeconds(
			jwtProperties.getRefreshTokenExpirationSeconds()));

		response.setHeader(jwtProperties.getAccessTokenHeaderName(),
			jwtProperties.getTokenPrefix() + " " + accessToken);
		response.setHeader(jwtProperties.getRefreshTokenHeaderName(),
			jwtProperties.getTokenPrefix() + " " + refreshToken);

	}
}
