package com.eos.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eos.admin.entity.TrainerMaster;
@Repository
public interface TrainerMasterRepository extends JpaRepository<TrainerMaster, Long> {
    List<TrainerMaster> findByTrainerId(String trainerId);

    void deleteByTrainerId(String trainerId);

    boolean existsByTrainerIdAndProcess(String trainerId, String process);
}
