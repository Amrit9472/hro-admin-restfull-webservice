package com.eos.admin.dto;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TrainingCandidateDTO {
	private Long ourEmployeeId;
    private String name;
    private String process;
    private LocalDateTime submissionDate;
    private String type;

    // Getters & Setters
}
