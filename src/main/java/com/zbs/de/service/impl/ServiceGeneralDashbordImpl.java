package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.dto.DtoGenralDashboard;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.service.ServiceGeneralDashbord;

@Service("serviceGeneralDashbord")
public class ServiceGeneralDashbordImpl implements ServiceGeneralDashbord {

	@Autowired
	private RepositoryCustomerMaster repositoryCustomerMaster;
	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	public List<DtoGenralDashboard> getYearlyCustomerEventReport(int year) {
		List<Object[]> customerData = repositoryCustomerMaster.getMonthlyCustomerCounts(year);
		List<Object[]> eventData = repositoryEventMaster.getMonthlyEventCounts(year);

		// Map month -> count
		Map<Integer, Long> customerMap = customerData.stream()
				.collect(Collectors.toMap(row -> ((Number) row[0]).intValue(), row -> ((Number) row[1]).longValue()));

		Map<Integer, Long> eventMap = eventData.stream()
				.collect(Collectors.toMap(row -> ((Number) row[0]).intValue(), row -> ((Number) row[1]).longValue()));

		// Fill report for all 12 months
		List<DtoGenralDashboard> reportList = new ArrayList<>();
		for (int month = 1; month <= 12; month++) {
			DtoGenralDashboard dto = new DtoGenralDashboard();
			dto.setNumMonth(month);
			dto.setNumCustomerCount(customerMap.getOrDefault(month, 0L));
			dto.setNumEventCount(eventMap.getOrDefault(month, 0L));
			reportList.add(dto);
		}

		return reportList;
	}
}
