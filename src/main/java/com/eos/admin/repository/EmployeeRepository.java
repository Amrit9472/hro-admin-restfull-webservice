package com.eos.admin.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eos.admin.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	boolean existsByEmail(String email);

	boolean existsByAadhaarNumber(String aadhaarNumber);


//	@Query("SELECT e.id, e.fullName, e.email, e.jobProfile, e.mobileNo, e.permanentAddress, e.gender, e.creationDate "
//			+ "FROM Employee e INNER JOIN EmployeeStatusDetails esd ON e.id = esd.employee.id "
//			+ "WHERE esd.initialStatus = 'Active' AND esd.hrStatus IS NULL AND e.appliedlocation = :location")
	@Query(value = "SELECT e.id, e.full_Name, e.email, e.job_Profile, e.mobile_No, e.permanent_Address, e.gender, e.creation_Date, "
	        + "GROUP_CONCAT(CONCAT(l.language_Name, ' (Read: ', "
	        + "IF(l.can_Read = 1, 'Yes', 'No'), ', Write: ', "
	        + "IF(l.can_Write = 1, 'Yes', 'No'), ')') "
	        + "SEPARATOR ', ') AS languages "
	        + "FROM Employees e "
	        + "INNER JOIN Employee_Status_Details esd ON e.id = esd.employee_id "
	        + "LEFT JOIN Languages l ON e.id = l.employee_id "
	        + "WHERE esd.initial_Status = 'Active' "
	        + "AND esd.hr_Status IS NULL "
	        + "AND e.applied_Location = :location "
	        + "AND e.applied_branch = :branch "
	        + "GROUP BY e.id, e.full_Name, e.email, e.job_Profile, e.mobile_No, "
	        + "e.permanent_Address, e.gender, e.creation_Date", nativeQuery = true)
	List<Object[]> getListOfEmployeeOnProfileScreening(@Param("location") String location ,@Param("branch") String branch);

	@Query("SELECT e.id, e.fullName, e.aadhaarNumber, e.email, e.creationDate, sh.status, sh.changesDateTime ,sh.hrName,sh.remarksOnEveryStages "
			+ "FROM Employee e " + "JOIN StatusHistory sh ON e.id = sh.employee.id " + "WHERE e.id = :id")
	List<Object[]> getListOfStatusHistoryRecordsOfEmployee(@Param("id") Long employeeId);

	@Query("SELECT e.id, e.fullName, e.email, e.gender, e.mobileNo, e.creationDate "
			+ "FROM Employee e JOIN EmployeeStatusDetails esd ON e.id = esd.employee.id "
			+ "WHERE esd.hrStatus = 'Select' AND esd.processesStatus IS NULL AND e.appliedlocation = :location AND e.appliedBranch = :branch ")
	List<Object[]> getListOfEmployeeSchedulePage(@Param("location") String location , @Param("branch") String branch );

	@Query("SELECT e.id , e.fullName,e.email, e.jobProfile,creationDate  "
			+ "FROM Employee e JOIN EmployeeStatusDetails esd ON e.id = esd.employee.id "
			+ "WHERE esd.processesStatus = 'Reject' AND e.appliedlocation = :location")
	List<Object[]> getListOfEmployeeRejectByProcessManager(@Param("location") String location);

	@Query(value = "SELECT e.id,e.fullName,e.email,e.mobileNo,e.gender,e.creationDate,e.jobProfile,esd.profileScreenRemarks,esd.remarksByHr,esd.remarksByManager,esd.grade,esd.companyType,esd.department,esd.lastInterviewAssign "
			+ "FROM Employee e " + "JOIN EmployeeStatusDetails esd ON e.id = esd.employee.id "
			+ "WHERE esd.managerStatus = 'Select' AND e.appliedlocation = :location "
			+ "AND e. appliedBranch = :branch "
			+"AND NOT EXISTS (SELECT 1 FROM OurEmployees oe WHERE oe.employee.id = e.id)")
	List<Object[]> getSelectedEmployeeDetails(@Param("location") String location,@Param("branch") String branch);

	@Query("SELECT e.id, e.fullName, e.email, e.gender, e.mobileNo, e.creationDate , esd.profileScreenRemarks "
			+ "FROM Employee e JOIN EmployeeStatusDetails esd ON e.id = esd.employee.id "
			+ "WHERE esd.hrStatus = 'Reject' AND e.appliedlocation = :location "
			+ " AND e.appliedBranch = :branch ")
	List<Object[]> getListOfProfileScreaningPage(@Param("location") String location ,@Param("branch") String branch);

