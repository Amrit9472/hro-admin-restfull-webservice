package com.eos.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailedFormDTO {

	// VENDOR DETAILS
	private Long id;
	private String companyName;
	private String telephone;
	private String mobile;
	private String email;
	private String contactPerson;
	private String pan;
	private String gst;
	private String msme;
	private String serviceType;
	private String serviceTypeOther;
	private boolean declaration;
	private String vendorDetailsVerification;
	private String verificationRemark;
	private boolean verification;
    
	// RELATED CHILD TABLES
	private List<DirectorDTO> directors;
	private BankDetailsDTO bankDetails;
	private List<AddressDTO> address;

}