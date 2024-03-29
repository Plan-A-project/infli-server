package com.plana.infli.infra.security.config;

import static java.util.Arrays.*;
import static java.util.List.*;
import static org.springframework.http.HttpMethod.*;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;

    private final AuthenticationProvider authenticationProvider;

    private final LogoutSuccessHandler logoutSuccessHandler;

    private final AuthenticationEntryPoint authenticationEntryPoint;

    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    private final AccessDeniedHandler accessDeniedHandler;

    private final AuthenticationFailureHandler authenticationFailureHandler;

    private final UserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        ProviderManager authenticationManager = (ProviderManager) authenticationConfiguration.getAuthenticationManager();
        authenticationManager.getProviders().add(authenticationProvider);
        return authenticationManager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .rememberMe((rememberMe) -> rememberMe
                        .userDetailsService(userDetailsService))

                .sessionManagement((sessionManagement) -> sessionManagement
                        .maximumSessions(1))

                .securityContext((securityContext) -> securityContext
                        .requireExplicitSave(false))

                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(POST, "/verification/student/**").hasAuthority("STUDENT")
                        .requestMatchers(POST, "/verification/company/**").hasAuthority("COMPANY")
                        .requestMatchers(GET, "/verification/student/email/{code}")
                        .permitAll()

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator",
                                "/error",
                                "/login",
                                "/signup/**").permitAll()
                        .requestMatchers("/boards/{boardId}/posts").permitAll()
                        .anyRequest().authenticated())

                .logout((logout) -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                        .deleteCookies("SESSION", "remember-me")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessHandler(logoutSuccessHandler))

                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                .csrf(AbstractHttpConfigurer::disable)

                .cors((cors) -> cors.configurationSource(corsConfigurationSource()));

        loginConfigurer(http);


        return http.build();
    }

    private void loginConfigurer(HttpSecurity http) throws Exception {
        http
                .apply(new CustomLoginConfigurer<>())
                .successHandlerCustom(authenticationSuccessHandler)
                .failureHandlerCustom(authenticationFailureHandler)
                .loginProcessingUrl("/login")
                .setAuthenticationManager(authenticationManager());

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "https://infli.co", "https://www.infli.co"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
