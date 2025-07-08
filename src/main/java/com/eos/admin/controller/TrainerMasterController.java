package com.eos.admin.controller;

import com.eos.admin.dto.TrainerMasterDTO;
import com.eos.admin.service.TrainerMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainer-master")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class TrainerMasterController {

    private final TrainerMasterService trainerMasterService;

    @PostMapping("/assign")
    public ResponseEntity<?> assignProcessesToTrainer(@RequestBody TrainerMasterDTO dto) {
        try {
            trainerMasterService.saveTrainerProcesses(dto);
            return ResponseEntity.ok("Processes assigned successfully.");
        } catch (Exception e) {
            log.error("Failed to assign processes", e);
            return ResponseEntity.internalServerError().body("Error assigning processes");
        }
    }

    @GetMapping("/trainer-name")
    public ResponseEntity<?> getTrainerName(@RequestParam("trainerId") String trainerId) {
        return trainerMasterService.getTrainerNameById(trainerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all-processes")
    public ResponseEntity<?> getAllProcesses() {
        return ResponseEntity.ok(trainerMasterService.getAllProcesses());
    }

    @GetMapping("/assigned")
    public ResponseEntity<?> getAssignedProcesses(@RequestParam("trainerId") String trainerId) {
        return ResponseEntity.ok(trainerMasterService.getAssignedTrainerData(trainerId));
    }

    // New endpoint to fetch all trainers with their assigned processes
    @GetMapping("/all-trainers")
    public ResponseEntity<?> getAllTrainerMasters() {
        try {
            List<TrainerMasterDTO> allTrainerMasters = trainerMasterService.getAllTrainerMasters();
            return ResponseEntity.ok(allTrainerMasters);
        } catch (Exception e) {
            log.error("Failed to fetch all trainer masters", e);
            return ResponseEntity.internalServerError().body("Error fetching trainer masters");
        }
    }
}
