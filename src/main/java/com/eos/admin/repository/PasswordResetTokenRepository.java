package com.eos.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eos.admin.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);
}