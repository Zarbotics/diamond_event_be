package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventDecorReferenceDocument;

@Repository("repositoryEventDecorReferenceDocument")
public interface RepositoryEventDecorReferenceDocument extends JpaRepository<EventDecorReferenceDocument, Integer> {

}
