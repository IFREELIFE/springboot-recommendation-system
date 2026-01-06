package com.recommendation.homestay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recommendation.homestay.entity.UserPropertyInteraction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * User Property Interaction Mapper Interface
 * 
 * Provides database operations for UserPropertyInteraction entity using MyBatis-Plus.
 * Tracks user interactions (views, favorites, bookings, reviews) with properties
 * for recommendation system algorithms.
 * 
 * @author Homestay Recommendation System
 */
@Mapper
public interface UserPropertyInteractionMapper extends BaseMapper<UserPropertyInteraction> {
    
    /**
     * Find recent interactions by user ID, ordered by created_at DESC
     * Used for analyzing user's recent activity patterns
     * 
     * @param userId User ID
     * @return List of user interactions ordered by most recent first
     */
    @Select("SELECT * FROM user_property_interactions WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<UserPropertyInteraction> findRecentInteractionsByUser(Long userId);
    
    /**
     * Find most interacted properties by user ID, grouped by property_id
     * Returns property_id and interaction count for recommendation scoring
     * 
     * @param userId User ID
     * @return List of PropertyInteractionCount with property IDs and their interaction counts
     */
    @Select("SELECT property_id as propertyId, COUNT(*) as interactionCount " +
            "FROM user_property_interactions " +
            "WHERE user_id = #{userId} " +
            "GROUP BY property_id " +
            "ORDER BY interactionCount DESC")
    List<PropertyInteractionCount> findMostInteractedProperties(Long userId);
    
    /**
     * DTO for property interaction count result
     * Used for recommendation algorithms to score properties based on user interaction frequency
     */
    class PropertyInteractionCount {
        private Long propertyId;
        private Long interactionCount;
        
        public Long getPropertyId() { return propertyId; }
        public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
        public Long getInteractionCount() { return interactionCount; }
        public void setInteractionCount(Long interactionCount) { this.interactionCount = interactionCount; }
    }
}
