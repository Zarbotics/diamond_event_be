//package com.zbs.de.reporting;
//
//import java.io.InputStream;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.sql.DataSource;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.zbs.de.controller.ControllerEventType;
//
//import net.sf.jasperreports.engine.JasperExportManager;
//import net.sf.jasperreports.engine.JasperFillManager;
//import net.sf.jasperreports.engine.JasperPrint;
//import net.sf.jasperreports.engine.JasperReport;
//import net.sf.jasperreports.engine.util.JRLoader;
//
//@Service("serviceReport")
//public class ServiceReportImpl implements ServiceReport {
//	@Autowired
//	private DataSource dataSource; // your Spring Boot datasource
//
//	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);
//
//	@Override
//	public byte[] generateEventReport(Integer eventId) throws Exception {
//
//		try {
//			// Parameters for report
//			Map<String, Object> params = new HashMap<>();
//			params.put("EVENT_ID", eventId);
//
//			// Load the compiled report
//			InputStream reportStream = getClass().getResourceAsStream("/reports/event/event_master_summary.jasper");
//			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);
//
//			// Fill report with data
//			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource.getConnection());
//
//			// Export to PDF (can also do XLSX/HTML)
//			return JasperExportManager.exportReportToPdf(jasperPrint);
//		} catch (Exception e) {
//			LOGGER.debug(e.getMessage(), e);
//			return null;
//		}
//
//	}
//	
//	@Override
//	public byte[] generateEventReportClientSide(Integer eventId) throws Exception {
//
//		try {
//			// Parameters for report
//			Map<String, Object> params = new HashMap<>();
//			params.put("EVENT_ID", eventId);
//
//			// Load the compiled report
//			InputStream reportStream = getClass().getResourceAsStream("/reports/event/event_master_summary_client.jasper");
//			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);
//
//			// Fill report with data
//			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource.getConnection());
//
//			// Export to PDF (can also do XLSX/HTML)
//			return JasperExportManager.exportReportToPdf(jasperPrint);
//		} catch (Exception e) {
//			LOGGER.debug(e.getMessage(), e);
//			return null;
//		}
//
//	}
//}

package com.zbs.de.reporting;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zbs.de.controller.ControllerEventType;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Service("serviceReport")
public class ServiceReportImpl implements ServiceReport {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	// Optional server folder fallback. Configure in application.properties if
	// needed:
	// report.images.folder=/opt/diamondevent/uploads/reports
	@Value("${report.images.folder:}")
	private String reportImagesFolder;

	// Classpath locations (adjust if you put images in other resource folder)
	private static final String CLASSPATH_COVER = "/static/images/cover.jpg";
	private static final String CLASSPATH_LOGO = "/static/images/de_logo.png";

	private final DataSource dataSource;

	public ServiceReportImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public byte[] generateEventReport(Integer eventId) throws Exception {
		return generateReport("/reports/event/event_master_summary.jasper", eventId);
	}

	@Override
	public byte[] generateEventReportClientSide(Integer eventId) throws Exception {
		return generateReport("/reports/event/event_master_summary_client.jasper", eventId);
	}
	
	@Override
	public byte[] generateNewItineraryReport(Integer eventId) throws Exception {
		return generateNewReport("/reports/new_event_reports/master_event_master.jasper", eventId);
	}
	
	@Override
	public byte[] generateNewCustomeReport(Integer eventId) throws Exception {
		return generateNewReport("/reports/new_customer_report/master_event_master.jasper", eventId);
	}

