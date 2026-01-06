package com.recommendation.homestay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.recommendation.homestay.entity.Property;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.entity.UserPropertyInteraction;
import com.recommendation.homestay.mapper.PropertyMapper;
import com.recommendation.homestay.mapper.UserPropertyInteractionMapper;
import com.recommendation.homestay.mapper.UserMapper;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
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
     * Hybrid recommendation: Combines collaborative filtering and content-based filtering
     */
    @Cacheable(value = "recommendations", key = "#userId")
    public List<Property> getRecommendations(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get recommendations from both algorithms
        List<Property> collaborativeRecommendations = getCollaborativeFilteringRecommendations(userId, limit * 2);
        List<Property> contentBasedRecommendations = getContentBasedRecommendations(userId, limit * 2);

        // Merge and score
        Map<Long, Double> propertyScores = new HashMap<>();

        // Weight: 60% collaborative, 40% content-based
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

        // Sort by score and return top results
        return propertyScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> propertyRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Collaborative Filtering: User-based recommendation
     * Finds similar users and recommends properties they liked
     */
    public List<Property> getCollaborativeFilteringRecommendations(Long userId, int limit) {
        List<UserPropertyInteraction> userInteractions = interactionRepository.findByUser(
            userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"))
        );

        if (userInteractions.isEmpty()) {
            // Cold start: return popular properties
            return propertyRepository.findTop10ByAvailableTrueOrderByBookingCountDesc()
                    .stream().limit(limit).collect(Collectors.toList());
        }

        // Get properties user has interacted with
        Set<Long> interactedPropertyIds = userInteractions.stream()
                .map(i -> i.getProperty().getId())
                .collect(Collectors.toSet());

        // Find similar users based on common property interactions
        List<UserPropertyInteraction> allInteractions = interactionRepository.findAll();
        
        Map<Long, Set<Long>> userPropertyMap = new HashMap<>();
        for (UserPropertyInteraction interaction : allInteractions) {
            userPropertyMap.computeIfAbsent(interaction.getUser().getId(), k -> new HashSet<>())
                    .add(interaction.getProperty().getId());
        }

        // Calculate similarity scores with other users
        Map<Long, Double> similarityScores = new HashMap<>();
        Set<Long> currentUserProperties = userPropertyMap.get(userId);
        
        if (currentUserProperties == null) {
            return propertyRepository.findTop10ByAvailableTrueOrderByBookingCountDesc()
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

        // Get recommendations from similar users
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

        // Get top recommendations
        return recommendationScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> propertyRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .filter(Property::getAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Content-Based Filtering: Recommend properties similar to what user has liked
     */
    public List<Property> getContentBasedRecommendations(Long userId, int limit) {
        List<UserPropertyInteraction> userInteractions = interactionRepository.findByUser(
            userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"))
        );

        if (userInteractions.isEmpty()) {
            // Cold start: return top-rated properties
            return propertyRepository.findTop10ByAvailableTrueOrderByRatingDesc()
                    .stream().limit(limit).collect(Collectors.toList());
        }

        // Get properties user has positively interacted with
        List<Property> likedProperties = userInteractions.stream()
                .filter(i -> i.getType() == UserPropertyInteraction.InteractionType.FAVORITE || 
                            i.getType() == UserPropertyInteraction.InteractionType.BOOK ||
                            (i.getRating() != null && i.getRating() >= 4))
                .map(UserPropertyInteraction::getProperty)
                .collect(Collectors.toList());

        if (likedProperties.isEmpty()) {
            return propertyRepository.findTop10ByAvailableTrueOrderByRatingDesc()
                    .stream().limit(limit).collect(Collectors.toList());
        }

        // Get user's preferences from liked properties
        Map<String, Integer> cityPreferences = new HashMap<>();
        Map<String, Integer> typePreferences = new HashMap<>();
        double avgPrice = 0;
        int avgBedrooms = 0;

        for (Property property : likedProperties) {
            cityPreferences.merge(property.getCity(), 1, Integer::sum);
            if (property.getPropertyType() != null) {
                typePreferences.merge(property.getPropertyType(), 1, Integer::sum);
            }
            avgPrice += property.getPrice().doubleValue();
            avgBedrooms += property.getBedrooms();
        }

        avgPrice /= likedProperties.size();
        avgBedrooms /= likedProperties.size();

        // Get all available properties
        List<Property> allProperties = propertyRepository.findByAvailableTrue(null).getContent();
        
        // Filter out already interacted properties
        Set<Long> interactedIds = userInteractions.stream()
                .map(i -> i.getProperty().getId())
                .collect(Collectors.toSet());

        // Score properties based on similarity to user preferences
        Map<Long, Double> scores = new HashMap<>();
        final double finalAvgPrice = avgPrice;
        final int finalAvgBedrooms = avgBedrooms;

        for (Property property : allProperties) {
            if (interactedIds.contains(property.getId())) continue;

            double score = 0.0;

            // City preference (30%)
            if (cityPreferences.containsKey(property.getCity())) {
                score += cityPreferences.get(property.getCity()) * 0.3;
            }

            // Property type preference (20%)
            if (property.getPropertyType() != null && typePreferences.containsKey(property.getPropertyType())) {
                score += typePreferences.get(property.getPropertyType()) * 0.2;
            }

            // Price similarity (25%)
            double priceDiff = Math.abs(property.getPrice().doubleValue() - finalAvgPrice);
            double priceScore = 1.0 / (1.0 + priceDiff / finalAvgPrice);
            score += priceScore * 0.25;

            // Bedroom similarity (15%)
            int bedroomDiff = Math.abs(property.getBedrooms() - finalAvgBedrooms);
            double bedroomScore = 1.0 / (1.0 + bedroomDiff);
            score += bedroomScore * 0.15;

            // Rating bonus (10%)
            score += property.getRating().doubleValue() / 5.0 * 0.1;

            scores.put(property.getId(), score);
        }

        // Return top scored properties
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> propertyRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Calculate Jaccard similarity between two sets
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
