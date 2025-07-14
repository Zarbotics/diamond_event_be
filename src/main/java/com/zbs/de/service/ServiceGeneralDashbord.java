package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoGenralDashboard;

public interface ServiceGeneralDashbord {
	List<DtoGenralDashboard> getYearlyCustomerEventReport(int year);
}
