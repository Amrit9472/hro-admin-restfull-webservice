package com.eos.admin.serviceImpl;

import com.eos.admin.dto.AddressDTO;
import com.eos.admin.dto.BankDetailsDTO;
import com.eos.admin.dto.DetailedFormDTO;
import com.eos.admin.dto.DirectorDTO;
import com.eos.admin.dto.VendorCompanyNameDTO;
import com.eos.admin.dto.VendorDashboardStatusDTO;
import com.eos.admin.dto.VendorInfoDTO;
import com.eos.admin.dto.VendorUploadStatusDTO;
import com.eos.admin.dto.VendorVerificationDTO;
import com.eos.admin.entity.AddressEntity;
import com.eos.admin.entity.BankDetails;
import com.eos.admin.entity.Director;
import com.eos.admin.entity.VendorInfo;
import com.eos.admin.enums.FileType;
import com.eos.admin.enums.VendorDetailsVerification;
import com.eos.admin.exception.EntityNotFoundException;
import com.eos.admin.repository.BankDetailsRepository;
import com.eos.admin.repository.VendorInfoRepository;
import com.eos.admin.service.FileService;
import com.eos.admin.service.VendorInfoService;

import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VendorInfoServiceImpl implements VendorInfoService {

	@Value("${project.image.check-image}")
	private String checkImagePath;

	private final VendorInfoRepository vendorInfoRepository;
	private final ModelMapper modelMapper;
	private final FileServiceImp fileServiceImp;
	private final BankDetailsRepository bankDetailsRepository;
	private final FileService fileService;
	@Autowired
	public VendorInfoServiceImpl(VendorInfoRepository vendorInfoRepository, ModelMapper modelMapper,
			FileServiceImp fileServiceImp, BankDetailsRepository bankDetailsRepository,FileService fileService) {
		this.vendorInfoRepository = vendorInfoRepository;
		this.modelMapper = modelMapper;
		this.bankDetailsRepository = bankDetailsRepository;
		this.fileServiceImp = fileServiceImp;
		this.fileService = fileService;
	}

	@Override
	public VendorInfoDTO saveVendorInfo(VendorInfoDTO vendorInfoDto) {
		try {
			// Mapping DTO to entity using ModelMapper
			VendorInfo vendorInfo = modelMapper.map(vendorInfoDto, VendorInfo.class);

			if (vendorInfo.getDirectors() != null) {
				vendorInfo.getDirectors().forEach(director -> director.setVendorInfo(vendorInfo));
			}

			if (vendorInfo.getBankDetails() != null) {
				vendorInfo.getBankDetails().setVendorInfo(vendorInfo);
			}

			// Saving the vendor info entity to the database
			VendorInfo savedVendorInfo = vendorInfoRepository.save(vendorInfo);

			// Mapping saved entity back to DTO and returning
			return modelMapper.map(savedVendorInfo, VendorInfoDTO.class);
		} catch (Exception e) {
			// Handle exceptions and log appropriately
			throw new RuntimeException("Error occurred while saving VendorInfo: " + e.getMessage(), e);
		}
	}

	@Override
	public VendorInfoDTO getVendorInfoById(Long id) {
		try {
			// Fetching VendorInfo by ID from the repository
			VendorInfo vendorInfo = vendorInfoRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("VendorInfo not found with id: " + id));

			// Mapping entity to DTO and returning
			return modelMapper.map(vendorInfo, VendorInfoDTO.class);
		} catch (Exception e) {
			// Handle exceptions and log appropriately
			throw new RuntimeException("Error occurred while fetching VendorInfo by id: " + e.getMessage(), e);
		}
	}

	@Override
	public List<VendorInfoDTO> getAllVendorInfos() {
		List<VendorInfo> vendorInfos = vendorInfoRepository.findAll();

		
		Collections.reverse(vendorInfos);
		return vendorInfos.stream().map(vendorInfo -> {
			VendorInfoDTO dto = modelMapper.map(vendorInfo, VendorInfoDTO.class);
			BankDetailsDTO bankDetails = dto.getBankDetails();

			if (bankDetails != null && bankDetails.getAccountNumber() != null) {
				String imageUrl = "/images/" + bankDetails.getAccountNumber() + "_img.jpg";
				bankDetails.setChequeImagePath(imageUrl);
			}

			return dto;
		}).collect(Collectors.toList());
	}
