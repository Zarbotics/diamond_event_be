package com.zbs.de.reporting;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.controller.ControllerEventType;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Service("serviceReport")
public class ServiceReportImpl implements ServiceReport {
	@Autowired
	private DataSource dataSource; // your Spring Boot datasource

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	@Override
	public byte[] generateEventReport(Integer eventId) throws Exception {

		try {
			// Parameters for report
			Map<String, Object> params = new HashMap<>();
			params.put("EVENT_ID", eventId);

			// Load the compiled report
			InputStream reportStream = getClass().getResourceAsStream("/reports/event/event_master_summary.jasper");
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

			// Fill report with data
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource.getConnection());

			// Export to PDF (can also do XLSX/HTML)
			return JasperExportManager.exportReportToPdf(jasperPrint);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}

	}
	
	@Override
	public byte[] generateEventReportClientSide(Integer eventId) throws Exception {

		try {
			// Parameters for report
			Map<String, Object> params = new HashMap<>();
			params.put("EVENT_ID", eventId);

			// Load the compiled report
			InputStream reportStream = getClass().getResourceAsStream("/reports/event/event_master_summary_client.jasper");
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

			// Fill report with data
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource.getConnection());

			// Export to PDF (can also do XLSX/HTML)
			return JasperExportManager.exportReportToPdf(jasperPrint);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}

	}
}
