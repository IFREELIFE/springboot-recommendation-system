package com.recommendation.homestay.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.recommendation.homestay.config.UploadUtils;
import com.recommendation.homestay.dto.ApiResponse;
import com.recommendation.homestay.dto.PageResponse;
import com.recommendation.homestay.dto.PropertyOccupancyDTO;
import com.recommendation.homestay.dto.PropertyRequest;
import com.recommendation.homestay.dto.PropertyResponseDTO;
import com.recommendation.homestay.entity.Property;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recommendation.homestay.security.UserPrincipal;
import com.recommendation.homestay.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.http.MediaType;

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
@Tag(name = "Property", description = "房源管理与查询接口")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_FILES = 10;
    private static final Logger log = LoggerFactory.getLogger(PropertyController.class);

    /**
     * 创建房源，接收 JSON 参数。
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
    @Operation(summary = "创建房源", description = "以JSON请求创建房源信息")
    public ResponseEntity<?> createProperty(
            @Valid @RequestBody PropertyRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Property property = propertyService.createProperty(request, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "房源创建成功", property));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 创建房源并同时上传多张图片。
     */
    @PostMapping(value = "/with-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
    @Operation(summary = "创建房源并上传图片", description = "提交房源信息和图片文件完成创建")
    public ResponseEntity<?> createPropertyWithImages(
            @RequestPart("request") @Valid PropertyRequest request,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "请至少上传一张图片"));
        }
        if (files.length > MAX_FILES) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "一次最多上传" + MAX_FILES + "张图片"));
        }
        try {
            Path uploadDir = UploadUtils.getUploadDir().normalize();
            Files.createDirectories(uploadDir);

            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.isBlank()) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "文件名无效"));
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

            request.setImages(objectMapper.writeValueAsString(imageUrls));
            Property property = propertyService.createProperty(request, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "房源创建成功", property));
        } catch (Exception e) {
            log.error("创建房源并上传图片失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "创建房源失败，请稍后重试"));
        }
    }

    /**
     * 单独上传房源图片，可选附加到已有房源。
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
    @Operation(summary = "上传房源图片", description = "单独上传房源图片，可选关联已有房源")
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

    /**
     * 根据ID更新房源基本信息。
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
    @Operation(summary = "更新房源", description = "根据ID更新房源基本信息")
    public ResponseEntity<?> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Property property = propertyService.updateProperty(id, request, currentUser.getId());
            return ResponseEntity.ok(new ApiResponse(true, "房源更新成功", property));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 根据ID删除房源。
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
    @Operation(summary = "删除房源", description = "根据ID删除房源")
    public ResponseEntity<?> deleteProperty(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            propertyService.deleteProperty(id, currentUser.getId());
            return ResponseEntity.ok(new ApiResponse(true, "房源删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 获取房源详情并增加浏览量。
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取房源详情", description = "根据ID返回房源信息并增加浏览量")
    public ResponseEntity<?> getProperty(@PathVariable Long id) {
        try {
            Property property = propertyService.getPropertyById(id);
            propertyService.incrementViewCount(id);
            PropertyResponseDTO dto = propertyService.toResponseDTO(property);
            return ResponseEntity.ok(new ApiResponse(true, "房源获取成功", dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 分页获取房源列表。
     */
    @GetMapping
    @Operation(summary = "分页获取房源列表", description = "按分页参数返回所有房源")
    public ResponseEntity<?> getAllProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        try {
            IPage<Property> properties = propertyService.getAllProperties(page, size);
            // 严格转换为自定义DTO，杜绝IPage暴露
            PageResponse<Property> pageResponse = PageResponse.fromIPage(properties);
            return ResponseEntity.ok(new ApiResponse(true, "成功", pageResponse));
        } catch (Exception e) {
            // 关键：打印完整异常栈（包含调用方代码位置）
            e.printStackTrace(); // 本地调试用
            // 生产环境建议用日志框架（如logback/log4j2）
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 房东查看自己的房源列表。
     */
    @GetMapping("/landlord/my-properties")
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
    @Operation(summary = "获取我的房源", description = "房东查看自己发布的房源列表")
    public ResponseEntity<?> getMyProperties(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            IPage<Property> properties = propertyService.getPropertiesByLandlord(currentUser.getId(), page, size);
            PageResponse<Property> pageResponse = PageResponse.fromIPage(properties);
            return ResponseEntity.ok(new ApiResponse(true, "房东房源列表获取成功", pageResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 房东查看房源的入住人数和剩余房间。
     */
    @GetMapping("/landlord/occupancy")
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
    @Operation(summary = "房源入住与剩余房间", description = "房东查看房源的入住人数和剩余房间")
    public ResponseEntity<?> getPropertyOccupancy(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageResponse<PropertyOccupancyDTO> pageResponse = propertyService.getPropertyOccupancy(currentUser.getId(), page, size);
            return ResponseEntity.ok(new ApiResponse(true, "房源入住情况获取成功", pageResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 按城市、价格、卧室数等条件搜索房源。
     */
    @GetMapping("/search")
    @Operation(summary = "搜索房源", description = "按城市、价格、卧室数等条件搜索房源")
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
            return ResponseEntity.ok(new ApiResponse(true, "成功", pageResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 获取热门房源列表（高浏览量）。
     */
    @GetMapping("/popular")
    @Operation(summary = "热门房源", description = "获取浏览量较高的房源列表")
    public ResponseEntity<?> getPopularProperties() {
        try {
            List<Property> properties = propertyService.getPopularProperties();
            return ResponseEntity.ok(new ApiResponse(true, "热门房源获取成功", properties));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 获取高评分房源列表。
     */
    @GetMapping("/top-rated")
    @Operation(summary = "高评分房源", description = "获取评分较高的房源列表")
    public ResponseEntity<?> getTopRatedProperties() {
        try {
            List<Property> properties = propertyService.getTopRatedProperties();
            return ResponseEntity.ok(new ApiResponse(true, "高评分房源获取成功", properties));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
