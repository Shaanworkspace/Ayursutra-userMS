package com.user.Security;

import com.user.Enum.AuthProviderName;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

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
		if(email!=null || !email.isBlank()){
			return email;
		}
		return switch (registrationId.toLowerCase()){
			case "google" -> oAuth2User.getAttribute("sub");
			case "github" -> oAuth2User.getAttribute("login").toString();
			default -> providerId;
		};
	}

	public String getLastNameFromUser(OAuth2User oAuth2User, String registrationId) {

		return switch (registrationId.toLowerCase()) {

			case "google" -> {
				String familyName = oAuth2User.getAttribute("family_name");
				yield (familyName != null && !familyName.isBlank())
						? familyName
						: "User";
			}

			case "github" -> {
				String fullName = oAuth2User.getAttribute("name");
				if (fullName != null && !fullName.isBlank()) {
					String[] parts = fullName.split(" ");
					yield parts.length > 1 ? parts[1] : "User";
				}
				yield "User";
			}

			default -> "User";
		};
	}
	public String getFirstNameFromUser(OAuth2User oAuth2User, String registrationId) {

		return switch (registrationId.toLowerCase()) {

			case "google" -> {
				String givenName = oAuth2User.getAttribute("given_name");
				yield (givenName != null && !givenName.isBlank())
						? givenName
						: "Patient";
			}

			case "github" -> {
				String fullName = oAuth2User.getAttribute("name");
				if (fullName != null && !fullName.isBlank()) {
					yield fullName.split(" ")[0];
				}
				yield "Patient";
			}

			default -> "Patient";
		};
	}


}
