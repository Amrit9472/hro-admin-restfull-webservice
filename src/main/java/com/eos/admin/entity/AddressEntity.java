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
@Table(name = "addressEntity")
public class AddressEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "addressLine1")
	private String addressLine1;

	@Column(name = "addressLine2")
	private String addressLine2;

	@Column(name = "city")
	private String city;

	@Column(name = "pinCode")
	private String pinCode;

	@Column(name = "state")
	private String state;

	@ManyToOne
	@JoinColumn(name = "vendor_info_id")
	private VendorInfo vendorInfo;

	@ManyToOne
	@JoinColumn(name = "director_id")
	private Director director;
	@ManyToOne
	@JoinColumn(name = "bank_details_id")
	private BankDetails bankDetails;

}
