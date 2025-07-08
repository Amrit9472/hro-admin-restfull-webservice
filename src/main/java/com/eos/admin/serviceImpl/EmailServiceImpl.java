package com.eos.admin.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.eos.admin.dto.StatusRequestDTO;
import com.eos.admin.entity.EmployeeStatusDetails;
import com.eos.admin.entity.OurEmployees;
import com.eos.admin.service.EmailService;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

//	@Value("${spring.mail1.username}")
//	private String fromEmail;

//	private final JavaMailSender javaMailSender;
//
//	public EmailServiceImpl(JavaMailSender javaMailSender) {
//		this.javaMailSender = javaMailSender;
//
//	}

	@Value("${custom-mail.mail1.username}")
	private String fromMail1;

	@Value("${custom-mail.mail2.username}")
	private String fromMail2;
	
	private final JavaMailSender mailSender1;
	private final JavaMailSender mailSender2;

	public EmailServiceImpl(@Qualifier("mailSender1") JavaMailSender mailSender1,
			@Qualifier("mailSender2") JavaMailSender mailSender2) {
		this.mailSender1 = mailSender1;
		this.mailSender2 = mailSender2;
	}


	@Override
	public void sendRegistrationEmail(String toEmail, String rawPassword, String vendorName) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(toEmail);
			message.setSubject("Welcome to EOS Globe – Vendor Portal Access & Onboarding Details");

			String emailContent = "Dear " + vendorName + "\n\n" + "Greetings from EOS Globe!\n\n"
					+ "We’re pleased to inform you that your empanelment with us has been successfully completed. Welcome aboard! "
					+ "We look forward to building a successful and collaborative partnership with you.\n\n"
					+ "Please find your login credentials for accessing the EOS Vendor Portal below:\n\n"
					+ "🔐 Vendor Portal Login Details\n" + "Portal URL: [Insert Portal Link]\n" + "Username: " + toEmail
					+ "\n" + "Temporary Password: " + rawPassword + "\n\n"
					+ "⚠️ Important: Please change your password upon first login for security purposes.\n\n"
					+ "📘 Getting Started – Portal User Guide\n" + "1. 📝 Complete Registration\n"
					+ "   - Log in and complete your vendor registration form.\n"
					+ "   - Upload all required documents for verification.\n"
					+ "   - Your submission will undergo a thorough audit process.\n"
					+ "   - Note: Payouts will only be processed after successful registration approval.\n\n"
					+ "2. 👥 Candidate Line-up\n" + "   - Enter candidate details individually, or\n"
					+ "   - Upload candidates in bulk using the prescribed template.\n\n"
					+ "3. 📊 Track Candidate Status\n"
					+ "   - Monitor each candidate's journey: Selected, Rejected, Onboarded, Joined, Active, Inactive.\n\n"
					+ "4. ❓ Raise a Query\n"
					+ "   - Use this feature to submit queries or flag any issues related to candidate data or system usage.\n\n"
					+ "5. 📁 Download Reports\n"
					+ "   - Access detailed reports for all candidates you’ve lined up.\n\n" + "📌 Please Note:\n"
					+ "This is a system-generated email. Replies to this message are not monitored. For assistance, please reach out through the “Raise a Query” section within the portal.\n\n"
					+ "We’re excited to have you with us!\n\n" + "Warm regards,\n" + "EOS Vendor Empanelment Team";

			message.setText(emailContent);
			message.setFrom(fromMail1);

//			javaMailSender.send(message);
			mailSender1.send(message);
		} catch (Exception e) {
			e.printStackTrace(); // Consider logging it instead
		}
	}

	@Override
	public void sendScheduleInterviewEmailToManager(List<String> toEmails) {
		if (toEmails == null || toEmails.isEmpty()) {
			log.warn("No recipient provided for interview schedule email.");
			throw new IllegalArgumentException("Email recipient cannot be null or empty.");
		}

		for (String email : toEmails) {
			if (!isValidEmail(email)) {
				log.error("Invalid email format: {}", email);
				throw new IllegalArgumentException("Invalid email address format: " + email);
			}
		}

		log.info("Preparing to send interview schedule email to: {}", toEmails);
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromMail1);
			message.setTo(toEmails.toArray(new String[0]));

			message.setSubject("Interview Schedule Notification");
			message.setText("Dear Manager,\n\n" + "Please be informed that an interview has been scheduled.\n\n"
					+ "Best regards,\nHR Team");
			log.debug("Sending email to: {}", toEmails);
