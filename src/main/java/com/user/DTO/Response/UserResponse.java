package com.user.DTO.Response;
import com.user.Enum.Role;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserResponse {
	private String id;
	private String auth0Id;
	private String email;
	private String firstName;
	private String lastName;
	private Set<Role> roles;
}