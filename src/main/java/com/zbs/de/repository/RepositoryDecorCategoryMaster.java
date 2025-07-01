package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorCategoryMaster;

@Repository("repositoryDecorCategoryMaster")
public interface RepositoryDecorCategoryMaster extends JpaRepository<DecorCategoryMaster, Integer> {

}
