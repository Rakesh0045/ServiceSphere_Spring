package com.kce.localservices.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:back-end/uploads}") // Default to back-end/uploads if not specified
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /uploads/** to the file system directory
        // Resolve relative path to absolute path
        File uploadPath = new File(uploadDir);
        if (!uploadPath.isAbsolute()) {
            uploadPath = new File(System.getProperty("user.dir"), uploadDir);
        }

        // Create directory if it doesn't exist
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }

        // Convert to file URL with forward slashes (works on all OS)
        String absolutePath = uploadPath.getAbsolutePath().replace("\\", "/");
        String fileUrl = "file:///" + absolutePath + "/";

        System.out.println("Serving uploads from: " + fileUrl);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileUrl);
    }
}
