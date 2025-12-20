package com.user.Controller;


import com.user.DTO.Request.RegisterRequest;
import com.user.DTO.Response.UserResponse;
import com.user.Entity.User;
import com.user.Repository.UserRepository;
import com.user.Service.UserService;
import com.user.DTO.Request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	private final UserRepository userRepository;

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse register(@RequestBody RegisterRequest request) {
		return userService.registerUser(request);
	}

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse login(@RequestBody LoginRequest request) {
		User u = userRepository.findByEmail(request.getEmail()).orElseThrow(()-> new RuntimeException("User not Found"));
		return userService.mapToResponse(u);
	}


	@GetMapping("/{auth0Id}")
	public UserResponse getUserByAuth0Id(@PathVariable String auth0Id) {
		return userService.getUserByAuth0Id(auth0Id);
	}
}