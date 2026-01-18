package com.user.Client;


import com.user.DTO.Request.PatientRegisterRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
		name ="patient-service",
		url = "${services.patient.url}"
)
public interface PatientClient {
	@GetMapping("/api/patients/email/{email}")
	Object getPatientByEmail(@PathVariable String email);

	@PostMapping("/api/patients")
	Object storePatient(PatientRegisterRequestDTO patientRegisterRequestDTO);
	@GetMapping("/api/patients/emailCheck/{email}")
	Boolean checkPatientByEmail(@PathVariable String email);
}
