package com.user.Controller;


import com.user.DTO.Request.RegisterRequest;
import com.user.DTO.Response.LoginResponse;
import com.user.DTO.Response.UserResponse;
import com.user.Entity.User;
import com.user.Repository.UserRepository;
import com.user.Service.LoginService;
import com.user.Service.UserService;
import com.user.DTO.Request.LoginRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
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
}