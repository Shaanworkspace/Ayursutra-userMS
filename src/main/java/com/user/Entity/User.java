package com.user.Entity;


import com.user.Enum.ApprovalStatus;
import com.user.Enum.AuthProviderName;
import com.user.Enum.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
	@Id
	@Column(nullable = false, updatable = false)
	private String id;   // ULID or UUID

	@Column(unique = true)
	private String email;
	private String password;

	@Enumerated(EnumType.STRING)
	private AuthProviderName oauthProviderName;
	private String oauthProviderId;


	@ElementCollection(fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	Set<Role> roles = new HashSet<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApprovalStatus approvalStatusOfDoctor;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApprovalStatus approvalStatusOfTherapist;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApprovalStatus approvalStatusOfAdmin;

	@Column(length = 500)
	private String rejectionReason;
	@PrePersist
	public void prePersistDefaults() {

		if (approvalStatusOfDoctor == null) {
			approvalStatusOfDoctor = ApprovalStatus.NOREQUEST;
		}

		if (approvalStatusOfTherapist == null) {
			approvalStatusOfTherapist = ApprovalStatus.NOREQUEST;
		}

		if (approvalStatusOfAdmin == null) {
			approvalStatusOfAdmin = ApprovalStatus.NOREQUEST;
		}

		if (this.id == null) {
			this.id = UUID.randomUUID().toString();
		}
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles
				.stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
				.toList();
	}


	@Override
	public String getUsername() {
		return email;
	}
}
