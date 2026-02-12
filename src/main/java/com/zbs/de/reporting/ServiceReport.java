package com.zbs.de.reporting;

import org.springframework.stereotype.Service;

@Service
public interface ServiceReport {
	byte[] generateEventReport(Integer eventId) throws Exception;
	
	 byte[] generateEventReportClientSide(Integer eventId) throws Exception;
	 
	 byte[] generateNewItineraryReport(Integer eventId) throws Exception;
	 
	 byte[] generateNewCustomeReport(Integer eventId) throws Exception;
	 
	 byte[] generateKitchenItineraryReport(Integer eventId) throws Exception;
}
