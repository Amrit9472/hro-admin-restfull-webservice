package com.eos.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eos.admin.entity.VendorLogEntity;

@Repository
public interface VendorLogRepository extends JpaRepository<VendorLogEntity, Long> {

}
