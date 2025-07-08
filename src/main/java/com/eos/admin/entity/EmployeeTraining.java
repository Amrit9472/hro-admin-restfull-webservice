package com.eos.admin.entity;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employeeTraining")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTraining {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "batch_name", nullable = false)
    private String batchName;

    @Column(name = "training_date")
    @Temporal(TemporalType.DATE)
    private Date trainingDate;

    @Column(name = "batch_code", nullable = false, unique = true)
    private String batchCode;

    @Column(name = "process")
    private String process;

    @Column(name = "faculty")
    private String faculty;

    @Column(name = "max_attempts")
    private int maxAttempts;

    @Column(name = "total_marks")
    private int totalMarks;

    @Column(name = "passing_marks")
    private int passingMarks;

    @Column(name = "certificate_required")
    private boolean certificateRequired;

//    @OneToMany(mappedBy = "employeeTraining", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<OurEmployees> employees;
    
    @OneToMany(mappedBy = "employeeTraining", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<OurEmployees> employees;

}
