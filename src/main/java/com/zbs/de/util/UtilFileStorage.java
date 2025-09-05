package com.zbs.de.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public class UtilFileStorage {

//	public static String saveFile(MultipartFile file, String category) throws IOException {
//		// String folder = "/data/uploads/" + category;
//		String folder = "/root/diamondevent_be/uploads/" + category;
//		Files.createDirectories(Paths.get(folder));
//
//		String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//		Path filePath = Paths.get(folder, fileName);
//		file.transferTo(filePath);
//
//		return filePath.toString();
//	}

	public static String saveFile(MultipartFile file, String category) throws IOException {
		String folder = "C:/Users/hp/Pictures/diamond_ev/" + category;
		Files.createDirectories(Paths.get(folder));

		String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
		Path filePath = Paths.get(folder, fileName);
		file.transferTo(filePath);

		// Replace this line:
		// return filePath.toString();

		// With this line:
		return "http://localhost:8080/diamond/deimg/" + category + "/" + fileName;
	}

}
