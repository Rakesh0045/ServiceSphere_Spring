package com.kce.localservices.service;

import com.kce.localservices.entity.Service;
import com.kce.localservices.entity.User;
import com.kce.localservices.event.AnalyticsEvent;
import com.kce.localservices.repository.ReviewRepository;
import com.kce.localservices.repository.ServiceRepository;
import com.kce.localservices.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void createService(Service service) {
        User currentUser = userService.getCurrentUser();
        if (!"Service Provider".equals(currentUser.getRole())) {
            throw new RuntimeException("Only Service Providers can create services.");
        }
        service.setProviderId(currentUser.getId());
        service.setStatus("Pending");
        service.setAvailability("Available");
        serviceRepository.save(service);

        // Publish analytics event
        eventPublisher.publishEvent(new AnalyticsEvent(this, "total_services", 1));
    }

    public List<Map<String, Object>> getAllServices(String category, String keyword, String location,
            Integer providerId, String sortBy) {
        // Simple implementation: Fetch all and filter/sort in memory if complex, or use
        // Repository methods.
        // Given the prompt "exact same frontend", we need to match the return structure
        // which includes "provider_name", "average_rating", "review_count".

        List<Service> services;

        if (providerId != null) {
            services = serviceRepository.findByProviderId(providerId);
        } else {
            // For customers, usually only "Approved".
            // Logic: If providerId is passed (provider viewing own services), show all.
            // If browsing, show only Approved.
            // But simpler to fetch all and filter.
            // Let's use the repository search method we defined, passing nulls for optional
            // params.
            services = serviceRepository.searchServices(category, keyword, location, providerId,
                    (providerId == null) ? "Approved" : null);
        }

        List<Map<String, Object>> result = services.stream().map(service -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", service.getId());
            map.put("provider_id", service.getProviderId());
            map.put("service_name", service.getServiceName());
            map.put("description", service.getDescription());
            map.put("category", service.getCategory());
            map.put("price", service.getPrice());
            map.put("availability", service.getAvailability());
            map.put("location", service.getLocation());
            map.put("image_url", service.getImageUrl());
            map.put("status", service.getStatus());
            map.put("created_at", service.getCreatedAt());

            User provider = userRepository.findById(service.getProviderId()).orElse(null);
            map.put("provider_name", provider != null ? provider.getName() : "Unknown");

            Double avgRating = reviewRepository.getAverageRating(service.getId());
            map.put("average_rating", avgRating != null ? avgRating : 0.0);

            Integer reviewCount = reviewRepository.countByServiceId(service.getId());
            map.put("review_count", reviewCount != null ? reviewCount : 0);

            return map;
        }).collect(Collectors.toList());

        // Sorting
        if ("price_asc".equals(sortBy)) {
            result.sort((a, b) -> ((BigDecimal) a.get("price")).compareTo((BigDecimal) b.get("price")));
        } else if ("price_desc".equals(sortBy)) {
            result.sort((a, b) -> ((BigDecimal) b.get("price")).compareTo((BigDecimal) a.get("price")));
        } else if ("rating_desc".equals(sortBy)) {
            result.sort((a, b) -> {
                int cmp = Double.compare((Double) b.get("average_rating"), (Double) a.get("average_rating"));
                if (cmp == 0) {
                    return Integer.compare((Integer) b.get("review_count"), (Integer) a.get("review_count"));
                }
                return cmp;
            });
        }

        return result;
    }

    public void updateService(Integer id, Service updatedService) {
        User currentUser = userService.getCurrentUser();
        Service service = serviceRepository.findById(id).orElseThrow(() -> new RuntimeException("Service not found"));

        if (!service.getProviderId().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to edit this service.");
        }

        // Only update fields that are not null in the request
        if (updatedService.getServiceName() != null) {
            service.setServiceName(updatedService.getServiceName());
        }
        if (updatedService.getDescription() != null) {
            service.setDescription(updatedService.getDescription());
        }
        if (updatedService.getCategory() != null) {
            service.setCategory(updatedService.getCategory());
        }
        if (updatedService.getPrice() != null) {
            service.setPrice(updatedService.getPrice());
        }
        if (updatedService.getAvailability() != null) {
            service.setAvailability(updatedService.getAvailability());
        }
        if (updatedService.getLocation() != null) {
            service.setLocation(updatedService.getLocation());
        }
        if (updatedService.getImageUrl() != null) {
            service.setImageUrl(updatedService.getImageUrl());
        }

        serviceRepository.save(service);
    }

    public void deleteService(Integer id) {
        User currentUser = userService.getCurrentUser();
        Service service = serviceRepository.findById(id).orElseThrow(() -> new RuntimeException("Service not found"));

        if (!service.getProviderId().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to delete this service.");
        }
        serviceRepository.delete(service);

        // Publish analytics event
        eventPublisher.publishEvent(new AnalyticsEvent(this, "total_services", -1));
    }

    // Admin methods
    public List<Map<String, Object>> getAllServicesForAdmin() {
        List<Service> services = serviceRepository.findAll(); // Should actully be custom query to order by date
        // Replicating: SELECT s.*, u.name as provider_name ... ORDER BY s.created_at
        // DESC

        // Simulating the join
        return services.stream().sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())).map(service -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", service.getId());
            map.put("provider_id", service.getProviderId());
            // ... copy all properties ...
            map.put("service_name", service.getServiceName());
            map.put("description", service.getDescription());
            map.put("category", service.getCategory());
            map.put("price", service.getPrice());
            map.put("location", service.getLocation());
            map.put("status", service.getStatus());
            map.put("image_url", service.getImageUrl());
            map.put("created_at", service.getCreatedAt());

            User provider = userRepository.findById(service.getProviderId()).orElse(null);
            map.put("provider_name", provider != null ? provider.getName() : "Unknown");
            return map;
        }).collect(Collectors.toList());
    }

    public void updateServiceStatus(Integer id, String status) {
        Service service = serviceRepository.findById(id).orElseThrow(() -> new RuntimeException("Service not found"));
        service.setStatus(status);
        serviceRepository.save(service);
    }
}
