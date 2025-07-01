package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorCategoryColorMapping;

@Repository("repositoryDecorCategoryColorMapping")
public interface RepositoryDecorCategoryColorMapping extends JpaRepository<DecorCategoryColorMapping, Integer> {

	List<DecorCategoryColorMapping> findByDecorCategory_SerDecorCategoryIdAndBlnIsDeletedFalse(
			Integer serDecorCategoryId);

}
