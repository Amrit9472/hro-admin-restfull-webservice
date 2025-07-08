package com.eos.admin.serviceImpl;

import com.eos.admin.dto.TrainerMasterDTO;
import com.eos.admin.entity.OurUsers;
import com.eos.admin.entity.TrainerMaster;
import com.eos.admin.repository.UsersRepository;
import com.eos.admin.repository.TrainerMasterRepository;
import com.eos.admin.service.TrainerMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerMasterServiceImpl implements TrainerMasterService {

    private final TrainerMasterRepository trainerMasterRepository;
    private final UsersRepository ourUsersRepository;

    @Override
    public void saveTrainerProcesses(TrainerMasterDTO dto) {
        // Check if user exists and role is TRAINER
        Optional<OurUsers> userOptional = ourUsersRepository.findByEmail(dto.getTrainerId());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        OurUsers user = userOptional.get();

        if (!"TRAINER".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException("User is not a TRAINER.");
        }

        // Now continue your existing logic
        List<TrainerMaster> existingAssignments = trainerMasterRepository.findByTrainerId(dto.getTrainerId());
        Set<String> existingProcesses = existingAssignments.stream()
                .map(TrainerMaster::getProcess)
                .collect(Collectors.toSet());

        if (existingProcesses.isEmpty()) {
            String originalProcess = user.getProcess();
            if (originalProcess != null && !originalProcess.trim().isEmpty()) {
                TrainerMaster originalAssignment = new TrainerMaster(
                        null,
                        dto.getTrainerId(),
                        user.getName(),
                        originalProcess.trim()
                );
                trainerMasterRepository.save(originalAssignment);
                existingProcesses.add(originalProcess.trim());
            }
        }

        List<TrainerMaster> newAssignments = dto.getProcesses().stream()
                .map(String::trim)
                .filter(process -> !existingProcesses.contains(process))
                .map(process -> new TrainerMaster(null, dto.getTrainerId(), dto.getTrainerName(), process))
                .collect(Collectors.toList());

        trainerMasterRepository.saveAll(newAssignments);
    }


    @Override
    public Optional<String> getTrainerNameById(String trainerId) {
        return ourUsersRepository.findByEmail(trainerId)
                .map(user -> {
                    if ("TRAINER".equalsIgnoreCase(user.getRole())) {
                        return user.getName();
                    } else {
                        return "NO TRAINER";
                    }
                });
    }

    @Override
    public List<String> getAllProcesses() {
        return ourUsersRepository.findDistinctProcesses();
    }

    @Override
    public List<TrainerMasterDTO> getAssignedTrainerData(String trainerId) {
        List<TrainerMaster> records = trainerMasterRepository.findByTrainerId(trainerId);
        if (records.isEmpty()) return List.of();

        TrainerMasterDTO dto = new TrainerMasterDTO();
        dto.setTrainerId(records.get(0).getTrainerId());
        dto.setTrainerName(records.get(0).getTrainerName());
        dto.setProcesses(records.stream().map(TrainerMaster::getProcess).collect(Collectors.toList()));

        return List.of(dto);
    }

    // New method implementation
    @Override
    public List<TrainerMasterDTO> getAllTrainerMasters() {
        List<TrainerMaster> allRecords = trainerMasterRepository.findAll();

        return allRecords.stream()
                .collect(Collectors.groupingBy(TrainerMaster::getTrainerId))
                .entrySet()
                .stream()
                .map(entry -> {
                    String trainerId = entry.getKey();
                    List<TrainerMaster> trainerRecords = entry.getValue();

                    TrainerMasterDTO dto = new TrainerMasterDTO();
                    dto.setTrainerId(trainerId);
                    dto.setTrainerName(trainerRecords.get(0).getTrainerName());
                    dto.setProcesses(trainerRecords.stream()
                            .map(TrainerMaster::getProcess)
                            .collect(Collectors.toList()));

                    return dto;
                })
                .collect(Collectors.toList());
    }
}
