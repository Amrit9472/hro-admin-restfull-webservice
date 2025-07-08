package com.eos.admin.dto;

import com.eos.admin.dto.TrainerMasterDTO;

import java.util.List;
import java.util.Optional;

public interface TrainerMasterService {

    void saveTrainerProcesses(TrainerMasterDTO dto);

    List<TrainerMasterDTO> getTrainerProcesses(String trainerId);
    
    Optional<String> getTrainerNameById(String trainerId);
}