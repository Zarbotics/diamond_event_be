package com.zbs.de.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.core.io.Resource; // ✅ CORRECT

@RestController
@RequestMapping("/deimg")
@CrossOrigin(origins = "")
public class ControllerFileServe {
	@GetMapping("/{category}/{filename:.+}")
	public ResponseEntity<Resource> serveFile(@PathVariable String category, @PathVariable String filename)
			throws IOException {
		Path filePath = Paths.get("/root/diamondevent_be/uploads/" + category + "/" + filename);
		if (!Files.exists(filePath)) {
			return ResponseEntity.notFound().build();
		}

		UrlResource resource = new UrlResource(filePath.toUri());
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
	}
}
