package com.kce.localservices.repository;

import com.kce.localservices.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByCustomerId(Integer customerId);
    Optional<Wishlist> findByCustomerIdAndServiceId(Integer customerId, Integer serviceId);
    void deleteByCustomerIdAndServiceId(Integer customerId, Integer serviceId);
    boolean existsByCustomerIdAndServiceId(Integer customerId, Integer serviceId);
}