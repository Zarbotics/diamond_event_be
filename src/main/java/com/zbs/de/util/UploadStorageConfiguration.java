package com.zbs.de.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Points {@link UtilFileStorage} and {@code ControllerFileServe} at the same
 * configured directory.
 *
 * <p>
 * Both had it hardcoded, separately. The writer used
 * {@code /root/diamondevent_be/uploads/} and returned a URL hardcoded to
 * {@code https://diamondevents.uk:8081}; the reader carried three copies of the
 * path, two commented out, one of them a {@code C:/Users/hp/Pictures}
 * directory on somebody's laptop. Running the application anywhere other than
 * that one production box meant editing Java and rebuilding.
 *
 * <p>
 * {@code app.upload.dir} already existed in {@code application.properties} and
 * was being ignored.
 */
@Configuration
public class UploadStorageConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(UploadStorageConfiguration.class);

	@Value("${app.upload.dir:./uploads/}")
	private String uploadDirectory;

	/**
	 * What uploads are addressed by. Relative by default, so a development
	 * machine serves its own files through its own host rather than handing the
	 * browser a production URL for a file that only exists locally.
	 */
	@Value("${app.upload.public-base-url:/diamond/deimg}")
	private String publicBaseUrl;

	@PostConstruct
	void apply() {
		UtilFileStorage.configure(uploadDirectory, publicBaseUrl);
		LOGGER.info("Uploads are stored in {} and served from {}",
				UtilFileStorage.baseDirectory(), publicBaseUrl);
	}
}
