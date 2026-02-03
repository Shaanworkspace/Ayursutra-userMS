package com.user.Security;

import com.user.Enum.AuthProviderName;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AuthUtil {
	public AuthProviderName returnAuthProviderName(String registrationId){
		return switch (registrationId.toLowerCase()){
			case "google" -> AuthProviderName.GOOGLE;
			case "github" -> AuthProviderName.GITHUB;
			default -> throw new IllegalArgumentException("Unsupported auth provider : "+registrationId);
		};
	}

	/*
	This we created a separate method as id is stored in different attribute depends on provider platform like GitHub have a key name "id" and Google have "sub"
	 */
	public String getProviderIdFromUser(OAuth2User oAuth2User, String registrationId) {
		String providerId = switch (registrationId.toLowerCase()){
			case "google" -> oAuth2User.getAttribute("sub");
			case "github" -> oAuth2User.getAttribute("id").toString();
			default -> {
				log.error("Unsupported Oauth2 provider");
				throw  new IllegalArgumentException("Not able to extract provider id from Oauth2User variable ");
			}
		};
		if(providerId==null  || providerId.isBlank()){
			log.error("Unsupported Oauth2 provider");
			throw  new IllegalArgumentException("Not able to extract provider id from Oauth2User variable ");
		}
		return providerId;
	}


	public String getProviderEmailFromUser(OAuth2User oAuth2User, String registrationId,String providerId) {
		String email = oAuth2User.getAttribute("email");
		if (email != null && !email.isBlank()) {
			return email;
		}
		return switch (registrationId.toLowerCase()){
			case "google" -> oAuth2User.getAttribute("sub");
			case "github" -> oAuth2User.getAttribute("login").toString();
			default -> providerId;
		};
	}
	public String fetchGitHubEmailUsingToken(String accessToken) {
		String githubApiUrl = "https://api.github.com/user/emails";

		// Headers taiyar karna
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.set("Accept", "application/vnd.github+json");

		HttpEntity<String> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		try {
			log.info("GitHub API se private email fetch kar rahe hain...");
			ResponseEntity<List> response = restTemplate.exchange(
					githubApiUrl,
					HttpMethod.GET,
					entity,
					List.class
			);

			List<Map<String, Object>> emailList = response.getBody();

			if (emailList != null) {
				for (Map<String, Object> emailObj : emailList) {
					// Hum primary email dhoond rahe hain
					if ((boolean) emailObj.get("primary")) {
						return (String) emailObj.get("email");
					}
				}
			}
		} catch (Exception e) {
			log.error("GitHub API se email lene mein error aayi: {}", e.getMessage());
		}
		return null;
	}
	public String getLastNameFromUser(OAuth2User oAuth2User, String registrationId) {
		Map<String, Object> attributes = oAuth2User.getAttributes();

		return switch (registrationId.toLowerCase()) {
			case "google" -> {
				String familyName = (String) attributes.get("family_name");
				yield (familyName != null && !familyName.isBlank()) ? familyName : "";
			}
			case "github" -> {
				String fullName = (String) attributes.get("name");
				if (fullName != null && fullName.contains(" ")) {
					String[] parts = fullName.split(" ");
					yield parts[parts.length - 1];
				}
				yield "";
			}
			default -> "";
		};
	}

	public String getFirstNameFromUser(OAuth2User oAuth2User, String registrationId) {
		Map<String, Object> attributes = oAuth2User.getAttributes();

		return switch (registrationId.toLowerCase()) {
			case "google" -> {
				String name = (String) attributes.getOrDefault("given_name", attributes.get("name"));
				yield (name != null && !name.isBlank()) ? name : "User";
			}
			case "github" -> {
				String name = (String) attributes.get("name"); // This can be null
				if (name != null && !name.isBlank()) {
					yield name.split(" ")[0];
				}
				// Fallback to login (username) if name is hidden
				yield (String) attributes.get("login");
			}
			default -> "User";
		};
	}


}
