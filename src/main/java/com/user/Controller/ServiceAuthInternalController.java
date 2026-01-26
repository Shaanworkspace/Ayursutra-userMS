package com.user.Controller;

import com.user.DTO.Request.ServiceTokenRequest;
import com.user.Security.ServiceTokenVerifierAndProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/internal/auth")
@RequiredArgsConstructor
@Slf4j
public class ServiceAuthInternalController {

	private final ServiceTokenVerifierAndProvider serviceTokenService;

	@PostMapping("/token")
	public String issueServiceToken(@RequestBody ServiceTokenRequest request) {

		log.info("Issuing And Providing service token for {}", request.getClientId());

		return serviceTokenService.generateServiceToken(
				request.getClientId(),
				request.getClientSecret()
		);
	}
}
