package com.eos.admin.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eos.admin.dto.BatchCodeDTO;
import com.eos.admin.dto.EmployeeTrainingCreateRequest;
import com.eos.admin.dto.EmployeeTrainingDTO;
import com.eos.admin.entity.EmployeeTraining;
import com.eos.admin.repository.EmployeeTrainingRepository;
import com.eos.admin.service.TrainingService;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin("http://localhost:5173")
@Slf4j
@RestController
@RequestMapping("/api/insert/training")
public class EmployeeTrainingController {

	private TrainingService trainingService;
    private EmployeeTrainingRepository employeeTrainingRepository;
	public EmployeeTrainingController(TrainingService trainingService,EmployeeTrainingRepository employeeTrainingRepository) {
		super();
		this.trainingService = trainingService;
		this.employeeTrainingRepository =employeeTrainingRepository;
	}

	@PostMapping("/batch/{location}")
	public ResponseEntity<?> createTraining(@RequestBody EmployeeTrainingCreateRequest request,@PathVariable("location") String location) {
		try {
			EmployeeTraining saved = trainingService.createTraining(request ,location);
			return ResponseEntity.status(HttpStatus.CREATED).body(saved);
		} catch (Exception e) {
			log.error("Error while creating batch: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create training");
		}
	}

	
	@GetMapping("/batchcodes")
	public ResponseEntity<List<BatchCodeDTO>> getAllBatchCodes() {
	    List<EmployeeTraining> trainings = employeeTrainingRepository.findAll();
	    List<BatchCodeDTO> dtos = trainings.stream()
	            .map(t -> new BatchCodeDTO(t.getBatchId(), t.getBatchCode()))
	            .collect(Collectors.toList());
	    return ResponseEntity.ok(dtos);
	}
	
	@GetMapping("/batchcode/{code}")
	public ResponseEntity<?> getTrainingByBatchCode(@PathVariable("code") String batchCode) {
	    Optional<EmployeeTraining> optionalTraining = employeeTrainingRepository.findByBatchCode(batchCode);

	    if (optionalTraining.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body("No training found with batch code: " + batchCode);
	    }
//
//	    return ResponseEntity.ok(optionalTraining.get());
	    EmployeeTraining training = optionalTraining.get();

	    // Force loading of employees
	    training.getEmployees().size();

	    return ResponseEntity.ok(training);
	}

	

//	@GetMapping("/batchcode/{code}")
//	public ResponseEntity<String> getTrainingByBatchCode2(@PathVariable("code") String batchCode) {
//	    return trainingService.getTrainingWithEmployees(batchCode)
//	            .map(training -> ResponseEntity.ok("Training found: " + training.toString()))  // or any other string representation
//	            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
//	                    .body("No training found with batch code: " + batchCode));
//	}

    @GetMapping("/batchcodes/{code}")
    public ResponseEntity<?> getTrainingByBatchCode2(@PathVariable("code") String batchCode) {
    	 Optional<EmployeeTrainingDTO> trainingDTO = trainingService.getTrainingWithEmployees2(batchCode);
         
         // Check if training data is present
         if (trainingDTO.isPresent()) {
             // If found, return the training data with a 200 OK status
             return new ResponseEntity<>(trainingDTO.get(), HttpStatus.OK);
         } else {
             // If not found, return a 404 Not Found status with a message
             return new ResponseEntity<>("No training found with batch code: " + batchCode, HttpStatus.NOT_FOUND);
         }
     }

}