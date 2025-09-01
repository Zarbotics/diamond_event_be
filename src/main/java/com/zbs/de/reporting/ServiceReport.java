package com.zbs.de.reporting;

import org.springframework.stereotype.Service;

@Service
public interface ServiceReport {
	byte[] generateEventReport(Integer eventId) throws Exception;
	
	 byte[] generateEventReportClientSide(Integer eventId) throws Exception;
}