//			javaMailSender.send(message);
			mailSender2.send(message);
			log.info("Interview schedule email sent successfully to: {}", toEmails);
		} catch (Exception e) {
			log.error("Failed to send interview schedule email to: {}", toEmails, e);
			throw new RuntimeException("Failed to send email. Reason: " + e.getMessage(), e);
		}
	}

	private boolean isValidEmail(String email) {
		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
		return email != null && email.matches(emailRegex);
	}

	@Override
	public void emailsendEmailWithAttachment(String toEmail, String filename, ByteArrayResource pdfResource) {
		try {
			MimeMessage message = mailSender1.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(fromMail1);
			helper.setTo(toEmail);
			helper.setSubject("Letter of Intent from EOS");
			helper.setText("Dear Candidate,\n\nPlease find attached your Letter of Intent.\n\nRegards,\nHR Department");

			helper.addAttachment(filename, pdfResource);

//			javaMailSender.send(message);
			mailSender1.send(message);
			log.info("Email with PDF attachment sent successfully to {}", toEmail);
		} catch (Exception e) {
			log.error("Failed to send email with attachment to {}: {}", toEmail, e.getMessage(), e);
			throw new RuntimeException("Failed to send email with attachment", e);
		}
	}

	@Override
	public void emailsendEmailWithAttachment(String toEmail, String filename, ByteArrayResource pdfResource,
			OurEmployees savedEmployee) {
		try {
			MimeMessage message = mailSender1.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(fromMail1);
			helper.setTo(toEmail);
			helper.setSubject("Letter of Intent from EOS");

			String emailContent = String.format("Dear %s,\n\n"
					+ "We are happy to announce that you have been selected for the position of %s for %s Department in Eureka Outsourcing Solutions\n\n"
					+ "Enclosed is your Letter of Intent for your reference.\n\n" + "Reference No - %s\n\n"
					+ "This Letter of Intent (LOI) will be valid for 7 days from the date of this email. "
					+ "If this LOI is acceptable to you, please let us know your acceptance of employment by confirming via email within 7 days of receiving this email. "
					+ "If we do not hear back from you within this period, this offer will be deemed to be cancelled & we are not obliged to hold the position open for you.\n\n"
					+ "Thanks,\n\n" + "Eureka Outsourcing Solutions.\n" + "Human Resource Department",
					savedEmployee.getEmployee().getFullName(), savedEmployee.getEmployee().getJobProfile(),
					savedEmployee.getDepartment(), savedEmployee.getEmployee().getId());

			helper.setText(emailContent);
			helper.addAttachment(filename, pdfResource);
			mailSender1.send(message);
			log.info("Email with PDF attachment sent successfully to {}", toEmail);
		} catch (Exception e) {
			log.error("Failed to send email with attachment to {}: {}", toEmail, e.getMessage(), e);
			throw new RuntimeException("Failed to send email with attachment", e);
		}
	}

	@Override
	public void sendScheduleInterviewEmailToManager(List<String> emailList, EmployeeStatusDetails employeeStatusDetails,
			StatusRequestDTO statusRequestDTO) {

		if (emailList == null || emailList.isEmpty()) {
			log.warn("No recipient provided for interview schedule email.");
			throw new IllegalArgumentException("Email recipient cannot be null or empty.");
		}

		for (String email : emailList) {
			if (!isValidEmail(email)) {
				log.error("Invalid email format: {}", email);
				throw new IllegalArgumentException("Invalid email address format: " + email);
			}
		}

		log.info("Preparing to send interview schedule email to: {}", emailList);

		try {
			MimeMessage message = mailSender1.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom(fromMail1);
			helper.setTo(emailList.toArray(new String[0]));
			helper.setSubject("Interview Scheduled – Candidate Details & Request for Support");

			String htmlContent = "<p>Dear Manager,</p>"
					+ "<p>We have scheduled an interview and request your support in completing the process as planned. Please find the candidate details below:</p>"
					+ "<p><strong>Candidate Name:</strong> " + employeeStatusDetails.getEmployee().getFullName()
					+ "<br>" + "<strong>Position Applied For:</strong> " + statusRequestDTO.getJobProfile() + "<br>"
					+ "<strong>Process Name:</strong> " + statusRequestDTO.getProcessName() + "</p>"
					+ "<p>Kindly ensure the interview is conducted as per the schedule.</p>"
					+ "<p>Click the link below to check more details:</p>"
					+ "<p><a href='http://localhost:5173/' target='_blank'>Click here to check</a></p>"
					+ "<p>Thank you for your support.<br><br>Best regards,<br>HR Team</p>";

			helper.setText(htmlContent, true); // `true` to indicate HTML

			log.debug("Sending HTML email to: {}", emailList);
//			javaMailSender.send(message);
			mailSender1.send(message);
			log.info("Interview schedule email sent successfully to: {}", emailList);

		} catch (Exception e) {
			log.error("Failed to send interview schedule email to: {}", emailList, e);
			throw new RuntimeException("Failed to send email. Reason: " + e.getMessage(), e);
		}
	}
	
	@Override
	public void sendPasswordResetEmail(String email, String token) {
		try {
			String resetUrl = "http://localhost:5173/reset-password?token=" + token;

			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(email);
			mailMessage.setFrom(fromMail1);  // using mailSender1
			mailMessage.setSubject("Password Reset Request");
			mailMessage.setText("Dear " + email + ",\n\n"
					+ "We received a request to reset your password.\n"
					+ "Please click the link below to reset your password:\n"
					+ resetUrl + "\n\n"
					+ "If you did not request this, please ignore this email.\n\n"
					+ "Best regards,\nYour Company Team");

			mailSender1.send(mailMessage);
			log.info("Password reset email sent to {}", email);

		} catch (Exception e) {
			log.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
			throw new RuntimeException("Failed to send password reset email", e);
		}
	}

}
