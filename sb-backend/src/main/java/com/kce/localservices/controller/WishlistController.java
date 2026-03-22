package com.kce.localservices.controller;

import com.kce.localservices.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    /** GET /api/wishlist — returns all saved services for current customer */
    @GetMapping
    public ResponseEntity<?> getWishlist() {
        try {
            return ResponseEntity.ok(wishlistService.getWishlist());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    /** POST /api/wishlist/{serviceId}/toggle — add or remove */
    @PostMapping("/{serviceId}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Integer serviceId) {
        try {
            return ResponseEntity.ok(wishlistService.toggleWishlist(serviceId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    /** GET /api/wishlist/{serviceId}/status — check if saved */
    @GetMapping("/{serviceId}/status")
    public ResponseEntity<?> status(@PathVariable Integer serviceId) {
        try {
            return ResponseEntity.ok(Map.of("saved", wishlistService.isSaved(serviceId)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }
}