package com.plana.infli.web.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class PrincipalHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		AuthenticatedPrincipal annotation = parameter.getParameterAnnotation(AuthenticatedPrincipal.class);

		if (annotation == null) {
			return false;
		}

		return parameter.getParameterType().equals(String.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		Object principal = null;

		if (authentication != null) {
			principal = authentication.getPrincipal();
		}

		if (principal == null || principal.getClass() != String.class) {
			return null;
		}

		return principal;
	}
}
