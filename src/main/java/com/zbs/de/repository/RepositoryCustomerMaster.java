package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.dto.DtoCustomerMasterDropDown;

@Repository("repositoryCutomerMaster")
public interface RepositoryCustomerMaster extends JpaRepository<CustomerMaster, Integer> {
	List<CustomerMaster> findByBlnIsDeleted(Boolean blnIsDeleted);

	@Query("SELECT MAX(c.txtCustCode) FROM CustomerMaster c")
	String findMaxCustomerCode();
	
	
	@Query("SELECT new com.zbs.de.model.dto.DtoCustomerMasterDropDown(c.serCustId, c.txtCustCode, c.txtFirstName, c.txtLastName, c.txtCustName, c.txt_phone_number_1, c.txtEmail) FROM CustomerMaster c WHERE c.blnIsDeleted = false AND c.blnIsActive = true")
	List<DtoCustomerMasterDropDown> getAllActive();

	@Query("SELECT c FROM CustomerMaster c WHERE LOWER(c.txtEmail) = LOWER(:txtEmail) AND c.blnIsDeleted = false")
	List<CustomerMaster> findByTxtEmailIgnoreCaseAndBlnIsDeletedFalse(@Param("txtEmail") String txtEmail);

	@Query("SELECT COUNT(c) FROM CustomerMaster c WHERE c.blnIsDeleted = false")
	long countTotalCustomers();

	@Query(value = """
			    SELECT COUNT(*) FROM customer_master
			    WHERE bln_is_deleted = false
			    AND EXTRACT(MONTH FROM created_date) = EXTRACT(MONTH FROM CURRENT_DATE)
			    AND EXTRACT(YEAR FROM created_date) = EXTRACT(YEAR FROM CURRENT_DATE)
			""", nativeQuery = true)
	long countCustomersThisMonth();

//	@Query(value = """
//			    SELECT COUNT(*) FROM customer_master c
//			    WHERE c.bln_is_deleted = false
//			    AND EXTRACT(MONTH FROM c.created_date) = EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 month')
//			    AND EXTRACT(YEAR FROM c.created_date) = EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 month')
//			""", nativeQuery = true)
//	long countCustomersLastMonth();

	@Query(value = """
			    SELECT COUNT(*)
			    FROM customer_master c
			    WHERE c.bln_is_deleted = false
			    AND DATE_TRUNC('month', c.created_date) = DATE_TRUNC('month', CURRENT_DATE - INTERVAL '1 month')
			    """, nativeQuery = true)
	long countCustomersLastMonth();

	@Query(value = """
			SELECT
			    EXTRACT(MONTH FROM created_date) AS month,
			    COUNT(*) AS customer_count
			FROM customer_master
			WHERE EXTRACT(YEAR FROM created_date) = :year
			GROUP BY EXTRACT(MONTH FROM created_date)
			ORDER BY month
			""", nativeQuery = true)
	List<Object[]> getMonthlyCustomerCounts(@Param("year") int year);

}
