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

		if (
				(request.getRole() == Role.DOCTOR || request.getRole() == Role.THERAPIST)
						&& user.getApprovalStatus() != ApprovalStatus.APPROVED
		) {
			throw new RuntimeException(
					user.getApprovalStatus() == ApprovalStatus.PENDING
							? "Your account is under review"
							: "Your registration was rejected: " + user.getRejectionReason()
			);
		}


		if (!user.getRoles().contains(request.getRole())) {
			throw new RuntimeException("Role not assigned");
		}

		String token = jwtUtil.generateToken(user);

		return LoginResponse.builder()
				.jwt(token)
				.role(request.getRole())
				.build();
	}
}

