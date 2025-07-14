package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.VenueMaster;
import com.zbs.de.model.VenueMasterDetail;

@Repository("repositoryVenueMasterDetail")
public interface RepositoryVenueMasterDetail extends JpaRepository<VenueMasterDetail, Integer> {
	
	@Query("SELECT v.venueMaster FROM VenueMasterDetail v WHERE v.serVenueMasterDetailId = :detailId AND v.blnIsDeleted = false")
	VenueMaster findVenueMasterByDetailId(@Param("detailId") Integer detailId);
	
	@Query("SELECT v FROM VenueMasterDetail v WHERE v.serVenueMasterDetailId = :detailId AND v.blnIsDeleted = false AND v.blnIsActive = true")
	VenueMasterDetail findActiveVenueMasterDetailByDetailId(@Param("detailId") Integer detailId);

}
