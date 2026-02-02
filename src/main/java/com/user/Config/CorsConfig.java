package com.user.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

	// Allowed origins for DIRECT access
	private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
			"http://localhost:5173",
			"http://localhost:3000",
			"https://ayursutra-frontend.netlify.app",
			"http://127.0.0.1:5173",
			"http://127.0.0.1:3000"
	);

	@Bean
	public FilterRegistrationBean<Filter> corsFilter() {
		FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();

		registration.setFilter(new Filter() {
			@Override
			public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
					throws IOException, ServletException {

				HttpServletRequest request = (HttpServletRequest) req;
				HttpServletResponse response = (HttpServletResponse) res;

				// Check if request is coming through API Gateway
				boolean isGatewayRequest = isRequestFromGateway(request);

				// Only add CORS headers for DIRECT access (not through gateway)
				if (!isGatewayRequest) {
					String origin = request.getHeader("Origin");

					if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
						response.setHeader("Access-Control-Allow-Origin", origin);
						response.setHeader("Access-Control-Allow-Methods",
								"GET, POST, PUT, DELETE, PATCH, OPTIONS");
						response.setHeader("Access-Control-Allow-Headers",
								"Authorization, Content-Type, X-Requested-With, Accept, Origin");
						response.setHeader("Access-Control-Allow-Credentials", "true");
						response.setHeader("Access-Control-Max-Age", "3600");
					}

					// Handle preflight OPTIONS request
					if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
						response.setStatus(HttpServletResponse.SC_OK);
						return;
					}
				}

				chain.doFilter(req, res);
			}

			private boolean isRequestFromGateway(HttpServletRequest request) {
				// Gateway adds these headers when forwarding
				String forwardedHost = request.getHeader("X-Forwarded-Host");
				String forwardedFor = request.getHeader("X-Forwarded-For");
				String gatewayHeader = request.getHeader("X-Gateway-Request");

				// Check if any gateway indicator is present
				return (forwardedHost != null &&
						(forwardedHost.contains("ayursutra-gateway") ||
								forwardedHost.contains("localhost:8085"))) ||
						(gatewayHeader != null && gatewayHeader.equals("true")) ||
						(forwardedFor != null);
			}
		});

		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		registration.addUrlPatterns("/*");
		registration.setName("corsFilter");

		return registration;
	}
}