//	public List<VendorInfoDTO> getAllVendorInfos() {
//		try {
//			// Fetching all VendorInfo records from the repository
//			List<VendorInfo> vendorInfos = vendorInfoRepository.findAll();
//
//			// Mapping each entity to DTO
//			 List<VendorInfoDTO> vendorInfoDTOList = vendorInfos.stream().map(vendorInfo -> {
//		            VendorInfoDTO dto = modelMapper.map(vendorInfo, VendorInfoDTO.class);
//		        
//		            BankDetailsDTO bankDetails = dto.getBankDetails();  
//		            
//		            if (bankDetails != null && bankDetails.getAccountNumber() != null) {
//		                // Construct the image URL using the account number from BankDetailsDTO
//		                String imageUrl = checkImagePath +"/"+ bankDetails.getAccountNumber() + "_img.jpg";
//		                
//		                // Set the cheque image path in the BankDetailsDTO
//		                bankDetails.setChequeImagePath(imageUrl);
//		            }
//
//		            return dto;
//		        }).collect(Collectors.toList());
//			 return vendorInfoDTOList;
//			 
////			return vendorInfos.stream().map(vendorInfo -> modelMapper.map(vendorInfo, VendorInfoDTO.class))
////					.collect(Collectors.toList( 	));
//		} catch (Exception e) {
//			// Handle exceptions and log appropriately
//			throw new RuntimeException("Error occurred while fetching all VendorInfos: " + e.getMessage(), e);
//		}
//	}

	@Override
	public void deleteVendorInfo(Long id) {
		try {
			// Deleting VendorInfo by ID from the repository
			vendorInfoRepository.deleteById(id);
		} catch (Exception e) {
			// Handle exceptions and log appropriately
			throw new RuntimeException(
					"Error occurred while deleting VendorInfo with id: " + id + " - " + e.getMessage(), e);
		}
	}

	@Override
	public VendorInfoDTO createVendorInfo(DetailedFormDTO detailedFormDTO, MultipartFile chequeImage)
			throws IOException {
		log.info("Starting to save vendor details for ID: {}", detailedFormDTO.getId());

		try {
			// Save cheque image and update path
			String savedPath = saveChequeImageAndSetPath(detailedFormDTO, chequeImage);

			// Map form data to DTO
			VendorInfoDTO vendorInfoDto = mapFormToVendorDTO(detailedFormDTO);

			// Convert DTO to Entity
			VendorInfo vendorInfo = modelMapper.map(vendorInfoDto, VendorInfo.class);

			// Set relationships
			associateRelatedEntities(vendorInfo);

			// Persist entity
			VendorInfo savedVendorInfo = vendorInfoRepository.save(vendorInfo);

			log.info("Successfully saved vendor info for ID: {}", savedVendorInfo.getId());

			// Return DTO
			return modelMapper.map(savedVendorInfo, VendorInfoDTO.class);

		} catch (Exception ex) {
			log.error("Error while saving vendor details: {}", ex.getMessage(), ex);
			throw new RuntimeException("Failed to save vendor information", ex);
		}
	}

	@Override
	public String verifyVendorDetails(Long id, VendorVerificationDTO vendorVerificationDTO) {
		log.info("Verifying vendor details for ID: {}", id);

		Optional<VendorInfo> optionalVendor = vendorInfoRepository.findById(id);

		if (!optionalVendor.isPresent()) {
			log.error("Vendor with ID {} not found.", id);
			throw new EntityNotFoundException("Vendor with ID " + id + " not found.");
		}

		VendorInfo vendor = optionalVendor.get();

		vendor.setVendorDetailsVerification(vendorVerificationDTO.getVendorDetailsVerification());
		vendor.setVerificationRemark(vendorVerificationDTO.getRemark());

		boolean isApproved = vendorVerificationDTO.getVendorDetailsVerification() != null
				&& "APPROVED".equalsIgnoreCase(vendorVerificationDTO.getVendorDetailsVerification().name());

		vendor.setVerification(isApproved);

		vendorInfoRepository.save(vendor);

		log.info("Vendor ID {} verification updated. Approved: {}", id, isApproved);

		return "Vendor verification updated successfully.";
	}

	private String saveChequeImageAndSetPath(DetailedFormDTO detailedFormDTO, MultipartFile chequeImage)
			throws IOException {
		if (chequeImage != null && !chequeImage.isEmpty()) {
			String uploadDir = checkImagePath;
			Files.createDirectories(Paths.get(uploadDir));

//			String filename = detailedFormDTO.getBankDetails().getAccountNumber() + "_"
//					+ chequeImage.getOriginalFilename();
//			Path filepath = Paths.get(uploadDir, filename);
//			Files.write(filepath, chequeImage.getBytes());
//
//			String savedPath = filepath.toString();
//			log.info("Saved cheque image to {}", savedPath);
//
//			String relativePath = "/images/" + filename; // URL path
//		    log.info("Saved cheque image accessible at {}", relativePath);
			String accountNumber = detailedFormDTO.getBankDetails().getAccountNumber();
			String filename = accountNumber + "_img.jpg"; // fixed clean format
			Path filepath = Paths.get(uploadDir, filename);
			Files.write(filepath, chequeImage.getBytes());

			String relativePath = "/images/" + filename;
			detailedFormDTO.getBankDetails().setChequeImagePath(relativePath);

			if (detailedFormDTO.getBankDetails() != null) {
				detailedFormDTO.getBankDetails().setChequeImagePath(relativePath);
			}

			return relativePath;
		}

		log.warn("Cheque image is null or empty, skipping file save.");
		return null;
	}

	private VendorInfoDTO mapFormToVendorDTO(DetailedFormDTO form) {
		VendorInfoDTO dto = new VendorInfoDTO();
		dto.setId(form.getId());
		dto.setCompanyName(form.getCompanyName());
//		dto.setAddress(form.getAddress());
//		dto.setCity(form.getCity());
//		dto.setPinCode(form.getPinCode());
		dto.setTelephone(form.getTelephone());
		dto.setMobile(form.getMobile());
		dto.setEmail(form.getEmail());
		dto.setContactPerson(form.getContactPerson());
		dto.setPan(form.getPan());
		dto.setGst(form.getGst());
		dto.setMsme(form.getMsme());
		dto.setServiceType(form.getServiceType());
		dto.setServiceTypeOther(form.getServiceTypeOther());
		dto.setDeclaration(form.isDeclaration());
		dto.setDirectors(form.getDirectors());
		dto.setBankDetails(form.getBankDetails());
		return dto;
	}

	private void associateRelatedEntities(VendorInfo vendorInfo) {
		if (vendorInfo.getDirectors() != null) {
			vendorInfo.getDirectors().forEach(director -> director.setVendorInfo(vendorInfo));
		}
		if (vendorInfo.getBankDetails() != null) {
			vendorInfo.getBankDetails().setVendorInfo(vendorInfo);
		}
	}

	private String saveFile(DetailedFormDTO detailedFormDTO, MultipartFile file) throws IOException {
		String uploadDir = "uploads/cheque_images/";
		Files.createDirectories(Paths.get(uploadDir));

		String filename = detailedFormDTO.getBankDetails().getAccountNumber() + "_" + file.getOriginalFilename();
		Path filepath = Paths.get(uploadDir, filename);
		Files.write(filepath, file.getBytes());

		log.info("Saved cheque image to {}", filepath.toString());

		return filepath.toString();
	}

	@Override
	public VendorDetailsVerification getVendorVerificationStatusByEmail(String email) {
		VendorInfo vendor = vendorInfoRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Vendor not found with email: " + email));
		return vendor.getVendorDetailsVerification();
	}

