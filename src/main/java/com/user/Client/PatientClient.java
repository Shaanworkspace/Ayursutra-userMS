package com.user.Client;


import com.user.DTO.Request.RegisterRequestToOtherServices;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
		name ="patient-service",
		url = "${services.patient.url}"
)
public interface PatientClient {
	@PostMapping("/api/patients")
	Object storePatient(RegisterRequestToOtherServices patientRegisterRequestDTO);
	@GetMapping("/api/patients/check/{userId}")
	boolean checkPatientByUserId(@PathVariable String userId);
}
