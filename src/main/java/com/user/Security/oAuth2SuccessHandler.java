package com.user.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.DTO.Response.LoginResponse;
import com.user.Enum.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class oAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final AuthService authService;
	private final ObjectMapper objectMapper;
	private final OAuth2AuthorizedClientService authorizedClientService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		// Like github , google, linkedin
		String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
		OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
				registrationId,
				oAuth2AuthenticationToken.getName()
		);
		String accessToken = null;
		if (client != null && client.getAccessToken() != null) {
			accessToken = client.getAccessToken().getTokenValue();
			log.info("Access Token successfully extracted for {}", registrationId);
		}
		String roleParam =
				(String) request.getSession().getAttribute("OAUTH_ROLE");

		if (roleParam == null) {
			throw new RuntimeException("Role not found in session");
		}

		log.info("Role received by {} is : {} , with url : {}",registrationId,roleParam,request.getRequestURI());

		Role role;
		try {
			role = Role.valueOf(roleParam);
		} catch (Exception e) {
			throw new RuntimeException("Invalid or missing role in OAuth request");
		}


		ResponseEntity<LoginResponse> loginResponse = authService.handleOAuth2LoginRequest(oAuth2User,registrationId, role,
				accessToken);

		log.info("Got login response in OAuthSuccessHandler : {}",loginResponse);
		String payload = objectMapper.writeValueAsString(loginResponse.getBody());

		response.setContentType("text/html;charset=UTF-8");

		String html = """
        <script>
          if (window.opener) {
            window.opener.postMessage(%s, "*");
            window.close();
          }
        </script>
        """.formatted(payload);

		response.getWriter().write(html);
	}
}
