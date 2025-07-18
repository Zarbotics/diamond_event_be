package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CityMaster;
import com.zbs.de.model.VenueMaster;

@Repository("repositoryVenueMaster")
public interface RepositoryVenueMaster extends JpaRepository<VenueMaster, Integer> {

	List<VenueMaster> findByBlnIsDeletedFalse();

	List<VenueMaster> findByCityMasterAndBlnIsDeletedFalse(CityMaster cityMaster);
}
