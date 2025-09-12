package com.eos.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "training_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String batchCode;

    private String process;

    private LocalDate trainingStartDate;

    private String faculty;

    private int maxAttempts;

    private int totalMarks;

    private int passingMarks;

    private boolean certificateRequired;

    @ElementCollection
    @CollectionTable(name = "training_batch_candidates", joinColumns = @JoinColumn(name = "training_batch_id"))
    @Column(name = "employee_id")
    private List<Long> candidateIds;
    
    private int trainingDays;
}
