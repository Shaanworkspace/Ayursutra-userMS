package com.user.Security;

import com.user.Client.PatientClient;
import com.user.DTO.Request.PatientRegisterRequestDTO;
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

import java.util.List;

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
	private final PatientClient patientClient;

	public ResponseEntity<LoginResponse> handleOAuth2LoginRequest(
			OAuth2User oAuth2User,
			String registrationId,
			Role role
	) {

		// Register / Fetch user
		User user = findOrCreateUser(oAuth2User, registrationId,role);

		// Generate JWT
		String jwt = generateJwtForUser(user);
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(
						user,
						jwt,
						user.getAuthorities()
				);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		switch (role) {

			case PATIENT -> {
				syncPatientIfRequired(oAuth2User, user, registrationId);

				return ResponseEntity.ok(
						LoginResponse.builder()
								.jwt(jwt)
								.role(role)
								.approvalStatus(ApprovalStatus.APPROVED)
								.build()
				);
			}

			case DOCTOR, THERAPIST -> {

				if (user.getApprovalStatus() == ApprovalStatus.APPROVED) {

					return ResponseEntity.ok(
							LoginResponse.builder()
									.jwt(jwt)
									.role(role)
									.approvalStatus(ApprovalStatus.APPROVED)
									.build()
					);

				}

				if (user.getApprovalStatus() == ApprovalStatus.PENDING) {
					return ResponseEntity.status(202).body(
							LoginResponse.builder()
									.role(role)
									.approvalStatus(ApprovalStatus.PENDING)
									.build()
					);
				}

				// REJECTED
				return ResponseEntity.status(403).body(
						LoginResponse.builder()
								.role(role)
								.approvalStatus(ApprovalStatus.REJECTED)
								.rejectionReason(user.getRejectionReason())
								.build()
				);
			}

			default -> throw new IllegalStateException("Unexpected role: " + role);
		}
	}
	private User findOrCreateUser(
			OAuth2User oAuth2User,
			String registrationId,
			Role role) {
		AuthProviderName authProviderName =
				authUtil.returnAuthProviderName(registrationId);

		String providerId =
				authUtil.getProviderIdFromUser(oAuth2User, registrationId);

		String email = oAuth2User.getAttribute("email");

		User user =
				userRepository
						.findByOauthProviderIdAndOauthProviderName(
								providerId,
								authProviderName
						)
						.orElse(null);

		if (user != null) return user;

		User userByEmail =
				userRepository.findByEmail(email).orElse(null);

		if (userByEmail != null) {
			log.info(
					"Email already registered with another provider"
			);
			return userByEmail;
		}

		// ---- signup flow ----
		String fetchEmail =
				authUtil.getProviderEmailFromUser(
						oAuth2User,
						registrationId,
						providerId
				);

		if (!fetchEmail.endsWith("@gmail.com")) {
			throw new IllegalArgumentException("Invalid email");
		}

		UserResponse userResponse =
				userService.registerUser(
						RegisterRequest.builder()
								.email(fetchEmail)
								.password(passwordEncoder.encode(fetchEmail))
								.role(role)
								.build()
				);

		return userRepository
				.findById(userResponse.getId())
				.orElseThrow();
	}

	private String generateJwtForUser(User user) {
		return jwtUtil.generateToken(user);
	}

	private void syncPatientIfRequired(
			OAuth2User oAuth2User,
			User user,
			String registrationId
	) {
		String email = user.getEmail();

		Boolean exists = patientClient.checkPatientByEmail(email);

		if (Boolean.TRUE.equals(exists)) return;

		patientClient.storePatient(
				PatientRegisterRequestDTO.builder()
						.userId(user.getId())
						.email(email)
						.firstName(
								authUtil.getFirstNameFromUser(
										oAuth2User,
										registrationId
								)
						)
						.lastName(
								authUtil.getLastNameFromUser(
										oAuth2User,
										registrationId
								)
						)
						.build()
		);
	}




}
