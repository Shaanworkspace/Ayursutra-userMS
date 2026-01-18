package com.user.Repository;


import com.user.Entity.User;
import com.user.Enum.AuthProviderName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);

	Optional<User> findByOauthProviderIdAndOauthProviderName(String registrationId, AuthProviderName authProviderName);
}