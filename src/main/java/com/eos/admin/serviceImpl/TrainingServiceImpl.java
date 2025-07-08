package com.eos.admin.serviceImpl;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.eos.admin.dto.EmployeeDTO2;
import com.eos.admin.dto.EmployeeTrainingCreateRequest;
import com.eos.admin.dto.EmployeeTrainingDTO;
import com.eos.admin.entity.EmployeeTraining;
import com.eos.admin.entity.OurEmployees;
import com.eos.admin.entity.ProcessEntity;
import com.eos.admin.repository.EmployeeTrainingRepository;
import com.eos.admin.repository.OurEmployeeRepository;
import com.eos.admin.repository.ProcessRepository;
import com.eos.admin.service.TrainingService;

@Service
public class TrainingServiceImpl implements TrainingService {
	
	private OurEmployeeRepository ourEmployeeRepository;
    private EmployeeTrainingRepository employeeTrainingRepository;
    private final ProcessRepository processRepository;
    
    
	public TrainingServiceImpl(OurEmployeeRepository ourEmployeeRepository,
			EmployeeTrainingRepository employeeTrainingRepository,ProcessRepository processRepository) {
		super();
		this.ourEmployeeRepository = ourEmployeeRepository;
		this.employeeTrainingRepository = employeeTrainingRepository;
		this.processRepository = processRepository;
	}



	@Override
	public EmployeeTraining createTraining(EmployeeTrainingCreateRequest request ,String location) {
		// TODO Auto-generated method stub
		 // Step 1: Create the training batch
	    EmployeeTraining training = new EmployeeTraining();
	    training.setBatchName(request.getBatchName());
	    training.setTrainingDate(request.getTrainingDate());
	    training.setBatchCode(batchCodeFormatting(request,location));
	    training.setProcess(request.getProcess());
	    training.setFaculty(request.getFaculty());
	    training.setMaxAttempts(request.getMaxAttempts());
	    training.setTotalMarks(request.getTotalMarks());
	    training.setPassingMarks(request.getPassingMarks());
	    training.setCertificateRequired(request.isCertificateRequired());

	    // Step 2: Assign existing OurEmployees to the batch
	    List<OurEmployees> employees = ourEmployeeRepository.findAllById(request.getOurEmployeeIds());

	    for (OurEmployees emp : employees) {
	        emp.setEmployeeTraining(training); // assign batch to employee
	    }

	    training.setEmployees(employees); // assign employees to batch

	    // Step 3: Save batch (cascades to employees if cascade set)
	    return employeeTrainingRepository.save(training);
	}

	@Override
	public Optional<EmployeeTrainingDTO> getTrainingWithEmployees(String batchCode) {
        Optional<EmployeeTraining> optionalTraining = employeeTrainingRepository.findByBatchCode(batchCode);

        if (optionalTraining.isEmpty()) {
            return Optional.empty();
        }

        EmployeeTraining training = optionalTraining.get();
        List<EmployeeDTO2> employeeDTOs = training.getEmployees().stream().map( emp ->{
        	EmployeeDTO2 dto = new EmployeeDTO2();
//        	dto.setId(emp.getOurEmployeeId());
        	dto.setName(emp.getEmployee().getFullName());
        	return dto;
        }) .collect(Collectors.toList());

        EmployeeTrainingDTO dto = new EmployeeTrainingDTO(
                training.getBatchId(),
                training.getBatchName(),
                training.getTrainingDate(),
                training.getBatchCode(),
                training.getProcess(),
                training.getFaculty(),
                training.getMaxAttempts(),
                training.getTotalMarks(),
                training.getPassingMarks(),
                training.isCertificateRequired(),
                employeeDTOs
        );

        return Optional.of(dto);
    }
	
	@Override
    public Optional<EmployeeTrainingDTO> getTrainingWithEmployees2(String batchCode) {

		
		Optional<EmployeeTraining> training = employeeTrainingRepository.findByBatchCode(batchCode);
	    if (training.isPresent()) {
	        EmployeeTraining empTraining = training.get();
	        EmployeeTrainingDTO dto = new EmployeeTrainingDTO();
	        
	        dto.setBatchId(empTraining.getBatchId());
	        dto.setBatchName(empTraining.getBatchName());
	        dto.setTrainingDate(empTraining.getTrainingDate());
	        dto.setBatchCode(empTraining.getBatchCode());
	        dto.setProcess(empTraining.getProcess());
	        dto.setFaculty(empTraining.getFaculty());
	        dto.setMaxAttempts(empTraining.getMaxAttempts());
	        dto.setTotalMarks(empTraining.getTotalMarks());
	        dto.setPassingMarks(empTraining.getPassingMarks());
	        dto.setCertificateRequired(empTraining.isCertificateRequired());

	        // Map list of employees to EmployeeDTO (fixing the stream mapping)
	        dto.setEmployees(empTraining.getEmployees().stream()
	            .map(employee -> new EmployeeDTO2(employee.getOurEmployeeId(),employee.getEmployee().getFullName(), employee.getEmployee().getEmail()))
	            .collect(Collectors.toList()));
	        
	        return Optional.of(dto);
	    }
	    
	    return Optional.empty();
    }
	
	
	private String batchCodeFormatting(EmployeeTrainingCreateRequest request, String location) {
	    // Get first 3 letters of location and process, uppercase
	    String locationPart = location.length() >= 3 ? location.substring(0, 3).toUpperCase() : location.toUpperCase();
	    String processName = request.getProcess();
	    String processPart = processName.length() >= 3 ? processName.substring(0, 3).toUpperCase() : processName.toUpperCase();

	    // Get processCode from repository
	    Optional<ProcessEntity> processOpt = processRepository.findByProcessName(processName);
	    if (processOpt.isEmpty()) {
	        throw new IllegalArgumentException("Invalid process name: " + processName);
	    }
	    Long processCode = processOpt.get().getProcessCode();

	    // Generate a unique 3-digit random number as a string (e.g., 001 to 999)
	    String randomDigits = String.format("%03d", new Random().nextInt(999) + 1);

	    // Concatenate all parts
	    String batchCode = locationPart + processPart + processCode + randomDigits;

	    return batchCode;
	}

}
