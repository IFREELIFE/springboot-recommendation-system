package com.recommendation.homestay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recommendation.homestay.entity.UserPropertyInteraction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserPropertyInteractionMapper extends BaseMapper<UserPropertyInteraction> {
    
    /**
     * Find recent interactions by user ID, ordered by created_at DESC
     */
    @Select("SELECT * FROM user_property_interactions WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<UserPropertyInteraction> findRecentInteractionsByUser(Long userId);
    
    /**
     * Find most interacted properties by user ID, grouped by property_id
     * Returns property_id and interaction count
     */
    @Select("SELECT property_id as propertyId, COUNT(*) as interactionCount " +
            "FROM user_property_interactions " +
            "WHERE user_id = #{userId} " +
            "GROUP BY property_id " +
            "ORDER BY interactionCount DESC")
    List<PropertyInteractionCount> findMostInteractedProperties(Long userId);
    
    /**
     * DTO for property interaction count result
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
