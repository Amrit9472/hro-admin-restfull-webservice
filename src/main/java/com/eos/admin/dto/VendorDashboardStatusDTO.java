package com.eos.admin.dto;

import com.eos.admin.enums.VendorDetailsVerification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VendorDashboardStatusDTO {

	private boolean showAllTabs;
//	private VendorInfo vendorInfo;
	private Long id;
    private String companyName;
    private String email;
    private String contactPerson;
    private VendorDetailsVerification  vendorDetailsVerification;
    private Boolean verification;
    private String verificationRemark;
}
