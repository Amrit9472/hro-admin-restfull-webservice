package com.eos.admin.service;

import com.eos.admin.dto.DetailedFormDTO;
import com.eos.admin.dto.VendorInfoDTO;
import com.eos.admin.dto.VendorVerificationDTO;
import com.eos.admin.enums.VendorDetailsVerification;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface VendorInfoService {

	// Save VendorInfo and return the corresponding DTO
	VendorInfoDTO saveVendorInfo(VendorInfoDTO vendorInfoDto);

	// Get VendorInfo by ID and return the corresponding DTO
	VendorInfoDTO getVendorInfoById(Long vendorId);

	// Get all VendorInfos and return a list of DTOs
	List<VendorInfoDTO> getAllVendorInfos();

	// Delete VendorInfo by ID
	void deleteVendorInfo(Long id);

	VendorInfoDTO createVendorInfo(DetailedFormDTO detailedFormDTO, MultipartFile chequeImage) throws IOException;

	String verifyVendorDetails(Long id, VendorVerificationDTO vendorVerificationDTO);
	
	VendorDetailsVerification getVendorVerificationStatusByEmail(String email);
}