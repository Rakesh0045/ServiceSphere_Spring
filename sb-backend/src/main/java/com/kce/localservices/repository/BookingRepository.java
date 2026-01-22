package com.kce.localservices.repository;

import com.kce.localservices.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Date;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    List<Booking> findByProviderIdOrderByCreatedAtDesc(Integer providerId);

    List<Booking> findByProviderIdAndBookingStartTimeAndStatusIn(Integer providerId, Date bookingStartTime,
            List<String> statuses);

    List<Booking> findByProviderIdAndBookingStartTimeBetween(Integer providerId, Date start, Date end);

    long countByStatus(String status);

    // Revenue analytics
    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Booking b JOIN Service s ON b.serviceId = s.id WHERE b.status = 'Completed'")
    Double getTotalRevenue();

    @Query("SELECT COALESCE(AVG(s.price), 0) FROM Booking b JOIN Service s ON b.serviceId = s.id WHERE b.status = 'Completed'")
    Double getAverageBookingValue();

    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Booking b JOIN Service s ON b.serviceId = s.id WHERE b.status = 'Pending'")
    Double getPendingRevenue();

    // Top providers by earnings
    @Query("SELECT b.providerId, u.name, COALESCE(SUM(s.price), 0) as earnings, COUNT(b.id) as bookingCount " +
            "FROM Booking b " +
            "JOIN Service s ON b.serviceId = s.id " +
            "JOIN User u ON b.providerId = u.id " +
            "WHERE b.status = 'Completed' " +
            "GROUP BY b.providerId, u.name " +
            "ORDER BY SUM(s.price) DESC")
    List<Object[]> getTopProvidersByEarnings();

    // Recent activities (last 10 bookings)
    @Query("SELECT b.id, u.name as customerName, s.serviceName, b.status, b.bookingStartTime, s.price, p.name as providerName "
            +
            "FROM Booking b " +
            "JOIN User u ON b.customerId = u.id " +
            "JOIN Service s ON b.serviceId = s.id " +
            "JOIN User p ON b.providerId = p.id " +
            "ORDER BY b.createdAt DESC")
    List<Object[]> getRecentActivities();
}
