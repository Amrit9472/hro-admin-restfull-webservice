package com.eos.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eos.admin.dto.ChangePasswordRequest;
import com.eos.admin.dto.ReqRes;
import com.eos.admin.serviceImpl.VendorAuthService;

@CrossOrigin("*")
@RestController
@RequestMapping("/auth/vendor")
public class VendorAuthController {

	@Autowired
	private VendorAuthService vendorAuthService;

	@PostMapping("/register")
	public ResponseEntity<ReqRes> registerVendor(@RequestBody ReqRes request) {
		return ResponseEntity.ok(vendorAuthService.registerVendor(request));
	}

	@PostMapping("/login")
	public ResponseEntity<ReqRes> loginVendor(@RequestBody ReqRes request) {
		return ResponseEntity.ok(vendorAuthService.loginVendor(request));
	}

	@PostMapping("/change-password")
	public ResponseEntity<ReqRes> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
		return ResponseEntity.ok(vendorAuthService.changePassword(changePasswordRequest));
	}

}
