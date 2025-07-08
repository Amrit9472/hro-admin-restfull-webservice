package com.eos.admin.service;

import java.util.Optional;

import com.eos.admin.dto.EmployeeTrainingCreateRequest;
import com.eos.admin.dto.EmployeeTrainingDTO;
import com.eos.admin.entity.EmployeeTraining;

public interface TrainingService {

	EmployeeTraining createTraining(EmployeeTrainingCreateRequest request, String location);

	Optional<EmployeeTrainingDTO> getTrainingWithEmployees(String batchCode);

	Optional<EmployeeTrainingDTO> getTrainingWithEmployees2(String batchCode);

}
