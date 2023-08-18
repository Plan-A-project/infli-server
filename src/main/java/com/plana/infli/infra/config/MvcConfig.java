package com.plana.infli.infra.config;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.plana.infli.web.resolver.PrincipalHandlerMethodArgumentResolver;

@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

	private final PrincipalHandlerMethodArgumentResolver principalHandlerMethodArgumentResolver;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(principalHandlerMethodArgumentResolver);
	}
}
