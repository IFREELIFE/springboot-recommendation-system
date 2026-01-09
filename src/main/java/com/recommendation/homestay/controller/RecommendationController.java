package com.recommendation.homestay.controller;

import com.recommendation.homestay.dto.ApiResponse;
import com.recommendation.homestay.entity.Property;
import com.recommendation.homestay.security.UserPrincipal;
import com.recommendation.homestay.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("isAuthenticated()")
@Tag(name = "Recommendation", description = "用户个性化推荐接口")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    @Operation(summary = "获取综合推荐", description = "基于多种算法为当前用户返回推荐房源")
    public ResponseEntity<?> getRecommendations(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Property> recommendations = recommendationService.getRecommendations(
                    currentUser.getId(), limit);
            return ResponseEntity.ok(new ApiResponse(true, 
                    "推荐列表获取成功", recommendations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/collaborative")
    @Operation(summary = "协同过滤推荐", description = "基于相似用户行为的推荐列表")
    public ResponseEntity<?> getCollaborativeRecommendations(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Property> recommendations = recommendationService
                    .getCollaborativeFilteringRecommendations(currentUser.getId(), limit);
            return ResponseEntity.ok(new ApiResponse(true, 
                    "协同过滤推荐获取成功", recommendations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/content-based")
    @Operation(summary = "内容相似推荐", description = "根据房源内容相似度生成推荐")
    public ResponseEntity<?> getContentBasedRecommendations(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Property> recommendations = recommendationService
                    .getContentBasedRecommendations(currentUser.getId(), limit);
            return ResponseEntity.ok(new ApiResponse(true, 
                    "内容相似推荐获取成功", recommendations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
