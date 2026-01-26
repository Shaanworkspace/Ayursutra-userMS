package com.user.Client;

import com.user.DTO.Request.RegisterRequestToOtherServices;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
		name ="therapist-service",
		url = "${services.therapist.url}"
)
public interface TherapistClient {

	@PostMapping("/api/therapists")
	Object storeTherapist(RegisterRequestToOtherServices build);

	@GetMapping("/api/therapists/exist/{email}")
	boolean checkTherapistByEmailId(@PathVariable String email) ;
}
