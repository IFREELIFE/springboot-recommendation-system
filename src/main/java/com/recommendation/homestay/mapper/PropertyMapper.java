package com.recommendation.homestay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recommendation.homestay.entity.Property;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PropertyMapper extends BaseMapper<Property> {
    
    /**
     * Find top 10 available properties ordered by booking count
     */
    @Select("SELECT * FROM properties WHERE available = 1 ORDER BY booking_count DESC LIMIT 10")
    List<Property> findTop10ByAvailableTrueOrderByBookingCountDesc();
    
    /**
     * Find top 10 available properties ordered by rating
     */
    @Select("SELECT * FROM properties WHERE available = 1 ORDER BY rating DESC LIMIT 10")
    List<Property> findTop10ByAvailableTrueOrderByRatingDesc();
    
    /**
     * Increment view count for a property
     */
    @Update("UPDATE properties SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);
}
