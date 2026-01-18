package com.user.Config.Interceptor;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignAuthInterceptor {

	@Bean
	public RequestInterceptor requestInterceptor() {
		return template -> {

			Authentication authentication =
					SecurityContextHolder.getContext().getAuthentication();

			if (authentication == null) return;

			Object credentials = authentication.getCredentials();

			if (credentials instanceof String jwt) {
				template.header("Authorization", "Bearer " + jwt);
			}
		};
	}
}
