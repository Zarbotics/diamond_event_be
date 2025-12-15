package com.zbs.de.repository;

import com.zbs.de.model.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositoryItineraryItem extends JpaRepository<ItineraryItem, Long> {
}
