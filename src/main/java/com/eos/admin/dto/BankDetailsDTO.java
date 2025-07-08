package com.eos.admin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailsDTO {
	private Long id;
    private String bankName;
    private String accountName;
    private String accountNumber;
    private String ifscCode;
    private String branch;
    private String branchAddress;
    private String chequeImagePath;
//    private List<AddressDTO> address;
}