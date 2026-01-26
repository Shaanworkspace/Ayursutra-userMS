package com.user.Service;


import com.user.Client.DoctorClient;
import com.user.Client.PatientClient;
import com.user.Client.TherapistClient;
import com.user.DTO.Request.RegisterRequestToOtherServices;
import com.user.DTO.Request.RegisterRequest;
import com.user.DTO.Response.UserResponse;
import com.user.Entity.User;
import com.user.Enum.ApprovalStatus;
import com.user.Enum.Role;
import com.user.Repository.UserRepository;
import com.user.JWT.JwtUtil;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
	private final PatientClient patientClient;
	private final DoctorClient doctorClient;
	private final TherapistClient therapistClient;
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
				.firstName(request.getFirstName())
				.lastName(request.getLastName())
				.password(request.getPassword())
				.roles(Set.of(role));

		switch (role) {

			case PATIENT -> {
				// Patient is auto-approved (no column needed)
			}

			case DOCTOR -> {
				builder.approvalStatusOfDoctor(
						status != null ? status : ApprovalStatus.APPROVED
				);
			}

			case THERAPIST -> {
				builder.approvalStatusOfTherapist(
						status != null ? status : ApprovalStatus.APPROVED
				);
			}

			case ADMIN -> {
				builder.approvalStatusOfAdmin(
						status != null ? status : ApprovalStatus.PENDING
				);
			}
		}

		User saved = userRepository.save(builder.build());
		return mapToUserResponse(saved);
	}


	public UserResponse mapToUserResponse(User user) {
		return UserResponse.builder()
				.id(user.getId())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
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
//				user.setApprovalStatusOfDoctor(ApprovalStatus.PENDING);
				// For development Choosing Accepted
				user.setApprovalStatusOfDoctor(ApprovalStatus.APPROVED);
			}
			case THERAPIST -> {
//				user.setApprovalStatusOfTherapist(ApprovalStatus.PENDING);
				user.setApprovalStatusOfTherapist(ApprovalStatus.APPROVED);
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


	public void syncPatientIfRequired(User user) {
		boolean exists = patientClient.checkPatientByUserId(user.getId());
		log.info("Patient Exist in Patient DB");

		if (exists) return;

		log.info("Patient NOT Exist in Patient DB, So Saving it ...");

		Object o = patientClient.storePatient(toRegisterRequest(user));

		log.info("SuccessFully Sync Id: {} with object : {}", user.getId(), o);
	}



	public void syncDoctorIfRequired(User user) {
		Boolean exists = doctorClient.checkDoctorByUserId(user.getId());
		if (Boolean.TRUE.equals(exists)) return;

		log.info("Saving in Doctor DB ...");

		Object o = doctorClient.storeDoctor(toRegisterRequest(user));

		log.info("SuccessFully Sync Doc Id: {} with object : {}", user.getId(), o);
	}



	public void syncTherapistIfRequired(User user) {
		Boolean exists = therapistClient.checkTherapistByUserId(user.getId());
		if (Boolean.TRUE.equals(exists)) return;

		log.info("Saving in Therapist DB ...");

		Object o = therapistClient.storeTherapist(toRegisterRequest(user));

		log.info("SuccessFully Sync Therapist Id: {} with object : {}", user.getId(), o);
	}


	private RegisterRequestToOtherServices toRegisterRequest(User user) {
		return RegisterRequestToOtherServices.builder()
				.userId(user.getId())
				.name(user.getFirstName() +" "+user.getLastName())
				.password(user.getPassword())
				.build();
	}
}