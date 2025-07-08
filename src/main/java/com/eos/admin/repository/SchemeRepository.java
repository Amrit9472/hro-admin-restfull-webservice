package com.eos.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eos.admin.entity.Schemes;

public interface SchemeRepository extends JpaRepository<Schemes, Long> {
	@Query(value = """
			SELECT s.*
			FROM schemes s
			WHERE
			    s.is_default = TRUE
			    OR (
			        :vendorId IS NOT NULL
			        AND s.end_date >= CURRENT_DATE
			        AND EXISTS (
			            SELECT 1
			            FROM vendor_schemes vs
			            WHERE vs.scheme_id = s.id
			              AND vs.vendor_id = :vendorId
			        )
			    )
			""", nativeQuery = true)
	List<Schemes> findAvailableSchemesForVendor(@Param("vendorId") Long vendorId);

	List<Schemes> findAllByOrderByIdDesc();

	// Filter by month and year from start_date
	@Query("SELECT s FROM Schemes s WHERE FUNCTION('MONTH', s.startDate) = :month AND FUNCTION('YEAR', s.startDate) = :year ORDER BY s.id DESC")
	List<Schemes> findByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);

	List<Schemes> findByIsDefaultTrue();

	@Query("SELECT s FROM Schemes s JOIN s.vendorInfoList v WHERE v.id = :vendorId")
	List<Schemes> findByVendorId(@Param("vendorId") Long vendorId);

	List<Schemes> findByIsDefault(boolean isDefault);

	Optional<Schemes> findByScheme(String schemeName);

	/**
	 * @Query(value = """ SELECT s.id AS scheme_id, s.scheme AS scheme_name,
	 *              GROUP_CONCAT(v.company_name ORDER BY v.company_name SEPARATOR ',
	 *              ') AS vendor_names FROM schemes s JOIN vendor_schemes vs ON s.id
	 *              = vs.scheme_id JOIN vendor_info v ON v.id = vs.vendor_id GROUP
	 *              BY s.id, s.scheme ORDER BY s.scheme """, nativeQuery = true)
	 *              List<Object[]> findSchemeVendorRaw();
	 */
	@Query(value = """
			SELECT
			    s.id AS scheme_id,
			    s.scheme AS scheme_name,
			    GROUP_CONCAT(v.company_name ORDER BY v.company_name SEPARATOR ', ') AS vendor_names
			FROM
			    schemes s
			LEFT JOIN
			    vendor_schemes vs ON s.id = vs.scheme_id
			LEFT JOIN
			    vendor_info v ON v.id = vs.vendor_id
			WHERE
			    s.is_default = false
			    AND s.type = 'Special'
			GROUP BY
			    s.id, s.scheme
			ORDER BY
			    s.scheme
			""", nativeQuery = true)
	List<Object[]> findSchemeVendorRaw();

	@Query("SELECT s.id FROM Schemes s WHERE s.scheme = :schemeName")
	Optional<Long> findIdBySchemeName(@Param("schemeName") String schemeName);

}
