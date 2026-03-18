package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.EventVendorMasterSelection;

public interface ServiceEventVendorMasterSelection {

	List<EventVendorMasterSelection> getByEventMasterId(Integer serEventMasterId);

}
