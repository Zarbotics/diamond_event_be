package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorCategoryPropertyValueDocument;

@Repository("repositoryDecorCategoryPropertyValueDocument")
public interface RepositoryDecorCategoryPropertyValueDocument extends JpaRepository<DecorCategoryPropertyValueDocument, Integer> {

}
