package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CityMaster;
import com.zbs.de.model.VenueMaster;

@Repository("repositoryVenueMaster")
public interface RepositoryVenueMaster extends JpaRepository<VenueMaster, Integer> {

	List<VenueMaster> findByBlnIsDeletedFalse();
	
	Optional<VenueMaster> findBySerVenueMasterIdAndBlnIsDeletedFalse(Integer id);

	List<VenueMaster> findByCityMasterAndBlnIsDeletedFalse(CityMaster cityMaster);
	
	@Query("SELECT MAX(e.txtVenueCode) FROM VenueMaster e")
	String findMaxVenueMasterCode();
	
	@Query("SELECT v FROM VenueMaster v left join v.cityMaster c where v.blnIsDeleted = false and v.blnIsActive = true and c.blnIsDeleted = false and c.blnIsActive")
	List<VenueMaster> getAllActiveVenuesByActiveCities();
}
