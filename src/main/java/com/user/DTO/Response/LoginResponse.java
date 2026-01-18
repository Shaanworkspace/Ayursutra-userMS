package com.user.DTO.Response;

import com.user.Enum.ApprovalStatus;
import com.user.Enum.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
	private String jwt;
	public Role role;
	private ApprovalStatus approvalStatus;
	private String rejectionReason;
}
