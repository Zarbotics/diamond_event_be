package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorColorMaster;

@Repository("repositoryDecorColorMaster")
public interface RepositoryDecorColorMaster extends JpaRepository<DecorColorMaster, Integer> {

}
