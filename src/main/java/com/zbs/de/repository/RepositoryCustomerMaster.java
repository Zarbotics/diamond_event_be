package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CustomerMaster;

@Repository("repositoryCutomerMaster")
public interface RepositoryCustomerMaster extends JpaRepository<CustomerMaster, Integer> {

	/**
	 * Find by is deleted.
	 *
	 * @param b the b
	 * @return the list
	 */
	List<CustomerMaster> findByBlnIsDeleted(Boolean blnIsDeleted);

	@Query("SELECT MAX(c.txtCustCode) FROM CustomerMaster c")
	String findMaxCustomerCode();

	@Query("SELECT c FROM CustomerMaster c WHERE LOWER(c.txtEmail) = LOWER(:txtEmail) AND c.blnIsDeleted = false")
	List<CustomerMaster> findByTxtEmailIgnoreCaseAndBlnIsDeletedFalse(@Param("txtEmail") String txtEmail);

}
