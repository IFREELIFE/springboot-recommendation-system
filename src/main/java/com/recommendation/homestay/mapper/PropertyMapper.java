package com.recommendation.homestay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recommendation.homestay.entity.Property;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Property Mapper Interface
 * 
 * Provides database operations for Property entity using MyBatis-Plus.
 * Extends BaseMapper for common CRUD operations and includes custom queries
 * for property recommendations and statistics.
 * 
 * @author Homestay Recommendation System
 */
@Mapper
public interface PropertyMapper extends BaseMapper<Property> {
    
    /**
     * Find top 10 available properties ordered by booking count
     * Used for popular property recommendations
     * 
     * @return List of top 10 most booked properties
     */
    @Select("SELECT * FROM properties WHERE available = 1 ORDER BY booking_count DESC LIMIT 10")
    List<Property> findTop10ByAvailableTrueOrderByBookingCountDesc();
    
    /**
     * Find top 10 available properties ordered by rating
     * Used for top-rated property recommendations
     * 
     * @return List of top 10 highest-rated properties
     */
    @Select("SELECT * FROM properties WHERE available = 1 ORDER BY rating DESC LIMIT 10")
    List<Property> findTop10ByAvailableTrueOrderByRatingDesc();
    
    /**
     * Increment view count for a property
     * Called when a user views property details
     * 
     * @param id Property ID
     * @return Number of rows affected (should be 1 if successful)
     */
    @Update("UPDATE properties SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);
}
