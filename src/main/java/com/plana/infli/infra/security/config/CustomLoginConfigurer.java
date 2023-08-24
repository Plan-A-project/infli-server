package com.plana.infli.infra.security.config;

import com.plana.infli.infra.security.filter.CustomLoginProcessingFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class CustomLoginConfigurer <H extends HttpSecurityBuilder<H>> extends
        AbstractAuthenticationFilterConfigurer<H, CustomLoginConfigurer<H>, CustomLoginProcessingFilter> {

    private AuthenticationSuccessHandler successHandler;

    private AuthenticationFailureHandler failureHandler;

    private AuthenticationManager authenticationManager;

    public CustomLoginConfigurer() {
        super(new CustomLoginProcessingFilter(), null);
    }

    @Override
    public void init(H http) throws Exception {
        super.init(http);
    }

    @Override
    public void configure(H http)  {

        if (authenticationManager == null) {
            authenticationManager = http.getSharedObject(AuthenticationManager.class);
        }
        getAuthenticationFilter().setAuthenticationManager(authenticationManager);
        getAuthenticationFilter().setAuthenticationSuccessHandler(successHandler);
        getAuthenticationFilter().setAuthenticationFailureHandler(failureHandler);
        getAuthenticationFilter().setFilterProcessesUrl("/login");

        SessionAuthenticationStrategy sessionAuthenticationStrategy = http
                .getSharedObject(SessionAuthenticationStrategy.class);

        if (sessionAuthenticationStrategy != null) {
            getAuthenticationFilter().setSessionAuthenticationStrategy(
                    sessionAuthenticationStrategy);
        }

        RememberMeServices rememberMeServices = http.getSharedObject(RememberMeServices.class);

        if (rememberMeServices != null) {
            getAuthenticationFilter().setRememberMeServices(rememberMeServices);
        }
        http.setSharedObject(CustomLoginProcessingFilter.class, getAuthenticationFilter());
        http.addFilterBefore(getAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    public CustomLoginConfigurer<H> successHandlerCustom(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
        return this;
    }

    public CustomLoginConfigurer<H> failureHandlerCustom(AuthenticationFailureHandler authenticationFailureHandler) {
        this.failureHandler = authenticationFailureHandler;
        return this;
    }

    public CustomLoginConfigurer<H> setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        return this;
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl, "POST");
    }
}
