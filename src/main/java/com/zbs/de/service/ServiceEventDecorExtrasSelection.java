package com.zbs.de.service;

import com.zbs.de.model.EventDecorExtrasSelection;

public interface ServiceEventDecorExtrasSelection {
	
	String deleteByEventMasterId(Integer id);
	EventDecorExtrasSelection save(EventDecorExtrasSelection eventDecorExtrasSelection);

}
