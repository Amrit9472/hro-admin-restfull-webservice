package com.eos.admin.controller;

import com.eos.admin.dto.TrainingBatchDTO;
import com.eos.admin.dto.TrainingCandidateListDTO;
import com.eos.admin.service.TrainingBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/training-batch")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class TrainingBatchController {

    private final TrainingBatchService trainingBatchService;

    // Get all distinct processes
    @GetMapping("/processes")
    public ResponseEntity<?> getProcesses() {
        try {
            List<String> processes = trainingBatchService.getAllProcesses();
            return ResponseEntity.ok(processes);
        } catch (Exception e) {
            log.error("Error fetching processes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve processes");
        }
    }

    // Get candidates filtered by process (for table display)
    @GetMapping("/candidates")
    public ResponseEntity<?> getCandidates(@RequestParam("process") String process) {
        try {
            List<TrainingCandidateListDTO> candidates = trainingBatchService.getEmployeeDetailsByProcess(process);
            return ResponseEntity.ok(candidates);
        } catch (Exception e) {
            log.error("Error fetching candidates for process: {}", process, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve candidates");
        }
    }

    // Save training batch details from modal
    @PostMapping("/batch")
    public ResponseEntity<?> createTrainingBatch(@RequestBody TrainingBatchDTO dto) {
        try {
            TrainingBatchDTO savedDto = trainingBatchService.saveTrainingBatch(dto);
            return ResponseEntity.ok(savedDto);
        } catch (Exception e) {
            log.error("Error creating training batch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create training batch");
        }
    }
    
    @GetMapping("/batches")
    public ResponseEntity<List<TrainingBatchDTO>> getAllTrainingBatchesWithTrainingDays() {
        List<TrainingBatchDTO> batches = trainingBatchService.getAllBatchesWithTrainingDays();
        return ResponseEntity.ok(batches);
    }

    
    // Get process code by process name
    @GetMapping("/process-code")
    public ResponseEntity<?> getProcessCode(@RequestParam("process") String process) {
        try {
            String processCode = trainingBatchService.getProcessCodeByName(process);
            if (processCode == null || processCode.equals("XXX")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Process code not found"));
            }
            return ResponseEntity.ok(Map.of("processCode", processCode));
        } catch (Exception e) {
            log.error("Error fetching process code for process: {}", process, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve process code");
        }
    }

    // Get next batch serial number by prefix
    @GetMapping("/next-batch-serial")
    public ResponseEntity<?> getNextBatchSerial(@RequestParam("prefix") String prefix) {
        try {
            int nextSerial = trainingBatchService.getNextBatchSerial(prefix);
            return ResponseEntity.ok(Map.of("nextSerial", nextSerial));
        } catch (Exception e) {
            log.error("Error fetching next batch serial for prefix: {}", prefix, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve batch serial");
        }
    }
}
