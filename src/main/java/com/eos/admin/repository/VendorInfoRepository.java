package com.eos.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eos.admin.entity.VendorInfo;

@Repository
public interface VendorInfoRepository extends JpaRepository<VendorInfo, Long> {
	
	Optional<VendorInfo> findByEmail(String email);
}
    