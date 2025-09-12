package com.eos.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.eos.admin.enums.AttendanceType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCandidateListDTO {
	private Long employeeId;
    private String process;
    private String fullName;
    private LocalDate inductionCompleteDate;
    
    private AttendanceType type;
}
