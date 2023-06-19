package com.plana.infli.config;

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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.plana.infli.security.JsonLoginFailureHandler;
import com.plana.infli.security.JsonLoginProcessingFilter;
import com.plana.infli.security.jwt.JwtLoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		AuthenticationManager authenticationManager = authenticationManager(
			http.getSharedObject(AuthenticationConfiguration.class));

		http.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.addFilterAt(jsonLoginProcessingFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class);

		http
			.authorizeHttpRequests((auth) -> auth
				.requestMatchers("/**").permitAll()
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
	public JsonLoginProcessingFilter jsonLoginProcessingFilter(AuthenticationManager authenticationManager) {
		return new JsonLoginProcessingFilter(new AntPathRequestMatcher("/member/login", "POST"),
			authenticationManager,
			jwtLoginSuccessHandler(),
			jwtLoginFailureHandler());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JwtLoginSuccessHandler jwtLoginSuccessHandler() {
		return new JwtLoginSuccessHandler();
	}

	@Bean
	public JsonLoginFailureHandler jwtLoginFailureHandler() {
		return new JsonLoginFailureHandler();
	}
}
