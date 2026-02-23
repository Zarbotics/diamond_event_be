package com.zbs.de.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoMenuCsvImportResult;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.menu.DtoMenuItemSearch;

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
    
    List<DtoMenuItem> getAll();
    
    DtoResult getAllByRoleId(Integer id);
    
    DtoResult getAllActiveCompositeItems();
    
    DtoResult getAllActive();
    
    List<DtoMenuItem> getValidParentsByRoleID(Integer menuItemRoleId);
    
    List<DtoMenuItem> searchMenuItems(String query, String role, String type, Integer limit);
    
    List<MenuItem> getAllMenuItems();
    
    MenuItem getMenuItemById(Long id);
    
    DtoResult getAllNonCompositeActiveItemsByParentItemCode(String txtCode);
    String readMenuItemCsv(MultipartFile file);
    
    List<String> getAllPriceUnitTypes();
    Page<DtoMenuItem> searchMenuITems(DtoMenuItemSearch dto);
}
