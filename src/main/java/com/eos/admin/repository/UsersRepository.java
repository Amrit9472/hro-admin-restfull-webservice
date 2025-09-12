package com.eos.admin.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eos.admin.entity.OurUsers;

@Repository
public interface UsersRepository extends JpaRepository<OurUsers,Integer> {

	Optional<OurUsers> findByEmail(String email);

	@Query("SELECT DISTINCT u.uniqueCode FROM OurUsers u WHERE u.uniqueCode IS NOT NULL AND u.uniqueCode <> 'Admin'")
	List<String> findDistinctProcesses();
	
	@Query("SELECT u.processCode FROM OurUsers u WHERE u.process = :process")
	String findProcessCodeByProcess(@Param("process") String process);

	List<OurUsers> findByProcess(String processName);

	List<OurUsers> findByUniqueCode(String processName);
	
	@Query("SELECT u.name FROM OurUsers u WHERE u.email = :email")
    Optional<String> findNameByEmail(@Param("email") String email);
}
