package com.eos.admin.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eos.admin.dto.InductionAttendanceDTO;
import com.eos.admin.entity.Employee;
import com.eos.admin.entity.InductionAttendance;
import com.eos.admin.entity.OurEmployees;
import com.eos.admin.entity.TrainingBatch;
import com.eos.admin.enums.AttendanceType;
import com.eos.admin.repository.EmployeeRepository;
import com.eos.admin.repository.InductionAttendanceRepository;
import com.eos.admin.repository.OurEmployeeRepository;
import com.eos.admin.repository.TrainingBatchRepository;
import com.eos.admin.service.InductionAttendanceService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InductionAttendanceServiceImpl implements InductionAttendanceService {


    private OurEmployeeRepository ourEmployeeRepository;

    private EmployeeRepository employeeRepository;
    

    private TrainingBatchRepository trainingBatchRepository;


    private InductionAttendanceRepository attendanceRepository;
    private final ModelMapper modelMapper;
    
    

    public InductionAttendanceServiceImpl(OurEmployeeRepository ourEmployeeRepository,
			EmployeeRepository employeeRepository, TrainingBatchRepository trainingBatchRepository,
			InductionAttendanceRepository attendanceRepository, ModelMapper modelMapper) {
		super();
		this.ourEmployeeRepository = ourEmployeeRepository;
		this.employeeRepository = employeeRepository;
		this.trainingBatchRepository = trainingBatchRepository;
		this.attendanceRepository = attendanceRepository;
		this.modelMapper = modelMapper;
	}

	@Override
    public List<String> getAllDistinctProcesses() {
        try {
            return ourEmployeeRepository.findDistinctProcesses();
        } catch (Exception e) {
            log.error("Failed to fetch distinct processes", e);
            return Collections.emptyList(); // return safe default
        }
    }

    @Override
    public List<InductionAttendanceDTO> getEmployeesByDateAndProcess(Date date, String process) {
        try {
            List<OurEmployees> filtered = ourEmployeeRepository.findByDateAndProcess( process,date);
            List<InductionAttendanceDTO> response = new ArrayList<>();
            
            for (OurEmployees ourEmployees : filtered) {
                InductionAttendanceDTO dto = new InductionAttendanceDTO();
                dto.setOurEmployeeId(ourEmployees.getOurEmployeeId());
                dto.setName(ourEmployees.getEmployee().getFullName());
//                dto.setEmployeeId(ourEmployees.getEmployee().getId());
//                dto.setEmployeeName(employee.getEmployee().getFullName());
                dto.setProcess(ourEmployees.getProcess());
//                dto.setDate(employee.getDate());
                // Add more fields as necessary

                response.add(dto);
            }

            return response;
        } catch (Exception e) {
            // Log exception and handle accordingly
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    
    
    @Override
    public List<InductionAttendanceDTO> getEmployeesByBatch(Long batchId) {
        List<InductionAttendanceDTO> response = new ArrayList<>();

        try {
            Optional<TrainingBatch> optionalBatch = trainingBatchRepository.findById(batchId);
            if (optionalBatch.isPresent()) {
                TrainingBatch batch = optionalBatch.get();
                List<Long> candidateIds = batch.getCandidateIds();

                for (Long empId : candidateIds) {
                    Employee emp = employeeRepository.findById(empId).orElse(null);
                    if (emp != null) {
                        InductionAttendanceDTO dto = new InductionAttendanceDTO();
                        dto.setEmployeeId(emp.getId());
                        dto.setName(emp.getFullName());
//                        dto.setDate(batch.getTrainingStartDate());
                        dto.setProcess(batch.getProcess());
                        dto.setType(AttendanceType.TRAINING);
                        response.add(dto);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch training batch employees for batchId={}", batchId, e);
        }

        return response;
    }
    @Override
    public List<Map<String, Object>> getAllTrainingBatches() {
        List<Map<String, Object>> batches = new ArrayList<>();

        trainingBatchRepository.findAll().forEach(batch -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", batch.getId());
            map.put("batchCode", batch.getBatchCode());
            map.put("trainingStartDate", batch.getTrainingStartDate());
            batches.add(map);
        });

        return batches;
    }




    @Override
    public void  saveAttendance(List<InductionAttendanceDTO> attendanceList) {
    	  List<InductionAttendance> entities = new ArrayList<>();

    	    for (InductionAttendanceDTO dto : attendanceList) {
    	        InductionAttendance attendance = modelMapper.map(dto, InductionAttendance.class);

    	        // Fetch managed entity from DB
    	        OurEmployees employee = ourEmployeeRepository.findById(dto.getOurEmployeeId())
    	            .orElseThrow(() -> new RuntimeException("Employee not found: " + dto.getOurEmployeeId()));

    	        attendance.setOurEmployee(employee);
    	        entities.add(attendance);
    	    }

    	    attendanceRepository.saveAll(entities);
    	
    }
    
//        try {
//            List<InductionAttendance> entities = attendanceList.stream().map(dto -> {
//                InductionAttendance a = new InductionAttendance();
//                a.setDate(dto.getDate());
//                a.setEmployeeId(dto.getEmployeeId());
//                a.setProcess(dto.getProcess());
//                a.setStatus(dto.getStatus());
//                a.setMarker(dto.getMarker());
//                a.setType(dto.getType());
//                a.setSubmissionDate(LocalDateTime.now());
//                return a;
//            }).collect(Collectors.toList());
//
//            attendanceRepository.saveAll(entities);
//        } catch (Exception e) {
//            log.error("Error saving attendance records", e);
//            throw new RuntimeException("Failed to save attendance records", e);
//        }
//    }

    }