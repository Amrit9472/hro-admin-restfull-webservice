package com.eos.admin.repository;

import com.eos.admin.entity.TrainingScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingScoreRepository extends JpaRepository<TrainingScore, Long> {
    List<TrainingScore> findByTrainingBatchId(Long trainingBatchId);
    TrainingScore findByTrainingBatchIdAndEmployeeId(Long batchId, Long employeeId);
}
