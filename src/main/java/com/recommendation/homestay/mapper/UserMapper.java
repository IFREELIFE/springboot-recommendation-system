package com.recommendation.homestay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recommendation.homestay.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * Find user by username
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
    
    /**
     * Find user by email
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);
    
    /**
     * Check if username exists
     */
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(String username);
    
    /**
     * Check if email exists
     */
    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    int countByEmail(String email);
}
