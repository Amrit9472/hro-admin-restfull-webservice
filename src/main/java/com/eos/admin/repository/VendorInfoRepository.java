package com.eos.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eos.admin.entity.VendorInfo;

@Repository
public interface VendorInfoRepository extends JpaRepository<VendorInfo, Long> {
	
	Optional<VendorInfo> findByEmail(String email);

	List<VendorInfo> findByCompanyNameIn(List<String> vendorNames);
	
	@Query("SELECT v.id FROM VendorInfo v WHERE v.companyName = :companyName")
	Optional<Long> findIdByCompanyName(@Param("companyName") String companyName);
	
	
	Optional<VendorInfo> findByCompanyName(String companyName);

}

    