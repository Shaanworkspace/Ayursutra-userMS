package com.user.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestToOtherServices {
	// Used to Transfer Registration from User -> Patient and Doctor and other Services
	private String userId;
	private String name;
	private String password;
}
