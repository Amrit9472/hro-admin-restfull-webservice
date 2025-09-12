package com.eos.admin.serviceImpl;

import com.eos.admin.dto.ScoreMetaDTO;
import com.eos.admin.dto.TrainingScoreDTO;
import com.eos.admin.entity.TrainingScore;
import com.eos.admin.repository.TrainingBatchRepository;
import com.eos.admin.repository.TrainingScoreRepository;
import com.eos.admin.service.TrainingScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingScoreServiceImpl implements TrainingScoreService {

    @Autowired
    private TrainingScoreRepository scoreRepository;

    @Override
    public List<TrainingScore> getScoresByBatchId(Long batchId) {
        return scoreRepository.findByTrainingBatchId(batchId);
    }

    @Override
    public TrainingScore getScore(Long batchId, Long employeeId) {
        return scoreRepository.findByTrainingBatchIdAndEmployeeId(batchId, employeeId);
    }

    @Override
    public TrainingScore saveScore(TrainingScoreDTO dto) {
        TrainingScore score = TrainingScore.builder()
                .employeeId(dto.getEmployeeId())
                .trainingBatchId(dto.getTrainingBatchId())
                .scores(dto.getScores())
                .certified(dto.isCertified())
                .build();

        return scoreRepository.save(score);
    }

    @Override
    public List<TrainingScore> saveAllScores(List<TrainingScoreDTO> dtos) {
        List<TrainingScore> entities = dtos.stream().map(dto -> TrainingScore.builder()
                .employeeId(dto.getEmployeeId())
                .trainingBatchId(dto.getTrainingBatchId())
                .scores(dto.getScores())
                .certified(dto.isCertified())
                .build()).collect(Collectors.toList());

        return scoreRepository.saveAll(entities);
    }
    
    @Autowired
    private TrainingBatchRepository batchRepository;

    @Override
    public ScoreMetaDTO getScoreMeta(Long batchId) {
        return batchRepository.findById(batchId).map(batch -> {
            ScoreMetaDTO dto = new ScoreMetaDTO();
            dto.setBatchId(batchId);
            dto.setMaxAttempts(batch.getMaxAttempts());
            dto.setPassingMarks(batch.getPassingMarks());
            return dto;
        }).orElseThrow(() -> new RuntimeException("Batch not found with id: " + batchId));
    }

}
