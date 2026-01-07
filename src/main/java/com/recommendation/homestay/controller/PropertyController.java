package com.recommendation.homestay.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.recommendation.homestay.config.UploadUtils;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_FILES = 10;
    private static final Logger log = LoggerFactory.getLogger(PropertyController.class);

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
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

    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
    public ResponseEntity<?> uploadPropertyImages(@RequestParam("files") MultipartFile[] files,
                                                  @RequestParam(value = "propertyId", required = false) Long propertyId) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "请至少选择一张图片"));
        }
        if (files.length > MAX_FILES) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "一次最多上传" + MAX_FILES + "张图片"));
        }

        try {
            Path uploadDir = UploadUtils.getUploadDir().normalize();
            try {
                Files.createDirectories(uploadDir);
            } catch (SecurityException se) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "无法创建上传目录"));
            }

            List<String> imageUrls = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.isBlank()) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false, "文件名无效"));
                }
                originalFilename = StringUtils.cleanPath(originalFilename);

                String extension = "";
                int dotIndex = originalFilename.lastIndexOf(".");
                if (dotIndex != -1) {
                    extension = originalFilename.substring(dotIndex);
                }
                if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false, "仅支持上传图片格式：" + ALLOWED_EXTENSIONS));
                }
                if (file.getSize() > MAX_FILE_SIZE) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false, "单个文件大小不能超过10MB"));
                }
                String filename = UUID.randomUUID().toString() + extension;
                Path targetPath = uploadDir.resolve(filename).normalize();
                if (!targetPath.startsWith(uploadDir)) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false, "文件路径无效"));
                }
                file.transferTo(targetPath.toFile());
                imageUrls.add("/api/uploads/" + filename);
            }

            if (imageUrls.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "上传的图片无效"));
            }

            if (propertyId != null) {
                try {
                    propertyService.appendImages(propertyId, imageUrls);
                } catch (Exception e) {
                    log.error("Failed to append images to property {}", propertyId, e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ApiResponse(false, "图片保存到房源失败，请稍后重试"));
                }
            }

            return ResponseEntity.ok(new ApiResponse(true, "图片上传成功", imageUrls));
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "图片上传失败，请稍后重试"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
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
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
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
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
    public ResponseEntity<?> getMyProperties(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            IPage<Property> properties = propertyService.getPropertiesByLandlord(currentUser.getId(), page, size);
            PageResponse<Property> pageResponse = PageResponse.fromIPage(properties);
            return ResponseEntity.ok(new ApiResponse(true, "Properties retrieved successfully", pageResponse));
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
