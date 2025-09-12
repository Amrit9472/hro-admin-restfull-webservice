package com.eos.admin.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@Data
public class TrainingBatchDTO {
    private Long id;
    private String batchCode;  // new
    private String process;
    private String city;       // optional: to pass from frontend or extract in backend
    private LocalDate trainingStartDate;
    private String faculty;
    private int maxAttempts;
    private int totalMarks;
    private int passingMarks;
    private boolean certificateRequired;
    private List<Long> candidateIds;
    
    private int trainingDays;
    
    public TrainingBatchDTO(Long id, String batchCode, LocalDate trainingStartDate, String process, int trainingDays) {
        this.id = id;
        this.batchCode = batchCode;
        this.trainingStartDate = trainingStartDate;
        this.process = process;
        this.trainingDays = trainingDays;
    }
}

