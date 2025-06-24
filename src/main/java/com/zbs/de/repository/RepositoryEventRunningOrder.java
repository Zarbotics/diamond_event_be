package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventRunningOrder;

@Repository("repositoryEventRunningOrder")
public interface RepositoryEventRunningOrder extends JpaRepository<EventRunningOrder, Integer> {

}
