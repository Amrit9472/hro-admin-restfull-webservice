package com.eos.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class TrainingScoreDTO {
    private Long employeeId;
    private Long trainingBatchId;
    private List<Integer> scores;
    private boolean certified;
}
