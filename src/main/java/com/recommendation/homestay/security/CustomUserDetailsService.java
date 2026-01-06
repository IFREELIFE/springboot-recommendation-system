package com.recommendation.homestay.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom User Details Service
 * 
 * Implements Spring Security's UserDetailsService to load user-specific data
 * from database using MyBatis-Plus. Integrates authentication with the User entity.
 * 
 * Key features:
 * - Load user by username for authentication
 * - Load user by ID for JWT token validation
 * - Uses MyBatis-Plus QueryWrapper for flexible queries
 * 
 * @author Homestay Recommendation System
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * Load user by username for Spring Security authentication
     * 
     * Uses MyBatis-Plus QueryWrapper to query user by username.
     * 
     * @param username Username to search for
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Query user using MyBatis-Plus QueryWrapper
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return UserPrincipal.create(user);
    }

    /**
     * Load user by ID for JWT token validation
     * 
     * Uses MyBatis-Plus selectById for efficient lookup by primary key.
     * 
     * @param id User ID
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional
    public UserDetails loadUserById(Long id) {
        // Query user using MyBatis-Plus selectById
        User user = userMapper.selectById(id);
        
        if (user == null) {
            throw new UsernameNotFoundException("User not found with id: " + id);
        }
        return UserPrincipal.create(user);
    }
}
