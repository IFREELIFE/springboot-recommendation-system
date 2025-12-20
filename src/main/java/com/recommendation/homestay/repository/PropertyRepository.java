package com.recommendation.homestay.repository;

import com.recommendation.homestay.entity.Property;
import com.recommendation.homestay.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    
    Page<Property> findByLandlord(User landlord, Pageable pageable);
    
    Page<Property> findByAvailableTrue(Pageable pageable);
    
    Page<Property> findByCityAndAvailableTrue(String city, Pageable pageable);
    
    @Query("SELECT p FROM Property p WHERE p.available = true " +
           "AND (:city IS NULL OR p.city = :city) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:bedrooms IS NULL OR p.bedrooms >= :bedrooms)")
    Page<Property> searchProperties(
        @Param("city") String city,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("bedrooms") Integer bedrooms,
        Pageable pageable
    );
    
    List<Property> findTop10ByAvailableTrueOrderByBookingCountDesc();
    
    List<Property> findTop10ByAvailableTrueOrderByRatingDesc();
}
