package com.eos.admin.serviceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.eos.admin.dto.TrainingBatchDTO;
import com.eos.admin.dto.TrainingCandidateListDTO;
import com.eos.admin.entity.TrainingBatch;
import com.eos.admin.enums.AttendanceType;
import com.eos.admin.repository.InductionAttendanceRepository;
import com.eos.admin.repository.OurEmployeeRepository;
import com.eos.admin.repository.TrainingBatchRepository;
import com.eos.admin.repository.UsersRepository;
import com.eos.admin.service.TrainingBatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingBatchServiceImpl implements TrainingBatchService {

    private final OurEmployeeRepository ourEmployeeRepository;
    private final TrainingBatchRepository trainingBatchRepository;
    private final UsersRepository usersRepository;
    private final InductionAttendanceRepository inductionAttendanceRepository;

    @Override
    public List<String> getAllProcesses() {
        try {
            return ourEmployeeRepository.findDistinctProcesses();
        } catch (Exception e) {
            log.error("Error fetching all processes", e);
            throw new RuntimeException("Failed to fetch processes");
        }
    }
    
    public List<TrainingBatchDTO> getAllBatchesWithTrainingDays() {
        return trainingBatchRepository.findAllWithTrainingDays();
    }

    @Override
    public TrainingBatchDTO saveTrainingBatch(TrainingBatchDTO dto) {
        try {
            String city = dto.getCity();
            if (city == null || city.length() < 3) {
                throw new IllegalArgumentException("City must be at least 3 characters");
            }
            String cityPrefix = city.substring(0, 3).toUpperCase();

            String process = dto.getProcess();
            String processInitials = process.length() >= 3 ? process.substring(0, 3).toUpperCase() : process.toUpperCase();

            String processCode = usersRepository.findProcessCodeByProcess(process);
            if (processCode == null) {
                log.warn("Process code not found for process: {}", process);
                processCode = "XXX";
            }

            String batchCodePrefix = cityPrefix + processInitials + processCode;

            int count = trainingBatchRepository.countByBatchCodeStartingWith(batchCodePrefix);
            String serial = String.format("%03d", count + 1);

            String batchCode = batchCodePrefix + serial;

            TrainingBatch entity = TrainingBatch.builder()
                    .process(process)
                    .trainingStartDate(dto.getTrainingStartDate())
                    .faculty(dto.getFaculty())
                    .maxAttempts(dto.getMaxAttempts())
                    .totalMarks(dto.getTotalMarks())
                    .passingMarks(dto.getPassingMarks())
                    .certificateRequired(dto.isCertificateRequired())
                    .batchCode(batchCode)
                    .candidateIds(dto.getCandidateIds())
                    .build();

            TrainingBatch saved = trainingBatchRepository.save(entity);

            dto.setId(saved.getId());
            dto.setBatchCode(batchCode);

            return dto;
        } catch (Exception e) {
            log.error("Error saving training batch", e);
            throw new RuntimeException("Failed to save training batch");
        }
    }

    @Override
    public List<TrainingCandidateListDTO> getEmployeeDetailsByProcess(String process) {
        List<Object[]> results = ourEmployeeRepository.findEmployeeDetailsByProcess(process);

        return results.stream().map(row -> {
            Long employeeId = ((Number) row[0]).longValue();
            String proc = (String) row[1];
            String fullName = (String) row[2];
            LocalDate inductionDate = row[3] != null ? ((java.sql.Timestamp) row[3]).toLocalDateTime().toLocalDate() : null;
            AttendanceType type = row[4] != null ? AttendanceType.valueOf(row[4].toString()) : null;

            return new TrainingCandidateListDTO(employeeId, proc, fullName, inductionDate, type);
        }).toList();
    }

    @Override
    public String getProcessCodeByName(String process) {
        try {
            String codeFromDB = usersRepository.findProcessCodeByProcess(process);
            if (codeFromDB != null) {
                return codeFromDB;
            }
            log.warn("Process code not found for process: {}", process);
            return "XXX";
        } catch (Exception e) {
            log.error("Error fetching process code by name: {}", process, e);
            throw new RuntimeException("Failed to fetch process code");
        }
    }

    @Override
    public int getNextBatchSerial(String prefix) {
        try {
            List<TrainingBatch> batches = trainingBatchRepository.findByBatchCodeStartingWith(prefix);

            if (batches.isEmpty()) {
                return 1;
            }

            int maxSerial = batches.stream()
                .mapToInt(batch -> {
                    String batchCode = batch.getBatchCode();
                    String serialStr = batchCode.substring(prefix.length());
                    try {
                        return Integer.parseInt(serialStr);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid serial format in batch code: {}", batchCode);
                        return 0;
                    }
                })
                .max()
                .orElse(0);

            return maxSerial + 1;
        } catch (Exception e) {
            log.error("Error calculating next batch serial for prefix: {}", prefix, e);
            throw new RuntimeException("Failed to calculate next batch serial");
        }
    }
}
