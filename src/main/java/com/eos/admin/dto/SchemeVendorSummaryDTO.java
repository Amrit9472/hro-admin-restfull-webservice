package com.eos.admin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SchemeVendorSummaryDTO {
    private Long schemeId;
    private String schemeName;
    private List<VendorCompanyNameDTO> vendorNames;
    
//    public SchemeVendorSummaryDTO(Long schemeId, String schemeName, List<VendorCompanyNameDTO> vendorNames) {
//        this.schemeId = schemeId;
//        this.schemeName = schemeName;
//        this.vendorNames = vendorNames;
//    }
  
}
