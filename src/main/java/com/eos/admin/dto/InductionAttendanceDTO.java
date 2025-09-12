package com.eos.admin.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

import com.eos.admin.enums.AttendanceType;

@Data
public class InductionAttendanceDTO {
    private Long ourEmployeeId;
    private Long employeeId;
    private String name;
    private Date date;
    private String process;
    private String status; 
    private String marker; 
    private AttendanceType type;  
   
}
