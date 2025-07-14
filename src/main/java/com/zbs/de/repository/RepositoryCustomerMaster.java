package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CustomerMaster;

@Repository("repositoryCutomerMaster")
public interface RepositoryCustomerMaster extends JpaRepository<CustomerMaster, Integer> {
	List<CustomerMaster> findByBlnIsDeleted(Boolean blnIsDeleted);

	@Query("SELECT MAX(c.txtCustCode) FROM CustomerMaster c")
	String findMaxCustomerCode();

	@Query("SELECT c FROM CustomerMaster c WHERE LOWER(c.txtEmail) = LOWER(:txtEmail) AND c.blnIsDeleted = false")
	List<CustomerMaster> findByTxtEmailIgnoreCaseAndBlnIsDeletedFalse(@Param("txtEmail") String txtEmail);

	@Query("SELECT COUNT(c) FROM CustomerMaster c WHERE c.blnIsDeleted = false")
	long countTotalCustomers();

	@Query("SELECT COUNT(c) FROM CustomerMaster c " + "WHERE c.blnIsDeleted = false "
			+ "AND FUNCTION('MONTH', c.createdDate) = FUNCTION('MONTH', CURRENT_DATE) "
			+ "AND FUNCTION('YEAR', c.createdDate) = FUNCTION('YEAR', CURRENT_DATE)")
	long countCustomersThisMonth();

	@Query(value = "SELECT COUNT(*) FROM customer_master c " + "WHERE c.bln_is_deleted = false "
			+ "AND MONTH(c.created_date) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) "
			+ "AND YEAR(c.created_date) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))", nativeQuery = true)
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
