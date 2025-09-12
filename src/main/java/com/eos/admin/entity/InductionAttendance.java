package com.eos.admin.entity;

import java.time.LocalDateTime;
import java.util.Date;

import com.eos.admin.enums.AttendanceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "induction_attendance")
@Data
public class InductionAttendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date date;
    

    private String process;

    private Long employeeId;

    private String status; // Present / Absent

    private String marker; // who marked

    @Enumerated(EnumType.STRING)
    private AttendanceType type;  // training / induction

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "our_employee_id", nullable = false)
    private OurEmployees ourEmployee;
}