	/**
	 * Core report generation: loads the compiled .jasper from classpath, loads
	 * images, sets parameters, fills the report and returns PDF bytes.
	 */
	private byte[] generateReport(String jasperClasspathLocation, Integer eventId) {
		// parameters for report
		Map<String, Object> params = new HashMap<>();
		params.put("EVENT_ID", eventId);

		// load images and put into params (java.awt.Image)
		Image coverImage = loadImageFromClasspathOrFolder(CLASSPATH_COVER, "cover.jpg");
		Image logoImage = loadImageFromClasspathOrFolder(CLASSPATH_LOGO, "de_logo.png");

		if (coverImage != null) {
			params.put("REPORT_COVER_IMAGE", coverImage);
		} else {
			LOGGER.warn("Cover image not found on classpath or configured folder.");
		}

		if (logoImage != null) {
			params.put("REPORT_LOGO_IMAGE", logoImage);
		} else {
			LOGGER.warn("Logo image not found on classpath or configured folder.");
		}

		// load compiled jasper and fill report
		try (InputStream reportStream = getClass().getResourceAsStream(jasperClasspathLocation)) {
			if (reportStream == null) {
				LOGGER.error("Compiled report not found in classpath at: {}", jasperClasspathLocation);
				return null;
			}

			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

			// get DB connection from DataSource; ensure it's closed after fill
			try (Connection conn = dataSource.getConnection()) {
				JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);

				// export to PDF bytes
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					JasperExportManager.exportReportToPdfStream(jasperPrint, baos);
					return baos.toByteArray();
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to generate report {} for event {} : {}", jasperClasspathLocation, eventId,
					e.getMessage(), e);
			return null;
		}
	}

	private byte[] generateNewReport(String jasperClasspathLocation, Integer eventId) {
		// parameters for report
		Map<String, Object> params = new HashMap<>();
		params.put("EVENT_ID", eventId);
		params.put(
			    "SUBREPORT_DIR",
			    getClass()
			        .getResource("/reports/new_customer_report/")
			        .toString()
			);

//		// load images and put into params (java.awt.Image)
//		Image coverImage = loadImageFromClasspathOrFolder(CLASSPATH_COVER, "cover.jpg");
//		Image logoImage = loadImageFromClasspathOrFolder(CLASSPATH_LOGO, "de_logo.png");
//
//		if (coverImage != null) {
//			params.put("REPORT_COVER_IMAGE", coverImage);
//		} else {
//			LOGGER.warn("Cover image not found on classpath or configured folder.");
//		}
//
//		if (logoImage != null) {
//			params.put("REPORT_LOGO_IMAGE", logoImage);
//		} else {
//			LOGGER.warn("Logo image not found on classpath or configured folder.");
//		}

		// load compiled jasper and fill report
		try (InputStream reportStream = getClass().getResourceAsStream(jasperClasspathLocation)) {
			if (reportStream == null) {
				LOGGER.error("Compiled report not found in classpath at: {}", jasperClasspathLocation);
				return null;
			}

			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

			// get DB connection from DataSource; ensure it's closed after fill
			try (Connection conn = dataSource.getConnection()) {
				JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);

				// export to PDF bytes
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					JasperExportManager.exportReportToPdfStream(jasperPrint, baos);
					return baos.toByteArray();
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to generate report {} for event {} : {}", jasperClasspathLocation, eventId,
					e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * Try to load image first from classpathPath (e.g.
	 * "/static/images/de_logo.png"). If not found, try to load from configured
	 * folder (reportImagesFolder + filename). Returns java.awt.Image
	 * (BufferedImage) or null if unable to load.
	 */
	private Image loadImageFromClasspathOrFolder(String classpathPath, String filename) {
		// 1) try classpath
		try (InputStream is = getClass().getResourceAsStream(classpathPath)) {
			if (is != null) {
				BufferedImage img = ImageIO.read(is);
				if (img != null)
					return img;
				// otherwise continue to fallback
			}
		} catch (Exception ex) {
			LOGGER.debug("Failed to load image from classpath {} : {}", classpathPath, ex.getMessage());
		}

		// 2) try server folder fallback (if configured)
		try {
			if (reportImagesFolder != null && !reportImagesFolder.trim().isEmpty() && filename != null
					&& !filename.trim().isEmpty()) {
				Path p = Paths.get(reportImagesFolder, filename);
				if (Files.exists(p) && Files.isReadable(p)) {
					try (InputStream fis = Files.newInputStream(p)) {
						BufferedImage img = ImageIO.read(fis);
						if (img != null)
							return img;
						LOGGER.debug("Image {} present but ImageIO couldn't read it.", p);
					}
				} else {
					LOGGER.debug("Image file not found or not readable at configured folder: {}", p);
				}
			}
		} catch (Exception ex) {
			LOGGER.debug("Failed to load image from configured folder {}: {}", reportImagesFolder, ex.getMessage());
		}

		return null;
	}
}
