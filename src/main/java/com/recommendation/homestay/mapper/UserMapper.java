package com.recommendation.homestay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recommendation.homestay.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 *
 * 基于 MyBatis-Plus 为 User 实体提供数据库操作。
 * 继承 BaseMapper 以获得通用 CRUD 能力。
 * 自定义查询可在业务层通过 QueryWrapper 实现。
 *
 * @author Homestay Recommendation System
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 通用 CRUD 由 MyBatis-Plus BaseMapper 提供
    // 自定义查询建议在业务层使用 QueryWrapper 以获得更好灵活性
}
