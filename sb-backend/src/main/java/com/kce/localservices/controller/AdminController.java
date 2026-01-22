package com.kce.localservices.controller;

import com.kce.localservices.service.AdminService;
import com.kce.localservices.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ServiceService serviceService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('Admin')") // Handled by filter/config usually, explicitly here
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/services")
    public ResponseEntity<?> getServices() {
        return ResponseEntity.ok(serviceService.getAllServicesForAdmin());
    }

    @PutMapping("/services/{id}/status")
    public ResponseEntity<?> updateServiceStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            serviceService.updateServiceStatus(id, body.get("status"));
            return ResponseEntity.ok(Map.of("message", "Service has been " + body.get("status").toLowerCase() + "."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/refresh-analytics")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> refreshAnalytics() {
        try {
            adminService.refreshAllAnalytics();
            return ResponseEntity
                    .ok(Map.of("message", "Analytics refreshed successfully", "stats", adminService.getStats()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to refresh analytics: " + e.getMessage()));
        }
    }
}
