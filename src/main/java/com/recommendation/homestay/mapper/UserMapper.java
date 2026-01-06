package com.recommendation.homestay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recommendation.homestay.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * User Mapper Interface
 * 
 * Provides database operations for User entity using MyBatis-Plus.
 * Extends BaseMapper to inherit common CRUD operations.
 * Custom queries can be implemented using QueryWrapper in service layer.
 * 
 * @author Homestay Recommendation System
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // All CRUD operations are provided by MyBatis-Plus BaseMapper
    // Custom queries should use QueryWrapper in service layer for better flexibility
}
