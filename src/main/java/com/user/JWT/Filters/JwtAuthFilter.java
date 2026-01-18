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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		log.info("Incoming Request : {}",request.getRequestURI());

		final String requestHeader = request.getHeader("Authorization");
		if(requestHeader==null || !requestHeader.startsWith("Bearer")){
			filterChain.doFilter(request,response);
			return;
		}

		// Token will split in two parts we will take the 1st index -> means second part
		String token = requestHeader.split("Bearer ")[1];
		String username = jwtUtil.getUsernameByToken(token);

		// As we can see the flow in image : https://www.notion.so/Spring-Security-2b6ad6030d42805a9a68ea051454b246?source=copy_link#2ecad6030d42807b87d7cbe3c3f2e332
		/*
		we are currently now at Security Filter Chain -> we will move to Security context via UsernamePasswordAuthenticationToken
		We need to set Token in SecurityContextHolder so that we can move to next step
		 */
		if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null){
			User user = userRepository.findByEmail(username).orElseThrow();
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
		}
		filterChain.doFilter(request,response);
		return;
	}
}
