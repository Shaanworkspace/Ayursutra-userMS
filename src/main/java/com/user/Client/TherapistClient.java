package com.user.Client;

import com.user.DTO.Request.RegisterRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
		name ="therapist-service",
		url = "${services.therapist.url}"
)
public interface TherapistClient {

	@PostMapping("/api/therapist")
	Object storeTherapist(RegisterRequestDTO build);
	@GetMapping("/api/therapist/exist/{id}")
	Boolean checkTherapistByUserId(String id);
}
