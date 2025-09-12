package com.eos.admin.repository;

import com.eos.admin.dto.TrainingBatchDTO;
import com.eos.admin.entity.TrainingBatch;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingBatchRepository extends JpaRepository<TrainingBatch, Long> {
	
    int countByBatchCodeStartingWith(String prefix);

    List<TrainingBatch> findByBatchCodeStartingWith(String prefix);
    
    @Query("SELECT DISTINCT new com.eos.admin.dto.TrainingBatchDTO(tb.id, tb.batchCode, tb.trainingStartDate, tb.process, li.trainingDays) " +
    	       "FROM TrainingBatch tb JOIN LoiInformationTableEntity li ON tb.process = li.process")
    	List<TrainingBatchDTO> findAllWithTrainingDays();

}
