package com.eos.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bank_details")
public class BankDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "bankName")
    private String bankName;
	
	@Column(name = "accountName")
    private String accountName;
	
	@Column(name = "accountNumber" )
    private String accountNumber;
	
	@Column(name = "ifscCode")
    private String ifscCode;
	
	@Column(name = "branch")
    private String branch;
    
//    @OneToMany(mappedBy = "bankDetails", cascade = CascadeType.ALL, orphanRemoval = true)
//	private List<AddressEntity> address;

    private String chequeImagePath;

    @ManyToOne
    @JoinColumn(name = "vendorid", referencedColumnName = "id", nullable = false)  // Foreign key
    private VendorInfo vendorInfo;
    
}