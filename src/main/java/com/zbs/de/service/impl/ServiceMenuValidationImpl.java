package com.zbs.de.service.impl;

import org.springframework.stereotype.Service;

import com.zbs.de.model.dto.DtoMenuComponent;
import com.zbs.de.service.ServiceMenuValidation;

@Service("serviceMenuValidationImpl")
public class ServiceMenuValidationImpl implements ServiceMenuValidation {
	public void validateComponentDto(DtoMenuComponent dto) {
		if ("SELECTION".equalsIgnoreCase(dto.getTxtComponentKind())) {
			if (dto.getNumSelectionMin() == null || dto.getNumSelectionMax() == null) {
				throw new IllegalArgumentException("SELECTION components must have selectionMin and selectionMax");
			}
			if (dto.getNumSelectionMin() > dto.getNumSelectionMax()) {
				throw new IllegalArgumentException("selectionMin cannot be greater than selectionMax");
			}
		}
		// add more rules as necessary
	}
}
