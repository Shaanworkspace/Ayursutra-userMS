package com.user.Security;

import com.user.Client.DoctorClient;
import com.user.Client.PatientClient;
import com.user.DTO.Request.RegisterRequest;

import com.user.DTO.Response.LoginResponse;
import com.user.DTO.Response.UserResponse;
import com.user.Entity.User;
import com.user.Enum.ApprovalStatus;
import com.user.Enum.AuthProviderName;
import com.user.Enum.Role;
import com.user.JWT.JwtUtil;
import com.user.Repository.UserRepository;
import com.user.Service.LoginService;
import com.user.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final PasswordEncoder passwordEncoder;
	private final AuthUtil authUtil;
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final UserService userService;
	private final LoginService loginService;

	public ResponseEntity<LoginResponse> handleOAuth2LoginRequest(
			OAuth2User oAuth2User,
			String registrationId,
			Role role,
			String accessToken
	) {

		log.info("handleOAuth2LoginReuqest with user :{} and role : {}",oAuth2User,role);
		// Register / Fetch user
		User user = findOrCreateUser(oAuth2User, registrationId,role,accessToken);

		// Generate JWT
		String jwt = jwtUtil.generateUserToServiceToken(user,role);
		log.info("Generated Jwt successfully:{}",jwt);

		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(
						user.getId(),
						jwt,
						user.getAuthorities()
				);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		switch (role) {
			case Role.PATIENT -> {
				log.info("Lets Sync the PATIENT");
				userService.syncPatientIfRequired(user);
				log.info("Complete Sync the PATIENT with user : {}",user);
				return ResponseEntity.ok( loginService.loginToLoginResponse(
								user,
								Role.PATIENT,
								ApprovalStatus.APPROVED,
								true
						)
				);
			}

			case Role.DOCTOR -> {
				// For Dev Propose Only
				log.info("Lets Sync the Doc");
				userService.syncDoctorIfRequired(user);
				log.info("Complete Sync the Doc with user : {}",user);
				return ResponseEntity
						.status(
								user.getApprovalStatusOfDoctor() == ApprovalStatus.APPROVED
										? 200
										: user.getApprovalStatusOfDoctor() == ApprovalStatus.PENDING
										? 202
										: 403
						)
						.body(
								loginService.loginToLoginResponse(
										user,
										role,
										user.getApprovalStatusOfDoctor(),
										user.getApprovalStatusOfDoctor() == ApprovalStatus.APPROVED
								)
						);
			}

			case Role.THERAPIST -> {
				log.info("Lets Sync the Therapist");
				userService.syncTherapistIfRequired(user);
				log.info("Complete Sync the Therapist with user : {}",user);
				return ResponseEntity
						.status(
								user.getApprovalStatusOfTherapist() == ApprovalStatus.APPROVED
										? 200
										: user.getApprovalStatusOfTherapist() == ApprovalStatus.PENDING
										? 202
										: 403
						)
						.body(
								loginService.loginToLoginResponse(
										user,
										role,
										user.getApprovalStatusOfTherapist(),
										user.getApprovalStatusOfTherapist() == ApprovalStatus.APPROVED
								)
						);
			}
			default -> throw new IllegalStateException("Unexpected role: " + role);
		}
	}
	private User findOrCreateUser(
			OAuth2User oAuth2User,
			String registrationId,
			Role role,
			String accessToken) {

		AuthProviderName authProviderName = authUtil.returnAuthProviderName(registrationId);
		String providerId = authUtil.getProviderIdFromUser(oAuth2User, registrationId);

		String email = oAuth2User.getAttribute("email");

		if ((email == null || email.isBlank()) && "github".equalsIgnoreCase(registrationId)) {
			log.info("Email missing in GitHub attributes, calling GitHub API with token...");
			email = authUtil.fetchGitHubEmailUsingToken(accessToken);
		}

		if (email == null || email.isBlank()) {
			log.info("Email null, attempting fallback fetch for provider: {}", registrationId);
			email = authUtil.getProviderEmailFromUser(oAuth2User, registrationId, providerId);
		}


		if (email == null || !email.contains("@")) {
			log.error("Invalid email fetched from {}: {}", registrationId, email);
			throw new IllegalArgumentException("Could not retrieve a valid email address. Please make your email primary/public in " + registrationId);
		}

		// 3. Database Checks
		User user = userRepository.findByOauthProviderIdAndOauthProviderName(providerId, authProviderName).orElse(null);
		if (user != null) {
			return userService.addRoleToExistingUser(user, role);
		}

		User userByEmail = userRepository.findByEmail(email).orElse(null);
		if (userByEmail != null) {
			log.info("Email already registered with another provider : {}", email);
			return userService.addRoleToExistingUser(userByEmail, role);
		}

		UserResponse userResponse = userService.registerUser(
				RegisterRequest.builder()
						.email(email)
						.oauthProviderName(authProviderName)
						.oauthProviderId(providerId)
						.password(passwordEncoder.encode(email))
						.firstName(authUtil.getFirstNameFromUser(oAuth2User, registrationId))
						.lastName(authUtil.getLastNameFromUser(oAuth2User, registrationId))
						.approvalStatus(ApprovalStatus.APPROVED)
						.role(role)
						.build()
		);

		return userRepository.findById(userResponse.getId()).orElseThrow();
	}

}
