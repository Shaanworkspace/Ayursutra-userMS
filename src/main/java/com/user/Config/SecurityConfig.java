package com.user.Config;

import com.user.JWT.Filters.JwtAuthFilter;
import com.user.Security.oAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,oAuth2SuccessHandler successHandler) throws Exception {

		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/api/user/login",
								"/api/user/pre-login",
								"/api/user/register",
								"/api/user/health",
								"/api/user",
								"/oauth2/**",
								"/api/user/doctor/**",
								"/api/user/therapist/**"
						).permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.oauth2Login(oAuth2->oAuth2
						.failureHandler((request,response,exception)->{
							log.error("OAuth2 Error: {}",exception.getMessage());
						})
						.successHandler(successHandler)
				);
		return http.build();
	}
}
