package com.zbs.de.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.model.EventDecorCategorySelection;
import com.zbs.de.model.dto.DtoEventDecorCategorySelection;
import com.zbs.de.model.dto.DtoEventDecorPropertySelection;
import com.zbs.de.model.dto.DtoEventDecorReferenceDocument;
import com.zbs.de.repository.RepositoryEventDecorCategorySelection;
import com.zbs.de.service.ServiceEventDecorCategorySelection;

@Service("serviceEventDecorCategorySelection")
public class ServiceEventDecorCategorySelectionImpl implements ServiceEventDecorCategorySelection {

	@Autowired
	RepositoryEventDecorCategorySelection repositoryEventDecorCategorySelection;

	@Override
	public void deleteByEventMasterId(Integer serEventMasterId) {
		List<EventDecorCategorySelection> selections = repositoryEventDecorCategorySelection
				.findByEventMaster_SerEventMasterId(serEventMasterId);

		for (EventDecorCategorySelection selection : selections) {
			selection.setBlnIsDeleted(true);
			repositoryEventDecorCategorySelection.save(selection);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<DtoEventDecorCategorySelection> getSelectionsWithChosenValues(Integer eventMasterId) {

		List<EventDecorCategorySelection> entities = repositoryEventDecorCategorySelection
				.findByEventMasterWithPropertiesWithOutJoin(eventMasterId);

//		List<EventDecorCategorySelection> entitiesa = repositoryEventDecorCategorySelection
//				.findByEventMasterWithProperties(eventMasterId);

		// Ensure documents are initialized (Hibernate will run batch queries instead of
		// N+1 if @BatchSize is used)
//		entities.forEach(edcs -> edcs.getUserUploadedDocuments().size());

		return entities.stream().map(edcs -> {
			DtoEventDecorCategorySelection dto = new DtoEventDecorCategorySelection();
			dto.setSerEventDecorCategorySelectionId(edcs.getSerEventDecorCategorySelectionId());
			dto.setTxtEventDecorCategorySelectionCode(edcs.getTxtEventDecorCategorySelectionCode());
			dto.setTxtRemarks(edcs.getTxtRemarks());
			dto.setNumPrice(edcs.getNumPrice());
			if (edcs.getDecorCategory() != null) {
				dto.setSerDecorCategoryId(edcs.getDecorCategory().getSerDecorCategoryId());
				dto.setTxtDecorCategoryCode(edcs.getDecorCategory().getTxtDecorCategoryCode());
				dto.setTxtDecorCategoryName(edcs.getDecorCategory().getTxtDecorCategoryName());
				dto.setNumPrice(edcs.getDecorCategory().getNumPrice());
			}

			// Map selected properties
			if (edcs.getSelectedProperties() != null) {
				dto.setSelectedProperties(edcs.getSelectedProperties().stream().map(prop -> {
					DtoEventDecorPropertySelection propDto = new DtoEventDecorPropertySelection();
					propDto.setSerEventDecorPropertyId(prop.getSerEventDecorPropertyId());
					propDto.setNumPrice(prop.getNumPrice());

					if (prop.getProperty() != null) {
						propDto.setSerPropertyId(prop.getProperty().getSerPropertyId());
						propDto.setTxtPropertyCode(prop.getProperty().getTxtPropertyCode());
						propDto.setTxtPropertyName(prop.getProperty().getTxtPropertyName());
					}

					if (prop.getSelectedValue() != null) {
						propDto.setSerPropertyValueId(prop.getSelectedValue().getSerPropertyValueId());
						propDto.setTxtPropertyValue(prop.getSelectedValue().getTxtPropertyValue());
					}

					return propDto;
				}).toList());
			}

			// Map user uploaded documents
			if (edcs.getUserUploadedDocuments() != null) {
				dto.setUserUploadedDocuments(edcs.getUserUploadedDocuments().stream().map(doc -> {
					DtoEventDecorReferenceDocument docDto = new DtoEventDecorReferenceDocument();
					docDto.setDocumentId(doc.getDocumentId());
					docDto.setDocumentName(doc.getDocumentName());
					docDto.setDocumentType(doc.getDocumentType());
					docDto.setOriginalName(doc.getOriginalName());
					docDto.setSize(doc.getSize());
					docDto.setTxtDocumentUrl(doc.getFilePath());
					docDto.setSerEventDecorCategorySelectionId(edcs.getSerEventDecorCategorySelectionId());
					docDto.setTxtEventDecorCategorySelectionCode(edcs.getTxtEventDecorCategorySelectionCode());
					docDto.setDocumentFile(null); // skip loading bytes
					return docDto;
				}).toList());
			}

			return dto;
		}).toList();
	}

}
