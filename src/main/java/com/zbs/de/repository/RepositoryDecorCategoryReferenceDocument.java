package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorCategoryReferenceDocument;

@Repository("repositoryDecorCategoryReferenceDocument")
public interface RepositoryDecorCategoryReferenceDocument
		extends JpaRepository<DecorCategoryReferenceDocument, Integer> {
	List<DecorCategoryReferenceDocument> findAllByBlnIsDeletedFalse();

	Optional<DecorCategoryReferenceDocument> findByIdAndBlnIsDeletedFalse(Integer id);

	List<DecorCategoryReferenceDocument> findByDecorCategoryMasterSerDecorCategoryIdAndBlnIsDeletedFalse(
			Integer categoryId);

}
