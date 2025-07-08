package com.eos.admin.entity;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@Setter
@Getter
@Table(name = "schemes")
@NoArgsConstructor
public class Schemes {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "type")
	private String type;

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "amount")
	private Double amount;

//	@Column(name = "vendor")
//	private String vendor;

	@Column(name = "scheme")
	private String scheme;

	@Column(name = "process")
	private String process;

	@Column(name = "details", length = 1000)
	private String details;

	@Column(name = "payout_days")
	private Integer payoutDays;

	@Column(name = "grade")
	private String grade;

	@Column(name = "target_candidates")
	private Long targetCandidates;

	@Column(name = "is_default")
	private Boolean isDefault;

	@JsonIgnore
	@ManyToMany(mappedBy = "schemes")
	private List<VendorInfo> vendorInfoList;

}
