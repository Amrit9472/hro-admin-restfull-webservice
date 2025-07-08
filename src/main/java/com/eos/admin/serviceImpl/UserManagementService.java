package com.eos.admin.serviceImpl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eos.admin.dto.ChangePasswordRequest;
import com.eos.admin.dto.ReqRes;
import com.eos.admin.entity.OurUsers;
import com.eos.admin.jwt.JWTUtilsImpl;
import com.eos.admin.repository.UsersRepository;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class UserManagementService {

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private PasswordEncoder passwordEncode;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JWTUtilsImpl jwtUtilsImpl;
	@Autowired
	private PasswordEncoder passwordEncoder;

	public ReqRes register(ReqRes registrationRequest) {
		ReqRes resp = new ReqRes();
		try {
			// checking user having unique id
			String email = registrationRequest.getEmail();
			if (email != null && !email.isEmpty()) {
				Optional<OurUsers> existingUser = usersRepository.findByEmail(email);

				if (existingUser.isPresent()) {
					resp.setStatusCode(400);
					resp.setError("Emp id is already in use");
					return resp;
				}
			} else {
				resp.setStatusCode(400);
				resp.setError("Emp id cann't be empty");
			}

			// Create and populate the new user object
			OurUsers ourUsers = new OurUsers();
			ourUsers.setEmail(email);
			ourUsers.setCity(registrationRequest.getCity());
			ourUsers.setBranch(registrationRequest.getBranch());
			ourUsers.setRole(registrationRequest.getRole());
			ourUsers.setName(formattedString(registrationRequest.getName()));
			ourUsers.setOfficeEmail(registrationRequest.getOfficeEmail());
			String processName = registrationRequest.getProcess().trim().toUpperCase();
			String processCode = registrationRequest.getProcessCode().trim();
			ourUsers.setProcess(processName);
			ourUsers.setProcessCode(processCode);
			

			if((processName != null && !processName.isEmpty()) && (processCode != null && !processCode.isEmpty())) {
			String uniqueCode = processName + "-" + processCode;
			ourUsers.setUniqueCode(uniqueCode);
			}
			ourUsers.setPassword(passwordEncode.encode(registrationRequest.getPassword()));

			OurUsers ourUsersResult = usersRepository.save(ourUsers);
			if (ourUsersResult.getId() > 0) {
				resp.setOurUsers(ourUsersResult);
				resp.setMessage("User save sucessfully");
				resp.setStatusCode(200);
			}
		} catch (Exception e) {
			resp.setStatusCode(500);
			resp.setError("An error occurred: " + e.getMessage());
		}
		return resp;
	}

	{/**
	public ReqRes login(ReqRes loginRequest) {
		ReqRes response = new ReqRes();
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
			var user = usersRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
			var jwt = jwtUtilsImpl.generateToken(user);
//			var refreshToken = jwtUtilsImpl.generateRefreshToken(new HashMap<>(), user);
			var refreshToken = jwtUtilsImpl.generateRefreshToken(user);

			response.setStatusCode(200);
			response.setToken(jwt);
			response.setRole(user.getRole());
//			response.setProcess(user.getProcess());
			response.setName(user.getName());
			response.setEmail(user.getEmail());
			response.setRefreshToken(refreshToken);
			response.setCity(user.getCity());
			response.setBranch(user.getBranch());
			response.setExpirationTime("24Hrs");
			response.setMessage("SuccessFull login ");
			response.setUniqueCode(user.getUniqueCode());
		} catch (Exception e) {
			response.setStatusCode(500);
			response.setMessage("Incorrect Password");
		}
		return response;
	}
	*/}
	
	public ReqRes login(ReqRes loginRequest) {
	    ReqRes response = new ReqRes();
	    log.debug("Login attempt for email: {}", loginRequest.getEmail());
	    try {
	        authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
	        log.debug("Authentication successful for email: {}", loginRequest.getEmail());
	        var user = usersRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
	        var jwt = jwtUtilsImpl.generateToken(user);
	        var refreshToken = jwtUtilsImpl.generateRefreshToken(user);

	        response.setStatusCode(200);
	        response.setToken(jwt);
	        response.setRole(user.getRole());
	        response.setName(user.getName());
	        response.setEmail(user.getEmail());
	        response.setRefreshToken(refreshToken);
	        response.setCity(user.getCity());
	        response.setBranch(user.getBranch());
	        response.setExpirationTime("24Hrs");
	        response.setMessage("Successful login");
	        response.setUniqueCode(user.getUniqueCode());

	    } catch (BadCredentialsException e) {
	        log.warn("Failed login attempt for email: {}", loginRequest.getEmail());
	        response.setStatusCode(401);
	        response.setMessage("Incorrect email or password");
	    } catch (Exception e) {
	        log.error("Error during login: {}", e.getMessage(), e);
	        response.setStatusCode(500);
	        response.setMessage("An error occurred during login");
	    }
	    return response;
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

				Optional<OurUsers> existingUser = usersRepository.findByEmail(email);
				if (existingUser.isPresent()) {
					OurUsers user = existingUser.get();
					if (passwordEncoder.matches(oldPassWord, user.getPassword())) {
						// Update the password
						user.setPassword(passwordEncoder.encode(newPassWord));
						usersRepository.save(user);

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

	private String formattedString(String string) {

		if (string != null && !string.isEmpty()) {
			String formattedResponse = string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
		}
		return string;

	}

	public List<String> getMyProcessName() {
		List<String> processes = usersRepository.findDistinctProcesses();
		  return processes.stream()
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
	}

}
