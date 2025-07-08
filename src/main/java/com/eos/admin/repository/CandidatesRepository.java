package com.eos.admin.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eos.admin.entity.CandidatesEntity;

@Repository
public interface CandidatesRepository extends JpaRepository<CandidatesEntity, Long> {
    
    List<CandidatesEntity> findBySubmittedDateBetweenAndVendorEmail(LocalDateTime start, LocalDateTime end, String vendorEmail);
    
    List<CandidatesEntity> findByScheme(String scheme);
    
    Optional<CandidatesEntity> findByCandiEmailAndScheme(String candiEmail, String scheme);

    @Query(value = "SELECT esd.manager_status " +
            "FROM employees e " +
            "JOIN employee_status_details esd ON e.id = esd.employee_id " +
            "WHERE e.email = :email", nativeQuery = true)
    Optional<String> findManagerStatusByEmail(@Param("email") String email);
    
    List<CandidatesEntity> findByVenIdAndVendorEmailOrderBySubmittedDateDesc(String venId, String vendorEmail);

	List<CandidatesEntity> findAllByVendorEmailOrderBySubmittedDateDesc(String vendorEmail);
	
	@Query(value = "SELECT CASE " +
            "WHEN es.profile_screen_remarks IS NOT NULL AND es.profile_screen_remarks <> '' " +
            "THEN true ELSE false END AS has_screening_remarks " +
            "FROM employees e " +
            "JOIN employee_status_details es ON e.id = es.employee_id " +
            "WHERE e.email = :email", nativeQuery = true)
	Long existsScreeningRemarksByEmail(@Param("email") String email);


}
