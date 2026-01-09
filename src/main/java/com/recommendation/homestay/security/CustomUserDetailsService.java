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
 * 自定义用户详情服务
 *
 * 实现 Spring Security 的 UserDetailsService，基于 MyBatis-Plus 加载用户数据，
 * 将认证流程与用户实体对接。
 *
 * 主要功能：
 * - 通过用户名加载用户用于认证
 * - 通过用户ID加载用户用于 JWT 校验
 * - 使用 MyBatis-Plus QueryWrapper 提供灵活查询
 *
 * @author Homestay Recommendation System
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 按用户名加载用户（用于认证）
     *
     * 通过 MyBatis-Plus QueryWrapper 查询用户名。
     *
     * @param username 需查询的用户名
     * @return Spring Security 使用的 UserDetails
     * @throws UsernameNotFoundException 当用户不存在时抛出
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 使用 MyBatis-Plus QueryWrapper 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            throw new UsernameNotFoundException("未找到用户名为 " + username + " 的用户");
        }
        return UserPrincipal.create(user);
    }

    /**
     * 按用户ID加载用户（用于 JWT 校验）
     *
     * 使用 MyBatis-Plus selectById 通过主键高效查询。
     *
     * @param id 用户ID
     * @return Spring Security 使用的 UserDetails
     * @throws UsernameNotFoundException 当用户不存在时抛出
     */
    @Transactional
    public UserDetails loadUserById(Long id) {
        // 使用 MyBatis-Plus selectById 查询用户
        User user = userMapper.selectById(id);
        
        if (user == null) {
            throw new UsernameNotFoundException("未找到ID为 " + id + " 的用户");
        }
        return UserPrincipal.create(user);
    }
}
