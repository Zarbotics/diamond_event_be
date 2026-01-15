package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.EventDecorExtrasSelection;

public interface ServiceEventDecorExtrasSelection {
	
	String deleteByEventMasterId(Integer id);
	EventDecorExtrasSelection save(EventDecorExtrasSelection eventDecorExtrasSelection);
	List<EventDecorExtrasSelection> getByEventMasterId(Integer serEventMasterId);
	Boolean existsByDecorExtrasOptionId(Integer id);

}
