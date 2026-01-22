package com.kce.localservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Enable scheduled tasks for analytics refresh
@EnableAsync // Enable async event handling
public class LocalServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalServicesApplication.class, args);
    }
}
