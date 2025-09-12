package com.eos.admin.service;

import com.eos.admin.dto.InductionAttendanceDTO;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface InductionAttendanceService {
    List<String> getAllDistinctProcesses();
    List<InductionAttendanceDTO> getEmployeesByDateAndProcess(Date date, String process);
//    List<InductionAttendanceDTO> saveAttendance(List<InductionAttendanceDTO> attendanceList);
    void saveAttendance(List<InductionAttendanceDTO> attendanceList);
    List<InductionAttendanceDTO> getEmployeesByBatch(Long batchId);
    List<Map<String, Object>> getAllTrainingBatches();


}
