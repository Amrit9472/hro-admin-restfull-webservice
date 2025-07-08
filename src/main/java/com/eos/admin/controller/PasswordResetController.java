package com.eos.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eos.admin.service.PasswordResetService;

@RestController
@RequestMapping("/api/passwordRest")
@CrossOrigin("*")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam("email") String email) {
        passwordResetService.createPasswordResetToken(email);
        
        Map<String, String> response = new HashMap();
        response.put("message", "Password reset link sent to your email");
        
        return ResponseEntity.ok(response);
    }


    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword) {
        
        passwordResetService.resetPassword(token, newPassword);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password successfully reset");

        return ResponseEntity.ok(response);
    }

}