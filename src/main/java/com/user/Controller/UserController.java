package com.user.Controller;


import com.user.DTO.Request.RegisterRequest;
import com.user.DTO.Response.LoginResponse;
import com.user.DTO.Response.UserResponse;
import com.user.Entity.User;
import com.user.Enum.ApprovalStatus;
import com.user.Repository.UserRepository;
import com.user.Service.LoginService;
import com.user.Service.UserService;
import com.user.DTO.Request.LoginRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Service APIs",description = "Endpoints for creating, retrieving, User entries")
public class UserController {
	private final UserService userService;
	private final LoginService loginService;
	private final UserRepository userRepository;



	@GetMapping("/health")
	public ResponseEntity<String> health() {
		return ResponseEntity.ok("USER SERVICE UP");
	}

	@PostMapping("/pre-login")
	public ResponseEntity<Void> storeRole(
			HttpServletRequest request,
			@RequestBody Map<String, String> body
	) {
		String role = body.get("role");
		request.getSession().setAttribute("OAUTH_ROLE", role);
		return ResponseEntity.ok().build();
	}


	@GetMapping("/oauth2/start/{provider}")
	public void startOAuth2(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable String provider,
			@RequestParam String role
	) throws IOException {
		// Store role in THIS session
		request.getSession().setAttribute("OAUTH_ROLE", role);
		log.info("Stored role {} in session, redirecting to OAuth2", role);

		// Redirect in the SAME session to OAuth2
		response.sendRedirect("/oauth2/authorization/" + provider);
	}


	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse register(@RequestBody RegisterRequest request) {
		return userService.registerUser(request);
	}

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.CREATED)
	public LoginResponse login(@RequestBody LoginRequest request) {
		return loginService.login(request);
	}

	@GetMapping()
	public List<User> getAllUser() {
		return userRepository.findAll();
	}


	//Staus changing API
	@PutMapping("/therapist/{userId}")
	public ResponseEntity<String> approveTherapist(@PathVariable String userId) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));


		user.setApprovalStatusOfTherapist(ApprovalStatus.APPROVED);
		//sync to doctor
		log.info("Lets Sync the Therapist");
		userService.syncTherapistIfRequired(user);
		log.info("Complete Sync the Therapist");
		userRepository.save(user);

		return ResponseEntity.ok("Therapist approval granted");
	}

	@PutMapping("/doctor/{userId}")
	public ResponseEntity<String> approveDoctor(@PathVariable String userId) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));

		//sync to doctor
		log.info("Lets Sync the Doc");
		userService.syncDoctorIfRequired(user);
		log.info("Complete Sync the Doc");
		user.setApprovalStatusOfDoctor(ApprovalStatus.APPROVED);
		userRepository.save(user);

		return ResponseEntity.ok("Doctor approval granted");
	}
}