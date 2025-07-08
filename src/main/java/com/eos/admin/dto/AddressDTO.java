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
public class AddressDTO {

	 private Long id;
     private String addressLine1;
     private String addressLine2;
     private String city;
     private String pinCode;
     private String state;
}
