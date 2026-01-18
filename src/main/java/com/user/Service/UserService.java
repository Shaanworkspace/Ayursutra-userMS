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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
	private final PatientClient patientClient;
	private final DoctorClient doctorClient;
	private final UserRepository userRepository;

	private final JwtUtil jwtUtil;

	public UserResponse registerUser(RegisterRequest request) {

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException(
					"User already exists with email " + request.getEmail()
			);
		}

		Role role = request.getRole();
		ApprovalStatus status = request.getApprovalStatus();

		User.UserBuilder builder = User.builder()
				.email(request.getEmail())
				.password(request.getPassword())
				.roles(Set.of(role));

		switch (role) {

			case PATIENT -> {
				// Patient is auto-approved (no column needed)
			}

			case DOCTOR -> {
				builder.approvalStatusOfDoctor(
						status != null ? status : ApprovalStatus.PENDING
				);
			}

			case THERAPIST -> {
				builder.approvalStatusOfTherapist(
						status != null ? status : ApprovalStatus.PENDING
				);
			}

			case ADMIN -> {
				builder.approvalStatusOfAdmin(
						status != null ? status : ApprovalStatus.PENDING
				);
			}
		}

		User saved = userRepository.save(builder.build());
		return mapToResponse(saved);
	}



	public UserResponse mapToResponse(User user) {
		return UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.roles(user.getRoles())
				.build();
	}
	public User addRoleToExistingUser(User user, Role role) {
		if (user.getRoles().contains(role)) {
			return user;
		}

		user.getRoles().add(role);
		switch (role) {
			case DOCTOR -> {
				user.setApprovalStatusOfDoctor(ApprovalStatus.PENDING);
			}
			case THERAPIST -> {
				user.setApprovalStatusOfTherapist(ApprovalStatus.PENDING);
			}
			case ADMIN -> {
				user.setApprovalStatusOfAdmin(ApprovalStatus.PENDING);
			}
		}

		return userRepository.save(user);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByEmail(username).orElseThrow();
	}
}