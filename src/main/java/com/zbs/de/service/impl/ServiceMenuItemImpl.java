package com.zbs.de.service.impl;

import com.zbs.de.mapper.MapperMenuItem;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.service.ServiceMenuItem;
import com.zbs.de.service.ServiceTreeUtility;
import com.zbs.de.util.exception.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("serviceMenuItemImpl")
public class ServiceMenuItemImpl implements ServiceMenuItem {

	@Autowired
	private RepositoryMenuItem repo;

	@Autowired
	private ServiceTreeUtility treeUtil;

	@Override
	@Transactional
	public DtoMenuItem create(DtoMenuItem dto) {
		MenuItem entity = MapperMenuItem.toEntity(dto);
		// parent handling
		if (dto.getParentId() != null) {
			MenuItem parent = repo.findById(dto.getParentId())
					.orElseThrow(() -> new NotFoundException("Parent not found"));
			entity.setParent(parent);
			entity.setTxtPath(treeUtil.computeChildPath(parent.getTxtPath(), entity.getTxtCode()));
		} else {
			entity.setTxtPath(treeUtil.sanitizeForLtree(entity.getTxtCode()));
		}
		if (entity.getBlnIsSelectable() == null)
			entity.setBlnIsSelectable(true);
		repo.save(entity);
		return MapperMenuItem.toDto(entity);
	}

	@Override
	@Transactional
	public DtoMenuItem update(Long id, DtoMenuItem dto) {
		MenuItem exist = repo.findById(id).orElseThrow(() -> new NotFoundException("MenuItem not found"));
		exist.setTxtCode(dto.getTxtCode());
		exist.setTxtName(dto.getTxtName());
		exist.setTxtShortName(dto.getTxtShortName());
		exist.setTxtDescription(dto.getTxtDescription());
		exist.setTxtRole(dto.getTxtRole());
		exist.setNumDisplayOrder(dto.getNumDisplayOrder());
		exist.setBlnIsSelectable(dto.getBlnIsSelectable());
		exist.setMetadata(dto.getMetadata());
		exist.setNumDefaultServingsPerGuest(dto.getNumDefaultServingsPerGuest());

		Long newParentId = dto.getParentId();
		Long oldParentId = exist.getParent() == null ? null : exist.getParent().getSerMenuItemId();
		if ((newParentId == null && oldParentId != null) || (newParentId != null && !newParentId.equals(oldParentId))) {
			MenuItem newParent = null;
			if (newParentId != null)
				newParent = repo.findById(newParentId).orElseThrow(() -> new NotFoundException("Parent not found"));
			exist.setParent(newParent);
			String newPath = newParent == null ? treeUtil.sanitizeForLtree(exist.getTxtCode())
					: treeUtil.computeChildPath(newParent.getTxtPath(), exist.getTxtCode());
			treeUtil.updatePathForSubtree(exist, newPath);
		} else {
			// if code changed, update path for subtree as well
			if (!exist.getTxtCode().equals(dto.getTxtCode())) {
				String basePath = exist.getParent() == null ? treeUtil.sanitizeForLtree(dto.getTxtCode())
						: treeUtil.computeChildPath(exist.getParent().getTxtPath(), dto.getTxtCode());
				treeUtil.updatePathForSubtree(exist, basePath);
			}
		}
		repo.save(exist);
		return MapperMenuItem.toDto(exist);
	}

	@Override
	public void delete(Long id) {
		MenuItem exist = repo.findById(id).orElseThrow(() -> new NotFoundException("MenuItem not found"));
		exist.setBlnIsDeleted(true);
		repo.save(exist);
	}

	@Override
	public DtoMenuItem getById(Long id) {
		MenuItem e = repo.findById(id).orElseThrow(() -> new NotFoundException("MenuItem not found"));
		return MapperMenuItem.toDto(e);
	}

	@Override
	public List<DtoMenuItem> getTree() {
		List<MenuItem> all = repo.findAll();
		return treeUtil.buildTreeDto(all);
	}

	@Override
	public List<DtoMenuItem> getChildren(Long parentId) {
		List<MenuItem> children = repo.findByParentId(parentId);
		return children.stream().map(MapperMenuItem::toDto).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void move(Long id, Long newParentId) {
		MenuItem node = repo.findById(id).orElseThrow(() -> new NotFoundException("MenuItem not found"));
		MenuItem newParent = newParentId == null ? null
				: repo.findById(newParentId).orElseThrow(() -> new NotFoundException("Parent not found"));
		node.setParent(newParent);
		String newPath = newParent == null ? treeUtil.sanitizeForLtree(node.getTxtCode())
				: treeUtil.computeChildPath(newParent.getTxtPath(), node.getTxtCode());
		treeUtil.updatePathForSubtree(node, newPath);
		repo.save(node);
	}
}
