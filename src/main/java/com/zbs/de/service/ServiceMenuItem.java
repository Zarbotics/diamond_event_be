package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoMenuItem;

public interface ServiceMenuItem {
	DtoMenuItem create(DtoMenuItem dto);

	DtoMenuItem update(Long id, DtoMenuItem dto);

	void delete(Long id);

	DtoMenuItem getById(Long id);

	List<DtoMenuItem> getTree();

	List<DtoMenuItem> getChildren(Long parentId);

	void move(Long id, Long newParentId);
}
