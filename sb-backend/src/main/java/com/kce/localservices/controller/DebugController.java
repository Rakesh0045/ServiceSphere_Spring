package com.kce.localservices.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/uploads-info")
    public ResponseEntity<Map<String, Object>> getUploadsInfo() {
        Map<String, Object> info = new HashMap<>();

        // Resolve upload path
        File uploadPath = new File(uploadDir);
        if (!uploadPath.isAbsolute()) {
            uploadPath = new File(System.getProperty("user.dir"), uploadDir);
        }

        info.put("uploadDir", uploadDir);
        info.put("absolutePath", uploadPath.getAbsolutePath());
        info.put("exists", uploadPath.exists());
        info.put("isDirectory", uploadPath.isDirectory());
        info.put("canRead", uploadPath.canRead());
        info.put("canWrite", uploadPath.canWrite());

        if (uploadPath.exists() && uploadPath.isDirectory()) {
            File[] files = uploadPath.listFiles();
            info.put("fileCount", files != null ? files.length : 0);
            if (files != null && files.length > 0) {
                String[] fileNames = new String[Math.min(5, files.length)];
                for (int i = 0; i < fileNames.length; i++) {
                    fileNames[i] = files[i].getName();
                }
                info.put("sampleFiles", fileNames);
            }
        }

        info.put("serverPort", serverPort);
        info.put("workingDirectory", System.getProperty("user.dir"));

        return ResponseEntity.ok(info);
    }
}
