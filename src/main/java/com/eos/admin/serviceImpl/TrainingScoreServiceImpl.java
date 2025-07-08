package com.eos.admin.serviceImpl;

import com.eos.admin.dto.ScoreMetaDTO;
import com.eos.admin.dto.TrainingScoreDTO;
import com.eos.admin.entity.EmployeeTraining;
import com.eos.admin.entity.TrainingScore;
import com.eos.admin.repository.EmployeeTrainingRepository;
import com.eos.admin.repository.TrainingBatchRepository;
import com.eos.admin.repository.TrainingScoreRepository;
import com.eos.admin.service.TrainingScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainingScoreServiceImpl implements TrainingScoreService {

    @Autowired
    private TrainingScoreRepository scoreRepository;
    
    @Autowired
    private EmployeeTrainingRepository trainingRepository;

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
//                .trainingBatchId(dto.getTrainingBatchCode())
                .scores(dto.getScores())
                .certified(dto.isCertified())
                .build();

        return scoreRepository.save(score);
    }

//    @Override
//    public List<TrainingScore> saveAllScores(List<TrainingScoreDTO> dtos) {
//        List<TrainingScore> entities = dtos.stream().map(dto -> TrainingScore.builder()
//                .employeeId(dto.getEmployeeId())
//                .trainingBatchId(dto.getTrainingBatchId())
//                .scores(dto.getScores())
//                .certified(dto.isCertified())
//                .build()).collect(Collectors.toList());
//
//        return scoreRepository.saveAll(entities);
//    }
    
    @Override
    public List<TrainingScore> saveAllScores(List<TrainingScoreDTO> dtos) {
        List<TrainingScore> entities = new ArrayList<>();

        for (TrainingScoreDTO dto : dtos) {
            Optional<EmployeeTraining> batchOpt = trainingRepository.findByBatchCode(dto.getTrainingBatchCode());
            if (batchOpt.isPresent()) {
            	Long batchId = batchOpt.get().getBatchId(); // this is the actual Long id to store

                TrainingScore score = TrainingScore.builder()
                        .employeeId(dto.getEmployeeId())
                        .trainingBatchId(batchId)
                        .scores(dto.getScores())
                        .certified(dto.isCertified())
                        .build();

                entities.add(score);
            } else {
                throw new RuntimeException("Invalid batch code: " + dto.getTrainingBatchCode());
            }
        }

        return scoreRepository.saveAll(entities);
    }
    
    @Autowired
    private TrainingBatchRepository batchRepository;

//    @Override
//    public ScoreMetaDTO getScoreMeta(Long batchId) {
//        return batchRepository.findById(batchId).map(batch -> {
//            ScoreMetaDTO dto = new ScoreMetaDTO();
//            dto.setBatchId(batchId);
//            dto.setMaxAttempts(batch.getMaxAttempts());
//            dto.setPassingMarks(batch.getPassingMarks());
//            return dto;
//        }).orElseThrow(() -> new RuntimeException("Batch not found with id: " + batchId));
//    }
    
    @Override
    public ScoreMetaDTO getScoreMeta(String batchCode) {
        return trainingRepository.findByBatchCode(batchCode)
            .map(batch -> {
                ScoreMetaDTO dto = new ScoreMetaDTO();
                dto.setBatchId(batch.getBatchId()); // optional
                dto.setMaxAttempts(batch.getMaxAttempts());
                dto.setPassingMarks(batch.getPassingMarks());
                return dto;
            })
            .orElseThrow(() -> new RuntimeException("Batch not found with code: " + batchCode));
    }


}