//	@Override
//	public VendorInfoDTO createVendorInfo(DetailedFormDTO detailedFormDTO, List<MultipartFile> images) {
//		try {
//			// Upload cheque image(s)
////			    if()
//			fileServiceImp.uploadImage(checkImagePath, images, detailedFormDTO.getBankDetails().getAccountNumber());
//
//			// Map form data to DTO
//			VendorInfoDTO vendorInfoDto = mapFormToVendorDTO(detailedFormDTO);
//
//			// Convert to Entity
//			VendorInfo vendorInfo = modelMapper.map(vendorInfoDto, VendorInfo.class);
//			System.out.println("Vendor addresses mapped: " + vendorInfo.getAddress());
//			// --- DETACH bankDetails initially to avoid unsaved transient vendorInfo
//			// reference ---
//
//			if (detailedFormDTO.getAddress() != null) {
//				List<AddressEntity> addressEntities = new ArrayList<>();
//				for (AddressDTO dto : detailedFormDTO.getAddress()) {
//					AddressEntity entity = new AddressEntity();
//					entity.setAddressLine1(dto.getAddressLine1());
//					entity.setAddressLine2(dto.getAddressLine2());
//					entity.setCity(dto.getCity());
//					entity.setPinCode(dto.getPinCode());
//					entity.setState(dto.getState());
//					entity.setVendorInfo(vendorInfo); // Set back-reference
//					addressEntities.add(entity);
//				}
//				vendorInfo.setAddress(addressEntities);
//			}
//
//			BankDetails bankDetails = vendorInfo.getBankDetails();
//			vendorInfo.setBankDetails(null); // Temporarily remove
//
//			// Set VendorInfo reference in Directors and their Addresses
//			if (vendorInfo.getDirectors() != null) {
//				for (Director director : vendorInfo.getDirectors()) {
//					director.setVendorInfo(vendorInfo);
//
//					if (director.getAddress() != null) {
//						for (AddressEntity address : director.getAddress()) {
//							address.setDirector(director);
//						}
//					}
//				}
//			}
//
//			// Set VendorInfo reference in Vendor-level Addresses
//			if (vendorInfo.getAddress() != null) {
//				for (AddressEntity address : vendorInfo.getAddress()) {
//					address.setVendorInfo(vendorInfo);
//				}
//			}
//
//			// --- Step 1: Save VendorInfo first ---
//			VendorInfo savedVendorInfo = vendorInfoRepository.save(vendorInfo);
//
//			// --- Step 2: Set vendorInfo in BankDetails and its Addresses ---
//			if (bankDetails != null) {
//				bankDetails.setVendorInfo(savedVendorInfo);
//
//				if (bankDetails.getAddress() != null) {
//					for (AddressEntity address : bankDetails.getAddress()) {
//						address.setBankDetails(bankDetails);
//					}
//				}
//
//				// Save BankDetails (you need BankDetailsRepository for this)
//				BankDetails savedBankDetails = bankDetailsRepository.save(bankDetails);
//
//				// Attach saved BankDetails to savedVendorInfo and update it
//				savedVendorInfo.setBankDetails(savedBankDetails);
//				vendorInfoRepository.save(savedVendorInfo); // update with bank
//			}
//
//			log.info("Successfully saved vendor info for ID: {}", savedVendorInfo.getId());
//
//			// Return DTO
//			return modelMapper.map(savedVendorInfo, VendorInfoDTO.class);
//
//		} catch (Exception ex) {
//			log.error("Error while saving vendor details: {}", ex.getMessage(), ex);
//			throw new RuntimeException("Failed to save vendor information", ex);
//		}
//	}
  
	@Override
	public VendorInfoDTO createVendorInfo(DetailedFormDTO detailedFormDTO, List<MultipartFile> images) {
	    try {
	        // Upload images (like cancelled cheque)
//	        fileServiceImp.uploadImage2(checkImagePath, images, detailedFormDTO.getBankDetails().getAccountNumber());

	        // Find existing vendor by email
	        Optional<VendorInfo> optionalVendor = vendorInfoRepository.findByEmail(detailedFormDTO.getEmail());

	        VendorInfo vendorInfo;
	        boolean isUpdate = false;

	        if (optionalVendor.isPresent()) {
	            vendorInfo = optionalVendor.get();
	            isUpdate = true;
	        } else {
	            vendorInfo = new VendorInfo();
	        }

	        // Manually map fields from DTO to entity
	        vendorInfo.setCompanyName(detailedFormDTO.getCompanyName());
	        vendorInfo.setContactPerson(detailedFormDTO.getContactPerson());
	        vendorInfo.setEmail(detailedFormDTO.getEmail());
	        vendorInfo.setMobile(detailedFormDTO.getMobile());
	        vendorInfo.setTelephone(detailedFormDTO.getTelephone());
	        vendorInfo.setPan(detailedFormDTO.getPan());
	        vendorInfo.setGst(detailedFormDTO.getGst());
	        vendorInfo.setMsme(detailedFormDTO.getMsme());
	     // Set document upload flags
	        vendorInfo.setPanUpload(detailedFormDTO.getPan() != null && !detailedFormDTO.getPan().trim().isEmpty());
	        vendorInfo.setGstUpload(detailedFormDTO.getGst() != null && !detailedFormDTO.getGst().trim().isEmpty());
	        vendorInfo.setMsmeUpload(detailedFormDTO.getMsme() != null && !detailedFormDTO.getMsme().trim().isEmpty());
	        vendorInfo.setCheckUpload(true); // Always true as per requirement
	        vendorInfo.setServiceType(detailedFormDTO.getServiceType());
	        vendorInfo.setServiceTypeOther(detailedFormDTO.getServiceTypeOther());
	        vendorInfo.setDeclaration(detailedFormDTO.isDeclaration());
	        vendorInfo.setVerification(false); // reset on update/creation
	        vendorInfo.setVendorDetailsVerification(null); // reset enum
	        vendorInfo.setVerificationRemark(null);

	        // --- Addresses ---
	        if (detailedFormDTO.getAddress() != null) {
	            // Ensure collection exists
	            if (vendorInfo.getAddress() == null) {
	                vendorInfo.setAddress(new ArrayList<>());
	            }
	            // Clear existing addresses
	            vendorInfo.getAddress().clear();

	            for (AddressDTO dto : detailedFormDTO.getAddress()) {
	                AddressEntity address = new AddressEntity();
	                address.setAddressLine1(dto.getAddressLine1());
	                address.setAddressLine2(dto.getAddressLine2());
	                address.setCity(dto.getCity());
	                address.setPinCode(dto.getPinCode());
	                address.setState(dto.getState());
	                address.setVendorInfo(vendorInfo); // Set back reference
	                vendorInfo.getAddress().add(address);
	            }
	        }

	        // --- Directors ---
	        if (detailedFormDTO.getDirectors() != null) {
	            if (vendorInfo.getDirectors() == null) {
	                vendorInfo.setDirectors(new ArrayList<>());
	            }
	            vendorInfo.getDirectors().clear();

	            for (DirectorDTO dto : detailedFormDTO.getDirectors()) {
	                Director director = new Director();
	                director.setDirectorName(dto.getDirectorName());
	                director.setDirectorDesignation(dto.getDirectorDesignation());
	                director.setDirectorPhone(dto.getDirectorPhone());
	                director.setVendorInfo(vendorInfo);

	                if (dto.getAddress() != null) {
	                    List<AddressEntity> dirAddresses = new ArrayList<>();
	                    for (AddressDTO addr : dto.getAddress()) {
	                        AddressEntity address = new AddressEntity();
	                        address.setAddressLine1(addr.getAddressLine1());
	                        address.setAddressLine2(addr.getAddressLine2());
	                        address.setCity(addr.getCity());
	                        address.setPinCode(addr.getPinCode());
	                        address.setState(addr.getState());
	                        address.setDirector(director);
	                        dirAddresses.add(address);
	                    }
	                    director.setAddress(dirAddresses);
	                }
	                vendorInfo.getDirectors().add(director);
	            }
	        }

	        // --- Bank Details ---
	        BankDetailsDTO bankDTO = detailedFormDTO.getBankDetails();
	        BankDetails bankDetails;

	        if (isUpdate && vendorInfo.getBankDetails() != null) {
	            bankDetails = vendorInfo.getBankDetails();
	        } else {
	            bankDetails = new BankDetails();
	        }

	        bankDetails.setAccountNumber(bankDTO.getAccountNumber());
	        bankDetails.setBankName(bankDTO.getBankName());
	        bankDetails.setAccountName(bankDTO.getAccountName());
	        bankDetails.setIfscCode(bankDTO.getIfscCode());
	        bankDetails.setBranch(bankDTO.getBranch());
	        bankDetails.setVendorInfo(vendorInfo);

	        // Bank Address
//	        if (bankDTO.getAddress() != null) {
//	            if (bankDetails.getAddress() == null) {
//	                bankDetails.setAddress(new ArrayList<>());
//	            }
//	            bankDetails.getAddress().clear();
//
//	            for (AddressDTO dto : bankDTO.getAddress()) {
//	                AddressEntity address = new AddressEntity();
//	                address.setAddressLine1(dto.getAddressLine1());
//	                address.setAddressLine2(dto.getAddressLine2());
//	                address.setCity(dto.getCity());
//	                address.setPinCode(dto.getPinCode());
//	                address.setState(dto.getState());
//	                address.setBankDetails(bankDetails);
//	                bankDetails.getAddress().add(address);
//	            }
//	        }

	        vendorInfo.setBankDetails(bankDetails);

	        // Save VendorInfo and cascade all changes (addresses, directors, bankDetails)
	        VendorInfo savedVendorInfo = vendorInfoRepository.save(vendorInfo);
	        
	        fileServiceImp.uploadImage2(checkImagePath, images, String.valueOf(savedVendorInfo.getId()));
	        
	        log.info("{} vendor info for email: {}", isUpdate ? "Updated" : "Created", savedVendorInfo.getEmail());

	        return modelMapper.map(savedVendorInfo, VendorInfoDTO.class);

	    } catch (Exception ex) {
	        log.error("Error saving vendor info: {}", ex.getMessage(), ex);
	        throw new RuntimeException("Failed to save vendor information", ex);
	    }
	}


	@Override
	public VendorDashboardStatusDTO getVendorDashboardStatus(String email) {
		VendorInfo vendorInfo = vendorInfoRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Vendor Info not found for email: " + email));

		if (vendorInfo == null) {
			return new VendorDashboardStatusDTO(false, null, null, null, null, null, null, null);
		}
		boolean showAllTabs = false;

		if (VendorDetailsVerification.APPROVED.equals(vendorInfo.getVendorDetailsVerification())) {
			showAllTabs = true;
		} else if (VendorDetailsVerification.REJECT.equals(vendorInfo.getVendorDetailsVerification())
				&& !Boolean.TRUE.equals(vendorInfo.isVerification())) {
			showAllTabs = false;
		}

		VendorDashboardStatusDTO dto = new VendorDashboardStatusDTO();
		dto.setShowAllTabs(showAllTabs);
		dto.setId(vendorInfo.getId());
		dto.setCompanyName(vendorInfo.getCompanyName());
		dto.setEmail(vendorInfo.getEmail());
		dto.setContactPerson(vendorInfo.getContactPerson());
		dto.setVendorDetailsVerification(vendorInfo.getVendorDetailsVerification());
		dto.setVerification(vendorInfo.isVerification());
		dto.setVerificationRemark(vendorInfo.getVerificationRemark());

		return dto;
	}

	@Override
	public String handleFileUpload(MultipartFile file, FileType fileType, String fileNo) {
	    String savedFileName;
	    try {
	        savedFileName = fileService.uploadSingleImages(checkImagePath, file, fileNo, fileType);
	    } catch (IOException e) {
	        throw new RuntimeException("File upload failed: " + e.getMessage(), e);
	    }

	    VendorInfo vendor = vendorInfoRepository.findById(Long.parseLong(fileNo))
	            .orElseThrow(() -> new IllegalArgumentException("Vendor not found with ID: " + fileNo));

	    switch (fileType) {
	        case PAN:
	            vendor.setPanUpload(true);
	            break;
	        case GST:
	            vendor.setGstUpload(true);
	            break;
	        case MSME:
	            vendor.setMsmeUpload(true);
	            break;
	        case CHEQUE:
	            vendor.setCheckUpload(true);
	            break;
	        default:
	            throw new IllegalArgumentException("Unsupported file type: " + fileType);
	    }

	    vendorInfoRepository.save(vendor);

	    return "File uploaded successfully: " + savedFileName;
	}

	@Override
	public List<VendorCompanyNameDTO> getAllVendorCompanyNames() {
	    List<VendorInfo> vendorInfos = vendorInfoRepository.findAll();

	    Collections.reverse(vendorInfos);

	    return vendorInfos.stream()
	                      .map(v -> new VendorCompanyNameDTO(v.getId(),v.getCompanyName() ))
	                      .collect(Collectors.toList());
	}

	@Override
	public VendorUploadStatusDTO getUploadStatus(Long vendorId) {
		 VendorInfo vendor = vendorInfoRepository.findById(vendorId)
	                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

	        return new VendorUploadStatusDTO(
	        		    vendor.getPanUpload(),
	        	        vendor.getGstUpload(),
	        	        vendor.getMsmeUpload(),
	        	        vendor.getCheckUpload()
	        );
	}

}