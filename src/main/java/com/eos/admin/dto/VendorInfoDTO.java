package com.eos.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

import com.eos.admin.enums.VendorDetailsVerification;

import jakarta.persistence.Column;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorInfoDTO {

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
	private Boolean panUpload;
	private Boolean gstUpload;
	private Boolean msmeUpload;
	private Boolean checkUpload;
    private String verificationRemark;
    private VendorDetailsVerification vendorDetailsVerification;
    private List<DirectorDTO> directors;
    private BankDetailsDTO bankDetails;
    private List<AddressDTO> address;
   
}