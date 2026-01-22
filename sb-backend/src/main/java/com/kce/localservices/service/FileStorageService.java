package com.kce.localservices.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;

@Service
public class FileStorageService {

    // Use configurable upload directory from application.properties
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.port}")
    private String serverPort;

    @PostConstruct
    public void init() {
        try {
            // Resolve relative path to absolute path
            Path uploadPath = Paths.get(uploadDir);
            if (!uploadPath.isAbsolute()) {
                uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
            }

            // Create directory if it doesn't exist
            Files.createDirectories(uploadPath);

            // Update uploadDir to absolute path for consistency
            uploadDir = uploadPath.toAbsolutePath().toString();

            System.out.println("Upload directory initialized at: " + uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    public String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueSuffix = new Date().getTime() + "-" + Math.round(Math.random() * 1E9);
        String fileName = "image-" + uniqueSuffix + "-" + originalFileName;

        try {
            Path targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Construct URL using the Spring Boot server port
            return "http://localhost:" + serverPort + "/uploads/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
}
