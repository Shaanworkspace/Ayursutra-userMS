package com.user.Config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@Slf4j
@ConfigurationProperties(prefix = "service-auth")
//Spring bolega → “service-auth" : se start hone wali saari application.properties ki field uthao”
public class ServiceCredentialConfig {

	/*

	Ye bna ke rkh dega
	--> Key = service ka naam (doctor-service, user-service)
	-> Value = ServiceCredential object

	services = {
			  "doctor-service" -> ServiceCredential(clientId, clientSecret),
			  "user-service"   -> ServiceCredential(clientId, clientSecret)
			}
	 */
	private Map<String, ServiceCredential> services = new HashMap<>();
	@Data
	public static class ServiceCredential {
		private String clientId;
		private String clientSecret;
	}

}
