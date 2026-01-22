package com.kce.localservices.repository;

import com.kce.localservices.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByServiceId(Integer serviceId);

    Optional<Review> findByBookingId(Integer bookingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.serviceId = :serviceId")
    Double getAverageRating(@Param("serviceId") Integer serviceId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.serviceId = :serviceId")
    Integer countByServiceId(@Param("serviceId") Integer serviceId);
}
