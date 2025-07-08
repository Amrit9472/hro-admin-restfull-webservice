package com.eos.admin.service;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;

import com.eos.admin.dto.StatusRequestDTO;
import com.eos.admin.entity.EmployeeStatusDetails;
import com.eos.admin.entity.OurEmployees;

public interface EmailService {
    void sendRegistrationEmail(String toEmail, String rawPassword, String string);
	void sendScheduleInterviewEmailToManager(List<String> toEmail);
	void emailsendEmailWithAttachment(String email, String string, ByteArrayResource pdfResource);
	void emailsendEmailWithAttachment(String email, String string, ByteArrayResource pdfResource,
			OurEmployees savedEmployee);
	void sendScheduleInterviewEmailToManager(List<String> emailList, EmployeeStatusDetails employeeStatusDetails ,StatusRequestDTO statusRequestDTO);
	void sendPasswordResetEmail(String email, String token);	
}
