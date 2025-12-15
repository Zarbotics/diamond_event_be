package com.zbs.de.repository;

import com.zbs.de.model.MenuComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositoryMenuComponent extends JpaRepository<MenuComponent, Long> {
	List<MenuComponent> findByParentMenuItem_SerMenuItemId(Long parentMenuItemId);
	
    List<MenuComponent> findByChildMenuItem_SerMenuItemId(Long childMenuItemId);
}
