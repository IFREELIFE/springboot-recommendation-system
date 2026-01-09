package com.recommendation.homestay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.recommendation.homestay.entity.Property;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.entity.UserPropertyInteraction;
import com.recommendation.homestay.mapper.PropertyMapper;
import com.recommendation.homestay.mapper.UserPropertyInteractionMapper;
import com.recommendation.homestay.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserPropertyInteractionMapper interactionMapper;

    /**
     * 混合推荐：结合协同过滤与内容相似推荐，是核心的综合推荐算法
     */
    @Cacheable(value = "recommendations", key = "#userId")
    public List<Property> getRecommendations(Long userId, int limit) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("未找到用户");
        }

        // 同时获取两种算法的推荐结果
        List<Property> collaborativeRecommendations = getCollaborativeFilteringRecommendations(userId, limit * 2);
        List<Property> contentBasedRecommendations = getContentBasedRecommendations(userId, limit * 2);

        // 合并并打分
        Map<Long, Double> propertyScores = new HashMap<>();

        // 权重：协同过滤 60%，内容相似 40%
        for (int i = 0; i < collaborativeRecommendations.size(); i++) {
            Property property = collaborativeRecommendations.get(i);
            double score = (collaborativeRecommendations.size() - i) * 0.6;
            propertyScores.merge(property.getId(), score, Double::sum);
        }

        for (int i = 0; i < contentBasedRecommendations.size(); i++) {
            Property property = contentBasedRecommendations.get(i);
            double score = (contentBasedRecommendations.size() - i) * 0.4;
            propertyScores.merge(property.getId(), score, Double::sum);
        }

        // 按得分排序并返回前 N 个
        return propertyScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> propertyMapper.selectById(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 协同过滤：基于相似用户的推荐，寻找相似用户喜欢的房源
     */
    public List<Property> getCollaborativeFilteringRecommendations(Long userId, int limit) {
        QueryWrapper<UserPropertyInteraction> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<UserPropertyInteraction> userInteractions = interactionMapper.selectList(queryWrapper);

        if (userInteractions.isEmpty()) {
            // 冷启动：返回热门房源
            return propertyMapper.findTop10ByAvailableTrueOrderByBookingCountDesc()
                    .stream().limit(limit).collect(Collectors.toList());
        }

        // 获取当前用户已交互的房源
        Set<Long> interactedPropertyIds = userInteractions.stream()
                .map(UserPropertyInteraction::getPropertyId)
                .collect(Collectors.toSet());

        // 基于公共房源交互寻找相似用户
        List<UserPropertyInteraction> allInteractions = interactionMapper.selectList(null);
        
        Map<Long, Set<Long>> userPropertyMap = new HashMap<>();
        for (UserPropertyInteraction interaction : allInteractions) {
            userPropertyMap.computeIfAbsent(interaction.getUserId(), k -> new HashSet<>())
                    .add(interaction.getPropertyId());
        }

        // 计算与其他用户的相似度
        Map<Long, Double> similarityScores = new HashMap<>();
        Set<Long> currentUserProperties = userPropertyMap.get(userId);
        
        if (currentUserProperties == null) {
            return propertyMapper.findTop10ByAvailableTrueOrderByBookingCountDesc()
                    .stream().limit(limit).collect(Collectors.toList());
        }

        for (Map.Entry<Long, Set<Long>> entry : userPropertyMap.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) continue;

            Set<Long> otherUserProperties = entry.getValue();
            double similarity = calculateJaccardSimilarity(currentUserProperties, otherUserProperties);
            if (similarity > 0) {
                similarityScores.put(otherUserId, similarity);
            }
        }

        // 根据相似用户的交互汇总推荐
        Map<Long, Double> recommendationScores = new HashMap<>();
        for (Map.Entry<Long, Double> entry : similarityScores.entrySet()) {
            Long similarUserId = entry.getKey();
            Double similarity = entry.getValue();
            
            Set<Long> similarUserProperties = userPropertyMap.get(similarUserId);
            for (Long propertyId : similarUserProperties) {
                if (!interactedPropertyIds.contains(propertyId)) {
                    recommendationScores.merge(propertyId, similarity, Double::sum);
                }
            }
        }

        // 取出得分最高的推荐结果
        return recommendationScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> propertyMapper.selectById(entry.getKey()))
                .filter(Objects::nonNull)
                .filter(Property::getAvailable)
                .collect(Collectors.toList());
    }

    /**
     * 基于内容的推荐：推荐与用户喜欢的房源相似的房源
     */
    public List<Property> getContentBasedRecommendations(Long userId, int limit) {
        // 1. 查询用户的房源交互记录
        QueryWrapper<UserPropertyInteraction> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<UserPropertyInteraction> userInteractions = interactionMapper.selectList(queryWrapper);

        // 冷启动：无交互记录时返回评分最高的可用房源
        if (userInteractions.isEmpty()) {
            return propertyMapper.findTop10ByAvailableTrueOrderByRatingDesc()
                    .stream().limit(limit).collect(Collectors.toList());
        }

        // 2. 筛选用户"喜欢"的房源（收藏/预订/高评分）
        List<Property> likedProperties = new ArrayList<>();
        for (UserPropertyInteraction interaction : userInteractions) {
            // 过滤正向交互行为
            boolean isPositiveInteraction = interaction.getType() == UserPropertyInteraction.InteractionType.FAVORITE
                    || interaction.getType() == UserPropertyInteraction.InteractionType.BOOK
                    || (interaction.getRating() != null && interaction.getRating() >= 4);

            if (isPositiveInteraction) {
                Property property = propertyMapper.selectById(interaction.getPropertyId());
                if (property != null) {
                    likedProperties.add(property);
                }
            }
        }

        // 无正向交互时仍返回高评分房源
        if (likedProperties.isEmpty()) {
            return propertyMapper.findTop10ByAvailableTrueOrderByRatingDesc()
                    .stream().limit(limit).collect(Collectors.toList());
        }

        // 3. 提取用户偏好特征
        Map<String, Integer> cityPreferences = new HashMap<>();
        Map<String, Integer> typePreferences = new HashMap<>();
        double totalPrice = 0.0;
        int totalBedrooms = 0;

        for (Property property : likedProperties) {
            // 城市偏好（计数）
            cityPreferences.merge(property.getCity(), 1, Integer::sum);
            // 房源类型偏好（计数）
            if (property.getPropertyType() != null) {
                typePreferences.merge(property.getPropertyType(), 1, Integer::sum);
            }
            // 价格和卧室数累加（用于计算平均值）
            totalPrice += property.getPrice().doubleValue();
            totalBedrooms += property.getBedrooms();
        }

        // 计算偏好的平均价格和平均卧室数
        double avgPrice = totalPrice / likedProperties.size();
        int avgBedrooms = totalBedrooms / likedProperties.size();

        // 4. 查询所有可用房源
        QueryWrapper<Property> availableQuery = new QueryWrapper<>();
        availableQuery.eq("available", true);
        List<Property> allProperties = propertyMapper.selectList(availableQuery);

        // 过滤用户已交互过的房源（避免重复推荐）
        Set<Long> interactedIds = userInteractions.stream()
                .map(UserPropertyInteraction::getPropertyId)
                .collect(Collectors.toSet());

        // 5. 基于用户偏好为房源打分
        Map<Long, Double> propertyScores = new HashMap<>();
        final double finalAvgPrice = avgPrice;
        final int finalAvgBedrooms = avgBedrooms;

        for (Property property : allProperties) {
            // 跳过已交互的房源
            if (interactedIds.contains(property.getId())) {
                continue;
            }

            double score = 0.0;

            // 5.1 城市偏好得分（权重30%）
            if (cityPreferences.containsKey(property.getCity())) {
                score += cityPreferences.get(property.getCity()) * 0.3;
            }

            // 5.2 房源类型偏好得分（权重20%）
            if (property.getPropertyType() != null && typePreferences.containsKey(property.getPropertyType())) {
                score += typePreferences.get(property.getPropertyType()) * 0.2;
            }

            // 5.3 价格相似度得分（权重25%）：价格越接近用户偏好平均值，得分越高
            double priceDiff = Math.abs(property.getPrice().doubleValue() - finalAvgPrice);
            double priceSimilarity = 1.0 / (1.0 + priceDiff / finalAvgPrice); // 归一化到0-1
            score += priceSimilarity * 0.25;

            // 5.4 卧室数相似度得分（权重15%）：卧室数越接近偏好值，得分越高
            int bedroomDiff = Math.abs(property.getBedrooms() - finalAvgBedrooms);
            double bedroomSimilarity = 1.0 / (1.0 + bedroomDiff); // 归一化到0-1
            score += bedroomSimilarity * 0.15;

            // 5.5 房源评分加分（权重10%）：满分5分归一化到0-1
            score += (property.getRating().doubleValue() / 5.0) * 0.1;

            // 存入房源ID和对应得分
            propertyScores.put(property.getId(), score);
        }

        // 6. 按得分降序排序，取前N个返回
        return propertyScores.entrySet().stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue())) // 降序排序
                .limit(limit)
                .map(entry -> propertyMapper.selectById(entry.getKey())) // 用Mapper查询房源详情
                .filter(Objects::nonNull) // 过滤空值（避免已删除的房源）
                .collect(Collectors.toList());
    }

    /**
     * 计算两个集合的杰卡德相似度
     */
    private double calculateJaccardSimilarity(Set<Long> set1, Set<Long> set2) {
        if (set1.isEmpty() && set2.isEmpty()) return 0.0;
        
        Set<Long> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<Long> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
}