//	@Query("SELECT e.id ,e.fullName, e.email , esd.initialStatus ,esd.creationDate ,esd.hrStatus ,esd.managerStatus "
//			+" esd.managerStatus, esd.lastInterviewAssign,esd.remarksByHr,esd.remarksByManager,esd.profileScreenRemarks "
//			+" FROM Employee e JOIN EmployeeStatusDetails esd ON e.id = esd.employee.id ")
//	List<Object[]> getRegisterEmployeeInformation();

	@Query("SELECT e.id, e.fullName, e.email, " 
	        + "esd.initialStatus, e.creationDate, esd.hrStatus, esd.managerStatus, "
			+ "esd.lastInterviewAssign, esd.remarksByHr, esd.remarksByManager, esd.profileScreenRemarks "
			+ "FROM Employee e JOIN EmployeeStatusDetails esd ON e.id = esd.employee.id")
	List<Object[]> getRegisterEmployeeInformation();

	@Query(value = "SELECT e.id, e.full_Name, e.email, e.job_Profile,e.qualification,e.mobile_no, e.permanent_address,e.current_address, e.gender, e.creation_Date, " 
	        +"e.previous_Organisation,e.dob, e.marital_Status, e.refferal,e.aadhaar_Number,e.languages, e.experience,e.source,e.sub_Source, "
	        +"esd.initial_status, esd.processes_status, esd.hr_status, esd.manager_status,esd.last_interview_assign, "
	        +"esd.profile_screen_remarks, esd.remarks_by_hr,esd.remarks_by_manager,esd.client_round, "
	        +"sh.status,sh.hr_name,sh.remarks_on_every_stages,sh.changes_date_time "
	        +"FROM employees e " 
	        +"INNER JOIN employee_status_details esd ON e.id = esd.employee_id "
	        +"INNER JOIN status_history sh ON e.id = sh.employee_id",
	       nativeQuery = true)
	List<Object[]> getEmployeeDumpData();

	@Query(value = "SELECT s.employee_id, ed.full_name, ed.qualification,ed.aadhaar_number,ed.creation_date, ed.current_address, ed.dob, "
	        + "ed.email, ed.experience, ed.gender, ed.marital_status, ed.mobile_no, ed.permanent_address, ed.previous_organisation, "
			+ "ed.processes_status, ed.refferal,ed.source, ed.sub_source, ed.work_exp, ed.languages,ed.job_profile, esd.initial_status, " 
	        + "esd.hr_status, esd.manager_status, esd.last_interview_assign, esd.remarks_by_hr, esd.remarks_by_manager, esd.profile_screen_remarks, " 
	        +"GROUP_CONCAT(IFNULL(s.hr_name, '') ORDER BY s.changes_date_time SEPARATOR ' | ') AS hr_names, " 
	        +"GROUP_CONCAT(IFNULL(s.changes_date_time, '') ORDER BY s.changes_date_time SEPARATOR ' | ') AS changes_date_times, "
	        +"GROUP_CONCAT(IFNULL(s.remarks_on_every_stages, '') ORDER BY s.changes_date_time SEPARATOR ' | ') AS remarks, " 
	        +"GROUP_CONCAT(IFNULL(s.status, '') ORDER BY s.changes_date_time SEPARATOR ' | ') AS status "
	        +"FROM status_history s "
	        +"JOIN employees ed ON s.employee_id = ed.id "
	        +"JOIN Employee_Status_Details esd ON ed.id = esd.employee_id "
	        +"WHERE ed.creation_date BETWEEN :startDate AND :endDate "
	        +"GROUP BY s.employee_id, ed.full_name",
	       nativeQuery = true)
	List<Object[]> getEmployeeSeqDumpData(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
	

	@Query("SELECT e.id, e.fullName, e.email, e.jobProfile, e.mobileNo, e.creationDate FROM Employee e JOIN e.employeeStatusDetails esd WHERE esd.processesStatus = :uniqueCodeProcess")
	List<Object[]> getScheduleInterviewManagerPage(@Param("uniqueCodeProcess") String uniqueCodeProcess);

	@Query(
		    value = "SELECT e.id, e.full_name, e.email, e.job_profile, e.mobile_no, " +
		            "e.gender, eps.processes_status, e.creation_date " +
		            "FROM employees e " +
		            "JOIN employee_status_details eps ON eps.employee_id = e.id " +
		            "WHERE eps.processes_status = :role AND e.applied_location = :location AND e.applied_branch = :branch ",
		    nativeQuery = true
		)
		List<Object[]> findEmployeesByRoleAndLocation(@Param("role") String role, @Param("location") String location, @Param("branch") String branch);
		@Query(
			    value = "SELECT vi.name " +
			            "FROM candidates_entity ce " +
			            "JOIN vendors vi ON ce.vendor_email = vi.email " +
			            "WHERE ce.candi_email = :candiEmail",
			    nativeQuery = true
			)
	String getVendorNameByEmployeeEmail(@Param("candiEmail") String candiEmail);
		

//	@Query("SELECT e FROM Employee e WHERE e.trainingBatch.id = :batchId")
//	List<Employee> findByTrainingBatchId(@Param("batchId") Long batchId);

	@Query(value = "SELECT * FROM employees WHERE id IN (SELECT employee_id FROM training_batch_candidates WHERE training_batch_id = :batchId)", nativeQuery = true)
	List<Employee> findAllByBatchId(@Param("batchId") Long batchId);

}
