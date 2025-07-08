package com.eos.admin.serviceImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.eos.admin.dto.SchemeVendorDTO;
import com.eos.admin.dto.SchemesDto;
import com.eos.admin.entity.Schemes;
import com.eos.admin.entity.VendorInfo;
import com.eos.admin.exception.EntityNotFoundException;
import com.eos.admin.repository.SchemeRepository;
import com.eos.admin.repository.VendorInfoRepository;
import com.eos.admin.service.SchemesService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SchemeServiceImpl implements SchemesService {

	private final VendorInfoRepository vendorInfoRepository;
	private final SchemeRepository schemeRepository;

	public SchemeServiceImpl(VendorInfoRepository vendorInfoRepository, SchemeRepository schemeRepository) {
		super();
		this.vendorInfoRepository = vendorInfoRepository;
		this.schemeRepository = schemeRepository;
	}

	@Override
	@Transactional
	public Schemes createSchemeWithVendorNames(SchemesDto dto) {
		log.info("Creating scheme for type: {}, grade: {}", dto.getType(), dto.getGrade());
		List<VendorInfo> vendors = vendorInfoRepository.findByCompanyNameIn(dto.getVendorNames());

		if (!"All".equals(dto.getType()) && vendors.isEmpty()) {
			log.warn("No vendors found for names: {}", dto.getVendorNames());
			throw new EntityNotFoundException("No vendors found with given names");
		}
		Schemes scheme = new Schemes();
		scheme.setType(dto.getType());
		scheme.setStartDate(dto.getStartDate());
		scheme.setEndDate(dto.getEndDate());
		scheme.setAmount(dto.getAmount());
		String combinedSchemeName = dto.getType() + "_" + dto.getGrade() + "_" + dto.getAmount();
		scheme.setScheme(combinedSchemeName);
		scheme.setProcess(String.join(",", dto.getProcess()));
		scheme.setDetails(dto.getDetails());
		scheme.setPayoutDays(dto.getPayoutDays());
		scheme.setGrade(dto.getGrade());
		scheme.setTargetCandidates(dto.getTargetCandidates());
		scheme.setIsDefault(dto.getIsDefault());
		scheme.setVendorInfoList(vendors);

		for (VendorInfo vendor : vendors) {
			if (vendor.getSchemes() == null) {
				vendor.setSchemes(new java.util.ArrayList<>());
			}
			vendor.getSchemes().add(scheme);
		}

		Schemes saved = schemeRepository.save(scheme);
		log.info("Scheme created successfully with ID: {}", saved.getId());
		return saved;

//		return schemeRepository.save(scheme);

	}

	@Override
	public List<Schemes> getAvailableSchemesForVendor(Long vendorId) {
		return schemeRepository.findAvailableSchemesForVendor(vendorId);
	}

	@Override
	public List<Schemes> getSchemes(Integer month, Integer year, Long vendorId, String sourceType) {
		List<Schemes> result = new ArrayList<>();

		switch (sourceType.toLowerCase()) {
		case "default":
			result = schemeRepository.findByIsDefaultTrue();
			break;

		case "vendor":
			if (vendorId != null) {
				result = schemeRepository.findByVendorId(vendorId).stream()
						.filter(s -> Boolean.FALSE.equals(s.getIsDefault())).collect(Collectors.toList());
			}
			break;

		case "all":
		default:
			Set<Schemes> combined = new HashSet<>();
			combined.addAll(schemeRepository.findByIsDefaultTrue());
			if (vendorId != null) {
				combined.addAll(schemeRepository.findByVendorId(vendorId));
			}
			result = new ArrayList<>(combined);
			break;
		}

		// Optional: apply date filter
		if (month != null && year != null) {
			result = result.stream().filter(s -> {
				Calendar cal = Calendar.getInstance();
				cal.setTime(s.getStartDate());
				return cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.YEAR) == year;
			}).collect(Collectors.toList());
		}

		return result;
	}

	@Override
	public List<Schemes> findByIsDefault(boolean isDefault) {
		return schemeRepository.findByIsDefault(isDefault);
	}

	@Override
	public Optional<Schemes> findByScheme(String schemeName) {
		return Optional.ofNullable(schemeRepository.findByScheme(schemeName).orElse(null));
	}

	@Override
	public Optional<Schemes> update(Long id, Schemes updated) {
		return schemeRepository.findById(id).map(existing -> {
			existing.setType(updated.getType());
			existing.setStartDate(updated.getStartDate());
			existing.setEndDate(updated.getEndDate());
			existing.setAmount(updated.getAmount());
			existing.setScheme(updated.getScheme());
			existing.setProcess(updated.getProcess());
			existing.setDetails(updated.getDetails());
			existing.setPayoutDays(updated.getPayoutDays());
			existing.setGrade(updated.getGrade());
			existing.setTargetCandidates(updated.getTargetCandidates());
			existing.setIsDefault(updated.getIsDefault());
			return schemeRepository.save(existing);
		});
	}

	@Override
	public List<SchemeVendorDTO> getSchemeVendorMapping() {
		List<Object[]> rawResults = schemeRepository.findSchemeVendorRaw();

		List<SchemeVendorDTO> dtoList = new ArrayList<>();
		for (Object[] row : rawResults) {
			Long schemeId = ((Number) row[0]).longValue(); // cast to Number first, then to Long
			String schemeName = (String) row[1];
			String vendorNames = (String) row[2];
			dtoList.add(new SchemeVendorDTO(schemeId, schemeName, vendorNames));
		}

		return dtoList;
	}

	public void addVendorsByName(String schemeName, List<String> vendorNames) {
		Long schemeId = schemeRepository.findIdBySchemeName(schemeName)
				.orElseThrow(() -> new RuntimeException("Scheme not found"));

		List<Long> vendorIds = vendorNames.stream().map(name -> vendorInfoRepository.findIdByCompanyName(name)
				.orElseThrow(() -> new RuntimeException("Vendor not found: " + name))).toList();

		updateVendorsForScheme(schemeId, vendorIds);
	}

	public void removeVendorByName(String schemeName, String vendorName) {
		Long schemeId = schemeRepository.findIdBySchemeName(schemeName)
				.orElseThrow(() -> new RuntimeException("Scheme not found"));

		Long vendorId = vendorInfoRepository.findIdByCompanyName(vendorName)
				.orElseThrow(() -> new RuntimeException("Vendor not found"));

		removeVendorFromScheme(schemeId, vendorId);
	}

	@Transactional
	public void updateVendorsForScheme(Long schemeId, List<Long> vendorIds) {
		Schemes scheme = schemeRepository.findById(schemeId)
				.orElseThrow(() -> new RuntimeException("Scheme not found"));

		List<VendorInfo> vendors = vendorInfoRepository.findAllById(vendorIds);
		scheme.setVendorInfoList(vendors);
		schemeRepository.save(scheme);
	}

	@Transactional
	public void removeVendorFromScheme(Long schemeId, Long vendorId) {
		Schemes scheme = schemeRepository.findById(schemeId)
				.orElseThrow(() -> new RuntimeException("Scheme not found"));

		scheme.getVendorInfoList().removeIf(v -> v.getId().equals(vendorId));
		schemeRepository.save(scheme);
	}

	@Override
	public void addVendorsToScheme(Long schemeId, List<String> vendorNames) {
		Schemes scheme = schemeRepository.findById(schemeId)
				.orElseThrow(() -> new RuntimeException("Scheme not found"));

		for (String vendorName : vendorNames) {
			VendorInfo vendor = vendorInfoRepository.findByCompanyName(vendorName).orElseGet(() -> {
				VendorInfo newVendor = new VendorInfo();
				newVendor.setCompanyName(vendorName);
				return vendorInfoRepository.save(newVendor);
			});

			// Add scheme to vendor's list if not already present
			if (!vendor.getSchemes().contains(scheme)) {
				vendor.getSchemes().add(scheme);
			}

			// Save vendor to update relationship in join table
			vendorInfoRepository.save(vendor);
		}
	}

	@Override
	public void removeVendorsFromScheme(Long schemeId, List<String> vendorNames) {
		// TODO Auto-generated method stub
		Schemes scheme = schemeRepository.findById(schemeId)
				.orElseThrow(() -> new RuntimeException("Scheme not found"));

		for (String vendorName : vendorNames) {
			VendorInfo vendor = vendorInfoRepository.findByCompanyName(vendorName)
					.orElseThrow(() -> new RuntimeException("Vendor not found: " + vendorName));

			// Remove scheme from vendor's list if present
			if (vendor.getSchemes().contains(scheme)) {
				vendor.getSchemes().remove(scheme);
				vendorInfoRepository.save(vendor);
			}
		}
	}

}
