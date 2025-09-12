package com.eos.admin.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.eos.admin.dto.TrainingBatchDTO;
import com.eos.admin.dto.TrainingCandidateListDTO;

@Service
public interface TrainingBatchService {

    List<String> getAllProcesses();

    TrainingBatchDTO saveTrainingBatch(TrainingBatchDTO dto);

    List<TrainingCandidateListDTO> getEmployeeDetailsByProcess(String process);

    String getProcessCodeByName(String process);

    int getNextBatchSerial(String prefix);
    
    public List<TrainingBatchDTO> getAllBatchesWithTrainingDays();

}
