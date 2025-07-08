package com.eos.admin.serviceImpl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eos.admin.dto.ChangePasswordRequest;
import com.eos.admin.dto.ReqRes;
import com.eos.admin.entity.OurUsers;
import com.eos.admin.entity.Vendor;
import com.eos.admin.jwt.JWTUtilsImpl;
import com.eos.admin.repository.VendorRepository;
import com.eos.admin.service.EmailService;

@Service
public class VendorAuthService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtilsImpl jwtUtils;
    
    @Autowired
    private EmailService emailService;
    @Autowired
    @Qualifier("vendorAuthenticationManager")
    private AuthenticationManager vendorAuthenticationManager;

    public ReqRes registerVendor(ReqRes request) {
        ReqRes resp = new ReqRes();
        Optional<Vendor> existing = vendorRepository.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            resp.setStatusCode(400);
            resp.setError("Vendor already exists.");
            return resp;
        }

        Vendor vendor = new Vendor();
        vendor.setEmail(request.getEmail());
        vendor.setName(request.getName());
        vendor.setAddress(request.getAddress()); // Use another field for company
        vendor.setPassword(passwordEncoder.encode(request.getPassword()));
        vendor.setRole("VENDOR");

        vendorRepository.save(vendor);
        emailService.sendRegistrationEmail(vendor.getEmail(), request.getPassword(), request.getName());
        resp.setStatusCode(200);
        resp.setMessage("Vendor registered.");
        return resp;
    }

    public ReqRes loginVendor(ReqRes loginRequest) {
        ReqRes resp = new ReqRes();

        try {
        	vendorAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            Vendor vendor = vendorRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
            String jwt = jwtUtils.generateToken(vendor);
            String refreshToken = jwtUtils.generateRefreshToken(vendor);

            resp.setToken(jwt);
            resp.setRefreshToken(refreshToken);
            resp.setEmail(vendor.getEmail());
            resp.setName(vendor.getName());
            resp.setRole(vendor.getRole());
            resp.setId(vendor.getId());
            resp.setStatusCode(200);
            resp.setMessage("Vendor login successful.");
        } catch (org.springframework.security.core.AuthenticationException ex) {
            ex.printStackTrace(); // Show actual cause
            resp.setStatusCode(401);
            resp.setMessage("Authentication failed: " + ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Optional for logging
            resp.setStatusCode(500);
            resp.setMessage("Unexpected error: " + e.getMessage());
        }

        return resp;
    }

    public ReqRes changePassword(ChangePasswordRequest changePasswordRequest) {
		ReqRes resp = new ReqRes();
		try {
			String email = changePasswordRequest.getEmail();
			String oldPassWord = changePasswordRequest.getOldPassword();
			String newPassWord = changePasswordRequest.getNewPassword();
			String confirmPassword = changePasswordRequest.getConfirmPassword();

			if (email != null && !email.isEmpty() && oldPassWord != null && !oldPassWord.isEmpty()
					&& newPassWord != null && !newPassWord.isEmpty() && confirmPassword != null
					&& !confirmPassword.isEmpty()) {

				if (!newPassWord.equals(confirmPassword)) { // Check if newPassword and confirmPassword match
					resp.setStatusCode(400);
					resp.setError("New password and confirm password do not match");
					return resp;
				}

				Optional<Vendor> existingUser = vendorRepository.findByEmail(email);
				if (existingUser.isPresent()) {
					Vendor user = existingUser.get();
					if (passwordEncoder.matches(oldPassWord, user.getPassword())) {
						// Update the password
						user.setPassword(passwordEncoder.encode(newPassWord));
						vendorRepository.save(user);

						resp.setMessage("Password changed successfully");
						resp.setStatusCode(200);
					} else {
						resp.setStatusCode(400);
						resp.setError("Old password is incorrect");
					}
				} else {
					resp.setStatusCode(404);
					resp.setError("User not Found");
				}
			} else {
				resp.setStatusCode(400);
				resp.setError("All fields are required");
			}

		} catch (Exception e) {
			resp.setStatusCode(500);
			resp.setError("An error occurred: " + e.getMessage());
		}

		return resp;
	}

}
