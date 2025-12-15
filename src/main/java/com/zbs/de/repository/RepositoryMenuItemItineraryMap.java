package com.zbs.de.repository;

import com.zbs.de.model.MenuItemItineraryMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositoryMenuItemItineraryMap extends JpaRepository<MenuItemItineraryMap, Long> {
	List<MenuItemItineraryMap> findByMenuItem_SerMenuItemId(Long menuItemId);
}
