package com.eos.admin.service;

import java.util.List;
import java.util.Optional;

import com.eos.admin.dto.SchemeVendorDTO;
import com.eos.admin.dto.SchemesDto;
import com.eos.admin.entity.Schemes;

public interface SchemesService {

	Schemes createSchemeWithVendorNames(SchemesDto dto);

	List<Schemes> getAvailableSchemesForVendor(Long vendorId);

	List<Schemes> getSchemes(Integer month, Integer year, Long vendorId, String sourceType);

	List<Schemes> findByIsDefault(boolean isDefault);

	Optional<Schemes> findByScheme(String scheme);


	Optional<Schemes> update(Long id, Schemes updated);

	List<SchemeVendorDTO> getSchemeVendorMapping();

	void addVendorsToScheme(Long schemeId, List<String> vendorNames);

	void removeVendorsFromScheme(Long schemeId, List<String> vendorNames);


}
