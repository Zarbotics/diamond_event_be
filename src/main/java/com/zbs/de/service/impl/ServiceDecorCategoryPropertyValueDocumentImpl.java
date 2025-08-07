package com.zbs.de.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.DecorCategoryPropertyValueDocument;
import com.zbs.de.repository.RepositoryDecorCategoryPropertyValueDocument;
import com.zbs.de.service.ServiceDecorCategoryPropertyValueDocument;

@Service("serviceDecorCategoryPropertyValueDocumentImpl")
public class ServiceDecorCategoryPropertyValueDocumentImpl implements ServiceDecorCategoryPropertyValueDocument {

	@Autowired
	RepositoryDecorCategoryPropertyValueDocument repositoryDecorCategoryPropertyValueDocument;

	@Override
	public DecorCategoryPropertyValueDocument save(DecorCategoryPropertyValueDocument document) {
		if (document != null) {
			return repositoryDecorCategoryPropertyValueDocument.save(document);
		}
		return null;
	}

}
