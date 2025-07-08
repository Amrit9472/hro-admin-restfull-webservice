package com.eos.admin.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "directors")
public class Director {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "directorName")
	private String directorName;

	@Column(name = "directorDesignation")
	private String directorDesignation;

//    @Column(name = "directorName")
//    private String directorAddress;
	@OneToMany(mappedBy = "director", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AddressEntity> address;

	@Column(name = "directorPhone")
	private String directorPhone;

	@ManyToOne
	@JoinColumn(name = "vendor_info_id")
	private VendorInfo vendorInfo;
}