package com.eos.admin.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "our_employee")
public class OurEmployees {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long OurEmployeeId;

	@OneToOne
	@JoinColumn(name = "employee_id", nullable = false)
	@JsonBackReference
	private Employee employee;

	@Column(name = "official_email")
	private String officialEmail;

	@Column(name = "employee_code", unique = true)
	private String employeeCode;

	@Column(name = "joining_date")
	private Date joiningDate;

	@Column(name = "department")
	private String department;

	@Column(name = "designation")
	private String designation;

	@Column(name = "process")
	private String process;

	@Column(name = "Company_type")
	private String CompanyType;

	@Column(name = "grid")
	private String grid;

	@Column(name = "grade")
	private String grade;

	@Column(name = "ctc")
	private Double ctc;

	@Column(name = "take_home")
	private Double takeHome;

	@Column(name = "training_applicable")
	private String trainingApplicable;

	@Column(name = "training_days")
	private Long trainingDays;

	@Column(name = "loyaltyBonus")
	private Double loyaltyBonus;

	@Column(name = "pli")
	private Double pli;

	@Version
	@Column(name = "version")
	private Long version;

}
