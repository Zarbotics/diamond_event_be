package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorCategoryMaster;

@Repository("repositoryDecorCategoryMaster")
public interface RepositoryDecorCategoryMaster extends JpaRepository<DecorCategoryMaster, Integer>, JpaSpecificationExecutor<DecorCategoryMaster>{

	List<DecorCategoryMaster> findByBlnIsDeletedFalse();
	
    @Query("""
            SELECT c 
            FROM DecorCategoryMaster c
            WHERE c.blnIsDeleted = false
            ORDER BY 
                CASE WHEN c.numDisplayOrder IS NULL THEN 1 ELSE 0 END,
                c.numDisplayOrder ASC
            """)
     List<DecorCategoryMaster> findByBlnIsDeletedFalseOrderByDisplayOrder();
    

	Optional<DecorCategoryMaster> findBySerDecorCategoryIdAndBlnIsDeletedFalse(int serDecorCategoryId);

	@Query("SELECT cm FROM DecorCategoryMaster cm where cm.blnIsDeleted = false and cm.blnIsActive ")
	List<DecorCategoryMaster> getAllActive();
	
	@Query("SELECT MAX(e.txtDecorCategoryCode) FROM DecorCategoryMaster e")
	String findMaxDecorCategoryMasterCode();

}
