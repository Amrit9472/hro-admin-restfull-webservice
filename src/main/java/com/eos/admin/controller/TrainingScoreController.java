package com.eos.admin.controller;

import com.eos.admin.dto.TrainingScoreDTO;
import com.eos.admin.service.TrainingScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/training-scores")
@Slf4j
public class TrainingScoreController {

    @Autowired
    private TrainingScoreService trainingScoreService;

    @GetMapping("/scores")
    public ResponseEntity<?> getScoresByBatchId(@RequestParam Long batchId) {
        try {
            log.info("Fetching scores for batchId={}", batchId);
            return ResponseEntity.ok(trainingScoreService.getScoresByBatchId(batchId));
        } catch (Exception e) {
            log.error("Error fetching scores for batchId={}", batchId, e);
            return ResponseEntity.internalServerError().body("Error fetching scores");
        }
    }

    @GetMapping("/score")
    public ResponseEntity<?> getScoreByBatchAndEmployee(@RequestParam Long batchId, @RequestParam Long employeeId) {
        try {
            log.info("Fetching score for batchId={}, employeeId={}", batchId, employeeId);
            return ResponseEntity.ok(trainingScoreService.getScore(batchId, employeeId));
        } catch (Exception e) {
            log.error("Error fetching score for batchId={}, employeeId={}", batchId, employeeId, e);
            return ResponseEntity.internalServerError().body("Error fetching score");
        }
    }

    @PostMapping("/save-score")
    public ResponseEntity<?> saveSingleScore(@RequestBody TrainingScoreDTO dto) {
        try {
            log.info("Saving single score: {}", dto);
            return ResponseEntity.ok(trainingScoreService.saveScore(dto));
        } catch (Exception e) {
            log.error("Error saving single score: {}", dto, e);
            return ResponseEntity.internalServerError().body("Error saving score");
        }
    }

    @PostMapping("/save-scores")
    public ResponseEntity<?> saveMultipleScores(@RequestBody List<TrainingScoreDTO> dtos) {
        try {
            log.info("Saving multiple scores: count={}", dtos.size());
            return ResponseEntity.ok(trainingScoreService.saveAllScores(dtos));
        } catch (Exception e) {
            log.error("Error saving multiple scores", e);
            return ResponseEntity.internalServerError().body("Error saving scores");
        }
    }

    @GetMapping("/score-meta")
    public ResponseEntity<?> getScoreMeta(@RequestParam("batchId") Long batchId) {
        try {
            log.info("Fetching score meta for batchId={}", batchId);
            return ResponseEntity.ok(trainingScoreService.getScoreMeta(batchId));
        } catch (Exception e) {
            log.error("Error fetching score meta for batchId={}", batchId, e);
            return ResponseEntity.internalServerError().body("Error fetching score meta");
        }
    }
}
