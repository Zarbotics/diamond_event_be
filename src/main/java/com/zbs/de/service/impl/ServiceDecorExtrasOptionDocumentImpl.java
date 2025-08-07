package com.zbs.de.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.DecorExtrasOptionDocument;
import com.zbs.de.repository.RepositoryDecorExtrasOptionDocument;
import com.zbs.de.service.ServiceDecorExtrasOptionDocument;

@Service("serviceDecorExtrasOptionDocument")
public class ServiceDecorExtrasOptionDocumentImpl implements ServiceDecorExtrasOptionDocument {

	@Autowired
	private RepositoryDecorExtrasOptionDocument repositoryDecorExtrasOptionDocument;
	
	@Override
	public DecorExtrasOptionDocument save(DecorExtrasOptionDocument doc) {

		if(doc != null) {
			return repositoryDecorExtrasOptionDocument.save(doc);
		}
		return null;
	}

}
