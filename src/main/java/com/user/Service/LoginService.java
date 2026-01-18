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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final PatientClient patientClient;

	public LoginResponse login(LoginRequest request) {

		Authentication auth = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getPassword()
				)
		);

		User user = (User) auth.getPrincipal();
		Role role = request.getRole();

		// Role assigned check
		if (!user.getRoles().contains(role)) {
			return LoginResponse.builder()
					.role(role)
					.approvalStatus(ApprovalStatus.NOREQUEST)
					.build();
		}

		// ===== PATIENT =====
		if (role == Role.PATIENT) {
			return LoginResponse.builder()
					.jwt(jwtUtil.generateToken(user))
					.role(Role.PATIENT)
					.approvalStatus(ApprovalStatus.APPROVED)
					.build();
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
		switch (status) {

			case APPROVED -> {
				return LoginResponse.builder()
						.jwt(jwtUtil.generateToken(user))
						.role(role)
						.approvalStatus(ApprovalStatus.APPROVED)
						.build();
			}

			case PENDING -> {
				return LoginResponse.builder()
						.role(role)
						.approvalStatus(ApprovalStatus.PENDING)
						.build();
			}

			case REJECTED -> {
				return LoginResponse.builder()
						.role(role)
						.approvalStatus(ApprovalStatus.REJECTED)
						.rejectionReason(user.getRejectionReason())
						.build();
			}

			case NOREQUEST -> {
				return LoginResponse.builder()
						.role(role)
						.approvalStatus(ApprovalStatus.NOREQUEST)
						.build();
			}
		}

		throw new RuntimeException("Unhandled approval status");
	}

}

