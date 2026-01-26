package com.user.Client;

//import com.user.DTO.Response.DoctorResponse;
import com.user.DTO.Request.RegisterRequestToOtherServices;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
		name ="doctor-service",
		url = "${services.doctor.url}"
)
public interface DoctorClient {
	@GetMapping("/api/doctors/check/{userId}")
	Boolean checkDoctorByUserId(@PathVariable String userId);

	@PostMapping("/api/doctors")
	Object storeDoctor(RegisterRequestToOtherServices build);
}
