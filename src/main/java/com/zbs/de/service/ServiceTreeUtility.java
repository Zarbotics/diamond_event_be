package com.zbs.de.service;

import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoMenuItem;

import java.util.List;

public interface ServiceTreeUtility {
	String computeChildPath(String parentPath, String childCode);

	void updatePathForSubtree(MenuItem node, String newPath);

	List<DtoMenuItem> buildTreeDto(List<MenuItem> flat);

	String sanitizeForLtree(String s);
}
