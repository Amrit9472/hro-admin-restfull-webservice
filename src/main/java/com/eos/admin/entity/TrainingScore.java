package com.eos.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "training_score")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "training_batch_id", nullable = false)
    private Long trainingBatchId;

    @ElementCollection
    @CollectionTable(name = "training_score_attempts", joinColumns = @JoinColumn(name = "training_score_id"))
    @Column(name = "score")
    private List<Integer> scores;

    private boolean certified;
}
