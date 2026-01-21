package com.user.Client;


import com.user.DTO.Request.PatientRegisterRequestDTO;
import com.user.DTO.Request.RegisterRequestDTO;
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
	Object storePatient(RegisterRequestDTO patientRegisterRequestDTO);
	@GetMapping("/api/patients/check/{userId}")
	Boolean checkPatientByUserId(@PathVariable String userId);
}
