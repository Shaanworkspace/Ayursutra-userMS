package com.user.Service;


import com.user.Client.DoctorClient;
import com.user.Client.PatientClient;
import com.user.DTO.Request.RegisterRequest;
import com.user.DTO.Response.UserResponse;
import com.user.Entity.User;
import com.user.Enum.ApprovalStatus;
import com.user.Enum.Role;
import com.user.Repository.UserRepository;
import com.user.JWT.JwtUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
	private final PatientClient patientClient;
	private final DoctorClient doctorClient;
	private final UserRepository userRepository;

	private final JwtUtil jwtUtil;

	public UserResponse registerUser(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("User already exists with email " + request.getEmail());
		}

		Role role = request.getRole();

		User user = User.builder()
				.email(request.getEmail())
				.password(request.getPassword())
				.roles(Collections.singleton(request.getRole()))
				.approvalStatus(
						(role == Role.PATIENT)
								? ApprovalStatus.APPROVED
								: ApprovalStatus.PENDING
				)
				.build();

		User saved = userRepository.save(user);

		return mapToResponse(saved);
	}


	public UserResponse mapToResponse(User user) {
		return UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.roles(user.getRoles())
				.build();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByEmail(username).orElseThrow();
	}
}