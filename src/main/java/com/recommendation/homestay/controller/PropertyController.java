package com.recommendation.homestay.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.recommendation.homestay.dto.ApiResponse;
import com.recommendation.homestay.dto.PageResponse;
import com.recommendation.homestay.dto.PropertyRequest;
import com.recommendation.homestay.entity.Property;
import com.recommendation.homestay.security.UserPrincipal;
import com.recommendation.homestay.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.max-size-bytes:10485760}")
    private long maxFileSize;

    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));

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

    @PostMapping(value = "/upload-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
    public ResponseEntity<?> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "未选择任何文件"));
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String sizeLimitMsg = String.format("单个文件大小不能超过%.1fMB", maxFileSize / 1024.0 / 1024.0);
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                if (file.getSize() > maxFileSize) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, sizeLimitMsg));
                }
                if (file.getContentType() == null || !file.getContentType().toLowerCase().startsWith("image/")) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "仅支持图片文件上传"));
                }
                String originalFilename = StringUtils.cleanPath(
                        file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
                if (originalFilename.isEmpty()) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "文件名无效"));
                }
                String extension = "";
                int dotIndex = originalFilename.lastIndexOf('.');
                if (dotIndex != -1) {
                    extension = originalFilename.substring(dotIndex);
                }
                String lowerExt = extension.toLowerCase();
                if (!ALLOWED_EXT.contains(lowerExt)) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "不支持的图片格式"));
                }
                byte[] fileBytes = file.getBytes();
                // 简单魔数校验
                if (ImageIO.read(new java.io.ByteArrayInputStream(fileBytes)) == null) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "文件内容不是有效图片"));
                }

                String filename = UUID.randomUUID().toString().replace("-", "") + extension;
                Path targetLocation = uploadPath.resolve(filename).normalize();
                if (!targetLocation.startsWith(uploadPath)) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "非法的文件路径"));
                }
                Files.write(targetLocation, fileBytes, StandardOpenOption.CREATE_NEW);
                imageUrls.add("/api/uploads/" + filename);
            }

            if (imageUrls.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "没有可上传的有效图片"));
            }

            return ResponseEntity.ok(new ApiResponse(true, "图片上传成功", imageUrls));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to upload images: " + e.getMessage()));
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
