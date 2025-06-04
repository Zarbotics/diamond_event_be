package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.VendorMaster;

@Repository("repositoryVendorMaster")
public interface RepositoryVendorMaster extends JpaRepository<VendorMaster, Integer> {
	List<VendorMaster> findByBlnIsDeleted(Boolean blnIsDeleted);
}