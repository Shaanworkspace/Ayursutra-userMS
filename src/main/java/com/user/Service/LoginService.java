package com.user.Service;

import com.user.Client.PatientClient;
import com.user.DTO.Request.LoginRequest;
import com.user.DTO.Response.LoginResponse;
import com.user.Entity.User;
import com.user.Enum.ApprovalStatus;
import com.user.Enum.Role;
import com.user.Repository.UserRepository;
import com.user.JWT.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor

public class LoginService {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final  UserService userService;
	private final PatientClient patientClient;

	public LoginResponse login(LoginRequest request) {

		Authentication auth = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getPassword()
				)
		);

		User user = (User) auth.getPrincipal();
		log.info("user Extracted while login from authentication : {}",user);
		Role role = request.getRole();

		// Role assigned check
		if (!user.getRoles().contains(role)) {
			return loginToLoginResponse(
					user,
					role,
					ApprovalStatus.NOREQUEST,
					false
			);
		}


		if (role == Role.PATIENT) {
			return loginToLoginResponse(
					user,
					Role.PATIENT,
					ApprovalStatus.APPROVED,
					true
			);
		}


		// ===== DOCTOR =====
		if (role == Role.DOCTOR) {
			return buildRoleBasedResponse(
					user,
					role,
					user.getApprovalStatusOfDoctor()
			);
		}

		// ===== THERAPIST =====
		if (role == Role.THERAPIST) {
			return buildRoleBasedResponse(
					user,
					role,
					user.getApprovalStatusOfTherapist()
			);
		}

		// fallback (should never happen)
		throw new RuntimeException("Invalid role");
	}



	private LoginResponse buildRoleBasedResponse(
			User user,
			Role role,
			ApprovalStatus status
	) {
		return switch (status) {

			case APPROVED -> loginToLoginResponse(
					user,
					role,
					ApprovalStatus.APPROVED,
					true
			);

			case PENDING -> loginToLoginResponse(
					user,
					role,
					ApprovalStatus.PENDING,
					false
			);

			case REJECTED -> loginToLoginResponse(
					user,
					role,
					ApprovalStatus.REJECTED,
					false
			);

			case NOREQUEST -> loginToLoginResponse(
					user,
					role,
					ApprovalStatus.NOREQUEST,
					false
			);
		};
	}


	public LoginResponse loginToLoginResponse(
			User user,
			Role role,
			ApprovalStatus status,
			boolean includeJwt
	) {
		return LoginResponse.builder()
				.jwt(includeJwt ? jwtUtil.generateUserToServiceToken(user) : null)
				.role(role)
				.approvalStatus(status)
				.userResponse(
						user != null ? userService.mapToUserResponse(user) : null
				)
				.build();
	}


}

