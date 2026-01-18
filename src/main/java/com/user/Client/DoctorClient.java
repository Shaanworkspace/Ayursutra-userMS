package com.user.Client;

//import com.user.DTO.Response.DoctorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
		name ="doctor-service",
		url = "${services.doctor.url}"
)
public interface DoctorClient {
//	@GetMapping("/patients/user/{userId}")
//	DoctorResponse getDoctorByUserId(@PathVariable Long userId);

}
