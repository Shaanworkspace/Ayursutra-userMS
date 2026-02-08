package com.user.JWT.Filters;

import com.user.Entity.User;
import com.user.Repository.UserRepository;
import com.user.JWT.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	/*
		-> This Filter will verify each token coming either from "service -> service"   OR    " User -> Service "
	 */
	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();

		return path.startsWith("/api/user/oauth2")
				|| path.startsWith("/oauth2")
				|| path.startsWith("/login/oauth2")
				|| path.startsWith("/swagger-ui")
				|| path.startsWith("/v3/api-docs")
				|| path.startsWith("/api/user/login")
				|| path.startsWith("/api/user/register")
				|| path.startsWith("/api/user/health");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		log.info("Jwt Filter Applied to Incoming Request : {}",request.getRequestURI());

		final String requestHeader = request.getHeader("Authorization");



		if(requestHeader==null || !requestHeader.startsWith("Bearer")){
			log.info("User : Header is null for Incoming Request : {}",request.getRequestURI());
			filterChain.doFilter(request,response);
			return;
		}

		// Token will split in two parts we will take the 1st index -> means second part
		String token = requestHeader.substring(7);
		String subject = jwtUtil.getSubjectFromToken(token);
		log.info("We got the Subject from JWT : {}",subject);

		if (jwtUtil.isServiceToken(token)) {
			log.info("This Request Have Jwt from Service");
			// SERVICE TOKEN → NO DB
			UsernamePasswordAuthenticationToken auth =
					new UsernamePasswordAuthenticationToken(
							subject,
							null,
							List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
					);

			SecurityContextHolder.getContext().setAuthentication(auth);
		} else {
			log.info("This Request Have Jwt from User");
			// USER TOKEN → DB REQUIRED
			User user = userRepository.findByEmail(subject)
					.orElseThrow(() -> {
						log.warn("JWT valid but user not found: {}", subject);
						return new RuntimeException("User not found");
					});

			UsernamePasswordAuthenticationToken auth =
					new UsernamePasswordAuthenticationToken(
							user,
							null,
							user.getAuthorities()
					);
			SecurityContextHolder.getContext().setAuthentication(auth);
		}
		filterChain.doFilter(request,response);
	}
}
