package com.user.DTO.Response;
import com.user.Enum.Role;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserResponse {
	private Long id;
	private String auth0Id;
	private String email;
	private String firstName;
	private String lastName;
	private List<Role> roles;
}