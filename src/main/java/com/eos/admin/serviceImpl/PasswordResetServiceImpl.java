package com.eos.admin.serviceImpl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eos.admin.entity.PasswordResetToken;
import com.eos.admin.entity.Vendor;
import com.eos.admin.repository.PasswordResetTokenRepository;
import com.eos.admin.repository.VendorRepository;
import com.eos.admin.service.PasswordResetService;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

	private VendorRepository vendorRepository;
	private PasswordResetTokenRepository tokenRepository;
	private EmailServiceImpl emailServiceImpl;
	private final PasswordEncoder passwordEncoder;

	public PasswordResetServiceImpl(VendorRepository vendorRepository, PasswordResetTokenRepository tokenRepository,
			EmailServiceImpl emailServiceImpl, PasswordEncoder passwordEncoder) {
		super();
		this.vendorRepository = vendorRepository;
		this.tokenRepository = tokenRepository;
		this.emailServiceImpl = emailServiceImpl;
		this.passwordEncoder = passwordEncoder;
	}

	public void createPasswordResetToken(String email) {
		Optional<Vendor> optionalVendor = vendorRepository.findByEmail(email);
		if (!optionalVendor.isPresent()) {
			// Handle vendor not found, e.g., throw exception
			throw new RuntimeException("Vendor not found with email: " + email);
		}
		Vendor vendor = optionalVendor.get();

		String token = UUID.randomUUID().toString();

		PasswordResetToken resetToken = new PasswordResetToken();
		resetToken.setToken(token);
		resetToken.setVendor(vendor);
		resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
		tokenRepository.save(resetToken);
		emailServiceImpl.sendPasswordResetEmail(vendor.getEmail(), token);
	}

	public boolean validatePasswordResetToken(String token) {
		PasswordResetToken resetToken = tokenRepository.findByToken(token);
		if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
			return false;
		}
		return true;
	}

	public void resetPassword(String token, String newPassword) {
		PasswordResetToken resetToken = tokenRepository.findByToken(token);
		if (resetToken != null && validatePasswordResetToken(token)) {
			Vendor vendor = resetToken.getVendor();
			// encode new password
			vendor.setPassword(passwordEncoder.encode(newPassword));
			vendorRepository.save(vendor);
			// delete token after use
			tokenRepository.delete(resetToken);
		}
	}
}
