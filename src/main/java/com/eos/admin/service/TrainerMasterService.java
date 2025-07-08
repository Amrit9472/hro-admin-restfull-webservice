package com.eos.admin.service;

import com.eos.admin.dto.TrainerMasterDTO;

import java.util.List;
import java.util.Optional;

public interface TrainerMasterService {

    void saveTrainerProcesses(TrainerMasterDTO dto);

    Optional<String> getTrainerNameById(String trainerId);

    List<String> getAllProcesses();

    List<TrainerMasterDTO> getAssignedTrainerData(String trainerId);

    // New method to get all trainers with processes
    List<TrainerMasterDTO> getAllTrainerMasters();
}

