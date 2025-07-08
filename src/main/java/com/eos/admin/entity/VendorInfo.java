package com.eos.admin.entity;

import java.util.List;

import com.eos.admin.enums.VendorDetailsVerification;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "vendor_info")
public class VendorInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "companyName")
	private String companyName;

	@Column(name = "telephone")
	private String telephone;

	@Column(name = "mobile")
	private String mobile;

	@Column(name = "email")
	private String email;

	@Column(name = "contactPerson")
	private String contactPerson;

	@Column(name = "pan")
	private String pan;

	@Column(name = "gst")
	private String gst;

	@Column(name = "msme")
	private String msme;

	@Column(name = "panUpload")
	private Boolean panUpload;

	@Column(name = "gstUpload")
	private Boolean gstUpload;

	@Column(name = "msmeUpload")
	private Boolean msmeUpload;

	@Column(name = "checkUpload")
	private Boolean checkUpload;

	@Column(name = "serviceType")
	private String serviceType;

	@Column(name = "serviceTypeOther")
	private String serviceTypeOther;

	@Column(name = "declaration")
	private boolean declaration;

	@Enumerated(EnumType.STRING)
	private VendorDetailsVerification vendorDetailsVerification;

	@Column(name = "verificationRemark")
	private String verificationRemark;

	@Column(name = "verification")
	private boolean verification;

	@OneToMany(mappedBy = "vendorInfo", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Director> directors;

	@OneToOne(mappedBy = "vendorInfo", cascade = CascadeType.ALL, orphanRemoval = true)
	private BankDetails bankDetails;

	@OneToMany(mappedBy = "vendorInfo", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AddressEntity> address;

	@ManyToMany
	@JoinTable(name = "vendor_schemes", joinColumns = @JoinColumn(name = "vendor_id"), inverseJoinColumns = @JoinColumn(name = "scheme_id"))
	private List<Schemes> schemes;

}