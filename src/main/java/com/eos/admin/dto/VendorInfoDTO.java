package com.eos.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

import com.eos.admin.enums.VendorDetailsVerification;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorInfoDTO {

    private Long id;
    private String companyName;
    private String address;
    private String city;
    private String pinCode;
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
    private String verificationRemark;
    private VendorDetailsVerification vendorDetailsVerification;
    private List<DirectorDTO> directors;
    private BankDetailsDTO bankDetails;
   
}