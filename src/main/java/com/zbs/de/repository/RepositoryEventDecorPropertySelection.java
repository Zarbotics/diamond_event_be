package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventDecorPropertySelection;

@Repository("repositoryEventDecorPropertySelection")
public interface RepositoryEventDecorPropertySelection extends JpaRepository<EventDecorPropertySelection, Integer> {

}
