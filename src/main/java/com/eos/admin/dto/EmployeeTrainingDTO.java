package com.eos.admin.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeTrainingDTO {
    private Long batchId;
    private String batchName;
    private Date trainingDate;
    private String batchCode;
    private String process;
    private String faculty;
    private int maxAttempts;
    private int totalMarks;
    private int passingMarks;
    private boolean certificateRequired;
    private List<EmployeeDTO2> employees;
}
