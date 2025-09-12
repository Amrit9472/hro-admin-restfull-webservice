package com.eos.admin.service;

import com.eos.admin.dto.ScoreMetaDTO;
import com.eos.admin.dto.TrainingScoreDTO;
import com.eos.admin.entity.TrainingScore;

import java.util.List;

public interface TrainingScoreService {
    List<TrainingScore> getScoresByBatchId(Long batchId);
    TrainingScore getScore(Long batchId, Long employeeId);
    TrainingScore saveScore(TrainingScoreDTO dto);
    List<TrainingScore> saveAllScores(List<TrainingScoreDTO> dtos);
    ScoreMetaDTO getScoreMeta(Long batchId);
}
