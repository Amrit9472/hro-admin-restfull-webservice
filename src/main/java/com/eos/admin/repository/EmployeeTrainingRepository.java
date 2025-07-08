package com.eos.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eos.admin.entity.EmployeeTraining;

public interface EmployeeTrainingRepository extends JpaRepository<EmployeeTraining, Long> {

	Optional<EmployeeTraining> findByBatchCode(String batchCode);
}
