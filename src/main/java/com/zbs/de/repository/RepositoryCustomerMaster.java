package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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
	
}
