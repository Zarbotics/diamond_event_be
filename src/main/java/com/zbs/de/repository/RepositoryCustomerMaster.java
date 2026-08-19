package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.dto.DtoCustomerMasterDropDown;

@Repository("repositoryCutomerMaster")
public interface RepositoryCustomerMaster extends JpaRepository<CustomerMaster, Integer> {
	List<CustomerMaster> findByBlnIsDeleted(Boolean blnIsDeleted);

	/**
	 * One page of customers, newest first, optionally narrowed by a search term.
	 *
	 * <p>
	 * The unpaged {@code findByBlnIsDeleted} above returns every customer the
	 * business has ever had, and the admin's customer table called it to show ten
	 * rows. That works at fifty customers and stops working long before the
	 * business would like to have the problem.
	 *
	 * <p>
	 * The term is matched against the things somebody actually searches a
	 * customer list by — name, code, email, phone.
	 */
	@Query("""
			SELECT c FROM CustomerMaster c
			WHERE c.blnIsDeleted = false
			  AND (:term IS NULL OR :term = ''
			       OR LOWER(c.txtCustName)   LIKE LOWER(CONCAT('%', :term, '%'))
			       OR LOWER(c.txtCustCode)   LIKE LOWER(CONCAT('%', :term, '%'))
			       OR LOWER(c.txtEmail)      LIKE LOWER(CONCAT('%', :term, '%'))
			       OR LOWER(c.txt_phone_number_1) LIKE LOWER(CONCAT('%', :term, '%')))
			""")
	Page<CustomerMaster> search(@Param("term") String term, Pageable pageable);

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
