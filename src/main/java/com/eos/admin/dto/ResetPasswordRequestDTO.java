package com.eos.admin.dto;
public class ResetPasswordRequestDTO {
    private String token;
    private String newPassword;

    // Default constructor
    public ResetPasswordRequestDTO() {
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}