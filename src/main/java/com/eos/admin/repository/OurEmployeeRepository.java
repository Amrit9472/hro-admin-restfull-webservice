package com.eos.admin.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eos.admin.entity.OurEmployees;
@Repository
public interface OurEmployeeRepository extends JpaRepository<OurEmployees, Long> {

	@Query(value = "SELECT * FROM our_employee oe JOIN employees e ON oe.employee_id = e.id WHERE e.applied_location = :location ", nativeQuery = true)
	List<OurEmployees> getAllOurEmployeesByLocation(@Param("location") String location);
	
	@Query("SELECT DISTINCT o.process FROM OurEmployees o")
	List<String> findDistinctProcesses();

	List<OurEmployees> findByProcess(String process);
	
	@Query(value = "SELECT e.id AS employeeId, oe.process, e.full_name AS fullName, ia.submission_date AS inductionCompleteDate, ia.type " +
	        "FROM our_employee oe " +
	        "JOIN employees e ON oe.employee_id = e.id " +
	        "LEFT JOIN induction_attendance ia ON e.id = ia.employee_id " +
	        "WHERE oe.process = :process AND ia.type = 'INDUCTION'", 
	        nativeQuery = true)
	List<Object[]> findEmployeeDetailsByProcess(@Param("process") String process);
	
	@Query(value = "SELECT * FROM our_employee WHERE process = :process AND joining_date = :joiningDate", nativeQuery = true)
	List<OurEmployees> findByDateAndProcess(@Param("process") String process, @Param("joiningDate") Date joiningDate);

}
