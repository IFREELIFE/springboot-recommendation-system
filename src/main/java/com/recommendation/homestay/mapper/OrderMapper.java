package com.recommendation.homestay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recommendation.homestay.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Order Mapper Interface
 * 
 * Provides database operations for Order entity using MyBatis-Plus.
 * Extends BaseMapper for common CRUD operations and includes custom queries
 * for order retrieval.
 * 
 * @author Homestay Recommendation System
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * Find order by order number
     * Order number is unique identifier for tracking orders
     * 
     * @param orderNumber Unique order number (format: ORD-XXXXXXXX)
     * @return Order entity or null if not found
     */
    @Select("SELECT * FROM orders WHERE order_number = #{orderNumber}")
    Order findByOrderNumber(String orderNumber);
}
