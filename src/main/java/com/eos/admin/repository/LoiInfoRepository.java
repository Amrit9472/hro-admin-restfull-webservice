package com.eos.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eos.admin.entity.LoiInformationTableEntity;



@Repository
public interface LoiInfoRepository extends JpaRepository< LoiInformationTableEntity,Long> {

	@Query("SELECT e.grid FROM LoiInformationTableEntity e WHERE TRIM(e.process) = TRIM(:process) AND TRIM(e.grade) =  TRIM(:grade) AND  TRIM(e.companyType) =  TRIM(:companyType)")
	List<Object[]> findGridValuesByProcessGradeCompanyType(@Param("process") String process,@Param("grade") String grade,
			@Param("companyType") String companyType);

//	List<Object[]> findByProcessAndGradeAndCompanyType(String process, String grade, String companyType);
	
	@Query(value = "SELECT DISTINCT company_type AS companyType, process, grade FROM loi_info", nativeQuery = true)
	List<Object[]> processAndGradeAndCompanyTypeUnique();

	@Query(value = "SELECT * FROM loi_Info e WHERE e.grid = :gridNo",nativeQuery = true)
	LoiInformationTableEntity findDetailsWithGridNo(@Param("gridNo") String gridNo);



}	
