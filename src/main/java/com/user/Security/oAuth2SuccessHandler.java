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
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException {

		try {
			OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
			OAuth2User oAuth2User = (OAuth2User) token.getPrincipal();

			String registrationId = token.getAuthorizedClientRegistrationId();

			String roleParam =
					(String) request.getSession().getAttribute("OAUTH_ROLE");

			if (roleParam == null) {
				throw new RuntimeException("Role not found in session");
			}

			Role role = Role.valueOf(roleParam);

			ResponseEntity<LoginResponse> loginResponse =
					authService.handleOAuth2LoginRequest(
							oAuth2User,
							registrationId,
							role,
							null
					);

			String payload =
					objectMapper.writeValueAsString(loginResponse.getBody());

			response.setContentType("text/html;charset=UTF-8");

			response.getWriter().write("""
        <script>
          if (window.opener) {
            window.opener.postMessage(%s, "*");
            window.close();
          }
        </script>
        """.formatted(payload));

		} catch (Exception ex) {

			log.error("OAuth2 failure", ex);

			response.setContentType("text/html;charset=UTF-8");

			response.getWriter().write("""
        <script>
          if (window.opener) {
            window.opener.postMessage({
              error: true,
              message: "%s"
            }, "*");
            window.close();
          }
        </script>
        """.formatted(ex.getMessage()));
		}
	}

}
