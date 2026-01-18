package com.user.DTO.Request;

import com.user.Enum.ApprovalStatus;
import com.user.Enum.Role;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
	private String email;
	private String password;
	private Role role;
	private ApprovalStatus approvalStatus;
}