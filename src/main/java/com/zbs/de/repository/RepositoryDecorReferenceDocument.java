package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorReferenceDocument;

@Repository("repositoryDecorReferenceDocument")
public interface RepositoryDecorReferenceDocument extends JpaRepository<DecorReferenceDocument, Integer> {
	List<DecorReferenceDocument> findByDecorItemMaster_SerDecorItemId(Integer serDecorItemId);

}
