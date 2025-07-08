package com.eos.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eos.admin.entity.PincodeDirectoryEntity;

public interface PincodeRepository extends JpaRepository<com.eos.admin.entity.PincodeDirectoryEntity, String> {
    Optional<PincodeDirectoryEntity> findByPincode(String pincode);

}
