package com.zbs.de.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoMenuCsvImportResult;
import com.zbs.de.model.dto.DtoMenuItem;

public interface ServiceMenuItem {
	DtoMenuItem create(DtoMenuItem dto);

	DtoMenuItem update(Long id, DtoMenuItem dto);

	void delete(Long id);

	DtoMenuItem getById(Long id);

	List<DtoMenuItem> getTree();

	List<DtoMenuItem> getChildren(Long parentId);

	void move(Long id, Long newParentId);
	
    List<DtoMenuItem> findByRole(String role);
    
    List<DtoMenuItem> findByType(String type);
    
    List<DtoMenuItem> getBundleItems(Long bundleMenuItemId);
    
    List<DtoMenuItem> findDescendantsByPath(String ltreePath);
    
    List<DtoMenuItem> getSelectableItemsUnderParent(Long parentId);
    
    List<String> getTypes();
    
    List<String> getRoles();
    
    DtoMenuCsvImportResult importCsv(MultipartFile file);
    
    String generateNextCode(String prefix);
    
    List<DtoMenuItem> getValidParentsByRole(String role);
    
    List<MenuItem> getAll();
}
