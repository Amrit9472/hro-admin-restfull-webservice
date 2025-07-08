package com.eos.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import com.eos.admin.dto.CandidatesDTO;

public interface CandidatesService {

	CandidatesDTO saveCandidates(CandidatesDTO candidatesDTO);

	List<CandidatesDTO> saveCandidatesMultiple(List<CandidatesDTO> candidatesDTO);
	
	public List<CandidatesDTO> findBySubmittedDateBetweenAndVendorEmail(LocalDateTime start, LocalDateTime end, String vendorEmail);
	
	public String getManagerStatusByEmail(String email);

	List<CandidatesDTO> getCandidatesByVendor(String venId, String vendorEmail);

	List<CandidatesDTO> findAllByVendorEmailLifo(String vendorEmail);
}