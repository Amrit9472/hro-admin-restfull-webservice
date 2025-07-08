package com.eos.admin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerMasterDTO {

    private String trainerId;

    private String trainerName;

    private List<String> processes; // multiple processes selected by user
}
