package com.user.Security;

import com.user.Config.ServiceCredentialConfig;
import com.user.JWT.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceTokenVerifierAndProvider {
	//credentials sahi hain ya nahi
	//agar sahi → JWT do”


	private final ServiceCredentialConfig credentialConfig;
	private final JwtUtil jwtUtil;

	public String generateServiceToken(String clientId, String clientSecret) {

		log.info("Service token request received for clientId={}", clientId);

		ServiceCredentialConfig.ServiceCredential credential =
				credentialConfig.getServices().get(clientId);
		log.info("Got Credentials : {}",credential);

		if (credential == null) {
			log.error("No service registered with clientId={}", clientId);
			throw new RuntimeException("Invalid service");
		}

		if (!credential.getClientSecret().equals(clientSecret)) {
			log.error("Invalid secret for clientId={}", clientId);
			throw new RuntimeException("Invalid service secret");
		}

		log.info("Service authenticated successfully: {}", clientId);

		return jwtUtil.generateServiceToServiceToken(clientId);
	}
}
