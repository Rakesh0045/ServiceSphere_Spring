package com.kce.localservices.repository;

import com.kce.localservices.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Integer> {
        List<Service> findByProviderId(Integer providerId);

        // Custom query to support dynamic filtering if needed, or use Specifications
        // later.
        // For now simple Jpa methods or @Query.

        @Query("SELECT s FROM Service s WHERE " +
                        "(:category IS NULL OR s.category LIKE %:category%) AND " +
                        "(:keyword IS NULL OR s.serviceName LIKE %:keyword% OR s.description LIKE %:keyword%) AND " +
                        "(:location IS NULL OR s.location LIKE %:location%) AND " +
                        "(:providerId IS NULL OR s.providerId = :providerId) AND " +
                        "(:status IS NULL OR s.status = :status)")
        List<Service> searchServices(@Param("category") String category,
                        @Param("keyword") String keyword,
                        @Param("location") String location,
                        @Param("providerId") Integer providerId,
                        @Param("status") String status);

        List<Service> findByStatus(String status);

        // Analytics queries for admin dashboard
        @Query("SELECT s.category as category, COUNT(b.id) as bookingCount " +
                        "FROM Service s JOIN Booking b ON s.id = b.serviceId " +
                        "WHERE b.status = 'Completed' " +
                        "GROUP BY s.category " +
                        "ORDER BY COUNT(b.id) DESC")
        List<Object[]> findTopCategoriesByBookingCount();

        @Query("SELECT s.id as serviceId, s.serviceName as serviceName, s.category as category, " +
                        "COUNT(b.id) as bookingCount " +
                        "FROM Service s JOIN Booking b ON s.id = b.serviceId " +
                        "WHERE b.status = 'Completed' " +
                        "GROUP BY s.id, s.serviceName, s.category " +
                        "ORDER BY COUNT(b.id) DESC")
        List<Object[]> findTopServicesByBookingCount();
}
