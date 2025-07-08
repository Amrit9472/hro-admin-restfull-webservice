package com.eos.admin.dto;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class EmployeeTrainingCreateRequest {
    private String batchName;
    private Date trainingDate;
    private String batchCode;
    private String process;
    private String faculty;
    private int maxAttempts;
    private int totalMarks;
    private int passingMarks;
    private boolean certificateRequired;

    private List<Long> ourEmployeeIds;  // Only IDs here
}
