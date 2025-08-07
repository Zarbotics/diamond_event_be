package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorExtrasOptionDocument;

@Repository("repositoryDecorExtrasOptionDocument")
public interface RepositoryDecorExtrasOptionDocument extends JpaRepository<DecorExtrasOptionDocument, Integer> {

}
