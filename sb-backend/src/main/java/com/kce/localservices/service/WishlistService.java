package com.kce.localservices.service;

import com.kce.localservices.entity.Service;
import com.kce.localservices.entity.User;
import com.kce.localservices.entity.Wishlist;
import com.kce.localservices.repository.ReviewRepository;
import com.kce.localservices.repository.ServiceRepository;
import com.kce.localservices.repository.UserRepository;
import com.kce.localservices.repository.WishlistRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    private final ServiceRepository serviceRepository;

    private final UserRepository userRepository;

    private final ReviewRepository reviewRepository;

    private final UserService userService;

    public WishlistService(
            WishlistRepository wishlistRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository,
            ReviewRepository reviewRepository,
            UserService userService) {
        this.wishlistRepository = wishlistRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.userService = userService;
    }

    /** Toggle: add if not present, remove if already saved. Returns new state. */
    @Transactional
    public Map<String, Object> toggleWishlist(Integer serviceId) {
        User currentUser = userService.getCurrentUser();
        boolean exists = wishlistRepository.existsByCustomerIdAndServiceId(currentUser.getId(), serviceId);

        if (exists) {
            wishlistRepository.deleteByCustomerIdAndServiceId(currentUser.getId(), serviceId);
            return Map.of("saved", false, "message", "Removed from wishlist.");
        } else {
            Wishlist w = new Wishlist();
            w.setCustomerId(currentUser.getId());
            w.setServiceId(serviceId);
            wishlistRepository.save(w);
            return Map.of("saved", true, "message", "Added to wishlist!");
        }
    }

    /** Get all wishlisted services for the current customer, fully hydrated. */
    public List<Map<String, Object>> getWishlist() {
        User currentUser = userService.getCurrentUser();
        List<Wishlist> wishlistItems = wishlistRepository.findByCustomerId(currentUser.getId());

        return wishlistItems.stream().map(w -> {
            Map<String, Object> map = new HashMap<>();
            Service service = serviceRepository.findById(w.getServiceId()).orElse(null);
            if (service == null)
                return null;

            map.put("wishlist_id", w.getId());
            map.put("saved_at", w.getCreatedAt());
            map.put("id", service.getId());
            map.put("service_name", service.getServiceName());
            map.put("description", service.getDescription());
            map.put("category", service.getCategory());
            map.put("price", service.getPrice());
            map.put("location", service.getLocation());
            map.put("image_url", service.getImageUrl());
            map.put("availability", service.getAvailability());
            map.put("status", service.getStatus());
            map.put("provider_id", service.getProviderId());

            User provider = userRepository.findById(service.getProviderId()).orElse(null);
            map.put("provider_name", provider != null ? provider.getName() : "Unknown");

            Double avg = reviewRepository.getAverageRating(service.getId());
            Integer count = reviewRepository.countByServiceId(service.getId());
            map.put("average_rating", avg != null ? avg : 0.0);
            map.put("review_count", count != null ? count : 0);

            return map;
        }).filter(m -> m != null).collect(Collectors.toList());
    }

    /** Check if a specific service is saved by the current user. */
    public boolean isSaved(Integer serviceId) {
        User currentUser = userService.getCurrentUser();
        return wishlistRepository.existsByCustomerIdAndServiceId(currentUser.getId(), serviceId);
    }
}