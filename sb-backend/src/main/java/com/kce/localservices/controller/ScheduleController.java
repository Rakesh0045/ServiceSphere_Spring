package com.kce.localservices.controller;

import com.kce.localservices.entity.ProviderSchedule;
import com.kce.localservices.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api") // api/schedules and api/availability
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/schedules")
    public ResponseEntity<?> getSchedule() {
        return ResponseEntity.ok(scheduleService.getSchedule());
    }

    @PostMapping("/schedules")
    public ResponseEntity<?> updateSchedule(@RequestBody Map<String, List<ProviderSchedule>> body) {
        try {
            scheduleService.updateSchedule(body.get("schedules"));
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Schedule updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/availability/{providerId}/{date}")
    public ResponseEntity<?> getAvailability(@PathVariable Integer providerId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        try {
            List<String> validSlots = scheduleService.getAvailableSlots(providerId, date);
            return ResponseEntity.ok(Map.of("availableSlots", validSlots));
        } catch (Exception e) {
            // e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
}
