package com.eos.admin.dto;

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
public class VendorUploadStatusDTO {

	    private Boolean PAN;
	    private Boolean GST;
	    private Boolean MSME;
	    private Boolean CHEQUE;
}
