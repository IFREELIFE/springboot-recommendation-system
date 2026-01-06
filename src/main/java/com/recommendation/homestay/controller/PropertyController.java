package com.recommendation.homestay.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.recommendation.homestay.dto.ApiResponse;
import com.recommendation.homestay.dto.PageResponse;
import com.recommendation.homestay.dto.PropertyRequest;
import com.recommendation.homestay.entity.Property;
import com.recommendation.homestay.security.UserPrincipal;
import com.recommendation.homestay.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<?> createProperty(
            @Valid @RequestBody PropertyRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Property property = propertyService.createProperty(request, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Property created successfully", property));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<?> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Property property = propertyService.updateProperty(id, request, currentUser.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Property updated successfully", property));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<?> deleteProperty(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            propertyService.deleteProperty(id, currentUser.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Property deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProperty(@PathVariable Long id) {
        try {
            Property property = propertyService.getPropertyById(id);
            propertyService.incrementViewCount(id);
            return ResponseEntity.ok(new ApiResponse(true, "Property retrieved successfully", property));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        try {
            IPage<Property> properties = propertyService.getAllProperties(page, size);
            // 严格转换为自定义DTO，杜绝IPage暴露
            PageResponse<Property> pageResponse = PageResponse.fromIPage(properties);
            return ResponseEntity.ok(new ApiResponse(true, "Success", pageResponse));
        } catch (Exception e) {
            // 关键：打印完整异常栈（包含调用方代码位置）
            e.printStackTrace(); // 本地调试用
            // 生产环境建议用日志框架（如logback/log4j2）
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/landlord/my-properties")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<?> getMyProperties(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            IPage<Property> properties = propertyService.getPropertiesByLandlord(currentUser.getId(), page, size);
            return ResponseEntity.ok(new ApiResponse(true, "Properties retrieved successfully", properties));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    // 确保searchProperties也返回PageResponse（而非IPage）
    @GetMapping("/search")
    public ResponseEntity<?> searchProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            IPage<Property> properties = propertyService.searchProperties(
                    city, minPrice, maxPrice, bedrooms, page, size);
            PageResponse<Property> pageResponse = PageResponse.fromIPage(properties);
            return ResponseEntity.ok(new ApiResponse(true, "Success", pageResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularProperties() {
        try {
            List<Property> properties = propertyService.getPopularProperties();
            return ResponseEntity.ok(new ApiResponse(true, "Popular properties retrieved successfully", properties));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/top-rated")
    public ResponseEntity<?> getTopRatedProperties() {
        try {
            List<Property> properties = propertyService.getTopRatedProperties();
            return ResponseEntity.ok(new ApiResponse(true, "Top rated properties retrieved successfully", properties));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
