package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoMenuComponent;

public interface ServiceMenuComponent {
	DtoMenuComponent create(DtoMenuComponent dto);

	DtoMenuComponent update(Long id, DtoMenuComponent dto);

	void delete(Long id);

	List<DtoMenuComponent> findByParent(Long parentMenuItemId);
}
