package com.eos.admin.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "loiInfo")
public class LoiInformationTableEntity {
	  
	  @Id
	  @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
	  @Column(name = "grid")
      private String grid;
	  
	  @Column(name = "process")
      private String process;
	  
	  @Column(name = "grade")
      private String grade;
	  
	  @Column(name = "companyType")
      private String companyType;
	  
	  @Column(name = "Designation")
      private String designation;
	  
	  @Column(name = "appenticeEnrollment")
      private String appenticeEnrollment;
	  
	  @Column(name = "aitmEnrollment")
      private String aitmEnrollment;
	  
	  @Column(name = "trainingApplicable")
      private String trainingApplicable;
	  
	  @Column(name = "trainingDays")
      private int trainingDays;
	  
	  @Column(name = "gridGeneration")
      private Date gridGeneration;
	  
	  @Column(name = "ctc")
      private Double ctc;
	  
	  @Column(name = "takeHome")
      private Double takeHome;
	  
	  @Column(name = "loyaltyBonus")
      private Double loyaltyBonus;
	  
	  @Column(name = "pli")
      private Double pli;
      
      

      
}
