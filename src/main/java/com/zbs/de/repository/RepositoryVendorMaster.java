package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.VendorMaster;

@Repository("repositoryVendorMaster")
public interface RepositoryVendorMaster extends JpaRepository<VendorMaster, Integer> {
	List<VendorMaster> findByBlnIsDeleted(Boolean blnIsDeleted);
	
	@Query("SELECT v FROM VendorMaster v where v.blnIsActive = true and v.blnIsDeleted = false ")
	List<VendorMaster> findAllActive();
	
	@Query("SELECT MAX(e.txtVendorCode) FROM VendorMaster e")
	String findMaxVendorMasterCode();
}