package com.plana.infli.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.plana.infli.repository.redis.RedisDao;
import com.plana.infli.security.CustomAuthenticationEntryPoint;
import com.plana.infli.security.JsonLoginFailureHandler;
import com.plana.infli.security.JsonLoginProcessingFilter;
import com.plana.infli.security.jwt.JwtAuthenticationFilter;
import com.plana.infli.security.jwt.JwtExceptionHandlerFilter;
import com.plana.infli.security.jwt.JwtLoginSuccessHandler;
import com.plana.infli.security.jwt.JwtManager;
import com.plana.infli.security.jwt.JwtProperties;
import com.plana.infli.security.jwt.JwtTokenExpiredEntrypoint;

@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {

	@Autowired
	private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	@Autowired
	private JwtTokenExpiredEntrypoint jwtTokenExpiredEntrypoint;

	@Autowired
	private JwtProperties jwtProperties;

	@Autowired
	private JwtManager jwtManager;

	@Autowired
	private RedisDao redisDao;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		AuthenticationManager authenticationManager = authenticationManager(
			http.getSharedObject(AuthenticationConfiguration.class));

		http.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.exceptionHandling(
				exceptionHandling -> exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint))
			.addFilterAt(jsonLoginProcessingFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(jwtExceptionHandlerFilter(), SecurityContextHolderFilter.class)
			.addFilterAfter(jwtAuthenticationFilter(), JwtExceptionHandlerFilter.class);

		http
			.authorizeHttpRequests((auth) -> auth
				.requestMatchers("/error").permitAll()
				.requestMatchers("/auth/**").permitAll()
				.requestMatchers("/member/email/auth/**").permitAll()
				.requestMatchers("/member/student/auth/**").permitAll()
				.requestMatchers("/member/email/auth/send").authenticated()
				.requestMatchers("/member/student/auth/send").authenticated()
				.anyRequest().authenticated()
			);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws
		Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtManager, jwtProperties);
	}

	@Bean
	public JsonLoginProcessingFilter jsonLoginProcessingFilter(AuthenticationManager authenticationManager) {
		return new JsonLoginProcessingFilter(new AntPathRequestMatcher("/auth/login", "POST"),
			authenticationManager,
			jwtLoginSuccessHandler(),
			jwtLoginFailureHandler());
	}

	@Bean
	public JwtExceptionHandlerFilter jwtExceptionHandlerFilter() {
		return new JwtExceptionHandlerFilter(jwtTokenExpiredEntrypoint);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JwtLoginSuccessHandler jwtLoginSuccessHandler() {
		return new JwtLoginSuccessHandler(jwtProperties, jwtManager, redisDao);
	}

	@Bean
	public JsonLoginFailureHandler jwtLoginFailureHandler() {
		return new JsonLoginFailureHandler();
	}
}
