package com.eos.admin.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.eos.admin.dto.DetailedFormDTO;
import com.eos.admin.dto.SchemeDropDownDTO;
import com.eos.admin.dto.VendorCompanyNameDTO;
import com.eos.admin.dto.VendorDashboardStatusDTO;
import com.eos.admin.dto.VendorInfoDTO;
import com.eos.admin.dto.VendorUploadStatusDTO;
import com.eos.admin.dto.VendorVerificationDTO;
import com.eos.admin.entity.Schemes;
import com.eos.admin.enums.FileType;
import com.eos.admin.enums.VendorDetailsVerification;
import com.eos.admin.exception.EmptyFileUploadException;
import com.eos.admin.exception.EntityNotFoundException;
import com.eos.admin.service.SchemesService;
import com.eos.admin.service.VendorInfoService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/vendorInfo")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class VendorInfoController {

	@Value("${project.image.check-image}")
	private String checkImagePath;

	private final VendorInfoService vendorInfoService;
	private final SchemesService schemesService;

	public VendorInfoController(VendorInfoService vendorInfoService, SchemesService schemesService) {
		this.vendorInfoService = vendorInfoService;
		this.schemesService = schemesService;

	}

	// Updated submit method to handle multipart/form-data
	@PutMapping(value = "/createVendorInfo2", consumes = { "multipart/form-data" })
	public ResponseEntity<?> submitDetailedForm2(@RequestPart("form") DetailedFormDTO detailedFormDTO,
			@RequestPart(value = "images") List<MultipartFile> images) {

		if (images == null || images.isEmpty()) {
			log.warn("No images received");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		} else {
			// Filter out empty files (zero size or no filename)
			List<MultipartFile> nonEmptyImages = images.stream().filter(file -> file.getSize() > 0
					&& file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty())
					.collect(Collectors.toList());

			if (nonEmptyImages.isEmpty()) {
				log.warn("All images are empty files");
//		        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
				throw new EmptyFileUploadException(
						"All uploaded images are empty files. Please upload valid image files.");
			}

			log.info("Number of non-empty images received: {}", nonEmptyImages.size());
			for (MultipartFile file : nonEmptyImages) {
				log.info("Image file name: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
			}

			// Use nonEmptyImages further instead of images
			VendorInfoDTO request = vendorInfoService.createVendorInfo(detailedFormDTO, nonEmptyImages);

			log.info("Successfully created vendor with ID: {}", request.getId());
			return new ResponseEntity<>(request, HttpStatus.CREATED);
		}

	}

	@PutMapping(value = "/createVendorInfo", consumes = { "multipart/form-data" })
	public ResponseEntity<?> submitDetailedForm(@RequestPart("form") DetailedFormDTO detailedFormDTO,
			@RequestPart(value = "images", required = false) List<MultipartFile> images) {
		boolean isUpdate = detailedFormDTO.getId() != null;

		if (!isUpdate) { // Creation case
			if (images == null || images.isEmpty()) {
				log.warn("No images received for new vendor creation.");
				return new ResponseEntity<>("Images are required for new vendor creation", HttpStatus.BAD_REQUEST);
			}

			// Filter out empty files
			List<MultipartFile> nonEmptyImages = images.stream().filter(file -> file.getSize() > 0
					&& file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty())
					.collect(Collectors.toList());

			if (nonEmptyImages.isEmpty()) {
				log.warn("All images are empty files");
				throw new EmptyFileUploadException(
						"All uploaded images are empty files. Please upload valid image files.");
			}

			log.info("Number of non-empty images received: {}", nonEmptyImages.size());
			nonEmptyImages.forEach(file -> log.info("Image file name: {}, size: {} bytes", file.getOriginalFilename(),
					file.getSize()));

			VendorInfoDTO created = vendorInfoService.createVendorInfo(detailedFormDTO, nonEmptyImages);
			log.info("Successfully created vendor with ID: {}", created.getId());
			return new ResponseEntity<>(created, HttpStatus.CREATED);
		}

		// Update case — skip image null check
		log.info("Update request received. Skipping image null/empty check.");
		VendorInfoDTO updated = vendorInfoService.createVendorInfo(detailedFormDTO, images); // images may be null or
																								// empty
		log.info("Successfully updated vendor with ID: {}", updated.getId());
		return new ResponseEntity<>(updated, HttpStatus.OK);
	}

	// Get single vendor by ID
	@GetMapping("/{id}")
	public ResponseEntity<VendorInfoDTO> getVendorInfoById(@PathVariable("id") Long id) {
		try {
			VendorInfoDTO vendorInfo = vendorInfoService.getVendorInfoById(id);
			return ResponseEntity.ok(vendorInfo);
		} catch (Exception e) {
			log.error("Error fetching vendor by ID {}: {}", id, e.getMessage());
			return ResponseEntity.internalServerError().body(null);
		}
	}

	// Get all vendor entries
	@GetMapping("/list")
	public ResponseEntity<List<VendorInfoDTO>> getAllVendorInfos() {
		try {
			List<VendorInfoDTO> vendors = vendorInfoService.getAllVendorInfos();
			return ResponseEntity.ok(vendors);
		} catch (Exception e) {
			log.error("Error fetching vendors: {}", e.getMessage());
			return ResponseEntity.internalServerError().body(null);
		}
	}

	// Delete vendor by ID
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteVendor(@PathVariable("id") Long id) {
		try {
			vendorInfoService.deleteVendorInfo(id);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			log.error("Error deleting vendor with ID {}: {}", id, e.getMessage());
			return ResponseEntity.internalServerError().body("Delete failed: " + e.getMessage());
		}
	}

	// Helper to save file on local disk

	@GetMapping("/vendor-detalils-status")
	public VendorDetailsVerification[] getAllStatuses() {
		return VendorDetailsVerification.values();
	}

	@PutMapping("/{id}/verification-vendor-details")
	public ResponseEntity<?> vendorDetailsVerification(@PathVariable("id") Long id,
			@RequestBody VendorVerificationDTO vendorVerificationDTO) {
		try {
			String result = vendorInfoService.verifyVendorDetails(id, vendorVerificationDTO);
			return ResponseEntity.ok(result);
		} catch (EntityNotFoundException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while verifying vendor: " + ex.getMessage());
		}
	}

	@GetMapping("/verification-status")
	public ResponseEntity<?> getVendorVerificationStatusByEmail(@RequestParam("email") String email) {
		try {
			VendorDetailsVerification status = vendorInfoService.getVendorVerificationStatusByEmail(email);
			return ResponseEntity.ok(status);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vendor with email " + email + " not found.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
		}
	}

	@GetMapping("/dashboard/status")
	public VendorDashboardStatusDTO getDashboardStatus(Principal principal) {
		String email = principal.getName();
		try {
			return vendorInfoService.getVendorDashboardStatus(email);
		} catch (RuntimeException ex) {

			return new VendorDashboardStatusDTO(false, null, null, null, null, null, null, null);
		}
	}

	@PostMapping("/upload/{fileNo}")
	public ResponseEntity<String> uploadVendorDocument(@RequestParam("file") MultipartFile file,
			@RequestParam("fileType") FileType fileType, @PathVariable("fileNo") String fileNo) {
		try {
//			String savedFileName = fileService.uploadSingleImages(checkImagePath, file, fileNo, fileType);
			String result = vendorInfoService.handleFileUpload(file, fileType, fileNo);
			return ResponseEntity.ok("File uploaded successfully: " + result);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
		}
	}

	@GetMapping("/available-schemes")
	public ResponseEntity<List<SchemeDropDownDTO>> getAvailableSchemes(
			@RequestParam(value = "vendorId", required = false) Long vendorId) {

		List<Schemes> schemes = schemesService.getAvailableSchemesForVendor(vendorId);
		List<SchemeDropDownDTO> dtos = schemes.stream().map(s -> new SchemeDropDownDTO(s.getId(), s.getScheme()))
				.collect(Collectors.toList());

		return ResponseEntity.ok(dtos);
	}

	@GetMapping("/company-names")
	public List<VendorCompanyNameDTO> getAllVendorCompanyNames() {
		return vendorInfoService.getAllVendorCompanyNames();
	}

	@GetMapping("/company-names-for-users")
	public List<VendorCompanyNameDTO> getAllVendorCompanyNamesForUsers() {
		return vendorInfoService.getAllVendorCompanyNames();
	}
	
	
	 @GetMapping("/upload-status/{vendorId}")
	    public ResponseEntity<VendorUploadStatusDTO> getUploadStatus(@PathVariable("vendorId") Long vendorId) {
	        VendorUploadStatusDTO status = vendorInfoService.getUploadStatus(vendorId);
	        return ResponseEntity.ok(status);
	    }
}