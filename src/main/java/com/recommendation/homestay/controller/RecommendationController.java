package com.recommendation.homestay.controller;

import com.recommendation.homestay.dto.ApiResponse;
import com.recommendation.homestay.entity.Property;
import com.recommendation.homestay.security.UserPrincipal;
import com.recommendation.homestay.service.RecommendationService;
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
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<?> getRecommendations(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Property> recommendations = recommendationService.getRecommendations(
                    currentUser.getId(), limit);
            return ResponseEntity.ok(new ApiResponse(true, 
                    "Recommendations retrieved successfully", recommendations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/collaborative")
    public ResponseEntity<?> getCollaborativeRecommendations(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Property> recommendations = recommendationService
                    .getCollaborativeFilteringRecommendations(currentUser.getId(), limit);
            return ResponseEntity.ok(new ApiResponse(true, 
                    "Collaborative recommendations retrieved successfully", recommendations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/content-based")
    public ResponseEntity<?> getContentBasedRecommendations(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Property> recommendations = recommendationService
                    .getContentBasedRecommendations(currentUser.getId(), limit);
            return ResponseEntity.ok(new ApiResponse(true, 
                    "Content-based recommendations retrieved successfully", recommendations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
