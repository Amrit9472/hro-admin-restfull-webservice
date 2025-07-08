package com.eos.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eos.admin.entity.InductionAttendance;
import com.eos.admin.enums.AttendanceType;

public interface InductionAttendanceRepository extends JpaRepository<InductionAttendance, Long> {

	List<InductionAttendance> findByOurEmployee_ProcessAndType(String process, AttendanceType induction);
}
