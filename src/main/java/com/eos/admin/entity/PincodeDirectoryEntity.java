package com.eos.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pincode_directory")
public class PincodeDirectoryEntity {
	
	 	@Id
	 	@Column(name = "pincode")
	    private String pincode;
	 	
	 	@Column(name = "officeName")
	    private String officeName;
	 	
	 	@Column(name = "division")
	    private String division;
	 	
	 	@Column(name = "district")
	    private String district;
	 	
	 	@Column(name = "stateName")
	    private String stateName;
	 	
	 	@Column(name = "region")
	    private String region;
	 	
	 	@Column(name = "circle")
	    private String circle;
}
