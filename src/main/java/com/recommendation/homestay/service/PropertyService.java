package com.recommendation.homestay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recommendation.homestay.config.UploadUtils;
import com.recommendation.homestay.dto.PropertyOccupancyDTO;
import com.recommendation.homestay.dto.PageResponse;
import com.recommendation.homestay.dto.PropertyRequest;
import com.recommendation.homestay.dto.PropertyResponseDTO;
import com.recommendation.homestay.entity.Order;
import com.recommendation.homestay.entity.Property;
import com.recommendation.homestay.entity.PropertyDocument;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.mapper.OrderMapper;
import com.recommendation.homestay.mapper.PropertyMapper;
import com.recommendation.homestay.mapper.UserMapper;
import com.recommendation.homestay.repository.PropertyDocumentRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    private static final Logger log = LoggerFactory.getLogger(PropertyService.class);

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired(required = false)
    private PropertyDocumentRepository propertyDocumentRepository;

    @Transactional
    @CacheEvict(value = {"popularProperties", "topRatedProperties"}, allEntries = true)
    public Property createProperty(PropertyRequest request, Long landlordId) {
        User landlord = userMapper.selectById(landlordId);
        if (landlord == null) {
            throw new RuntimeException("未找到房东");
        }

        Property property = new Property();
        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setCity(request.getCity());
        property.setDistrict(request.getDistrict());
        property.setAddress(request.getAddress());
        property.setPrice(request.getPrice());
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        property.setMaxGuests(request.getMaxGuests());
        property.setPropertyType(request.getPropertyType());
        property.setAmenities(request.getAmenities());
        property.setImages(request.getImages());
        property.setLandlordId(landlordId);
        property.setAvailable(true);

        propertyMapper.insert(property);
        indexToElasticsearch(property);
        return property;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "properties", key = "#propertyId"),
            @CacheEvict(value = {"popularProperties", "topRatedProperties"}, allEntries = true)
    })
    public Property updateProperty(Long propertyId, PropertyRequest request, Long landlordId) {
        Property property = propertyMapper.selectById(propertyId);
        if (property == null) {
            throw new RuntimeException("未找到房源");
        }

        if (!property.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("无权更新该房源");
        }

        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setCity(request.getCity());
        property.setDistrict(request.getDistrict());
        property.setAddress(request.getAddress());
        property.setPrice(request.getPrice());
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        property.setMaxGuests(request.getMaxGuests());
        property.setPropertyType(request.getPropertyType());
        property.setAmenities(request.getAmenities());
        property.setImages(request.getImages());

        propertyMapper.updateById(property);
        indexToElasticsearch(property);
        return property;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "properties", key = "#propertyId"),
            @CacheEvict(value = {"popularProperties", "topRatedProperties"}, allEntries = true)
    })
    public Property appendImages(Long propertyId, List<String> newImages) {
        Property property = propertyMapper.selectById(propertyId);
        if (property == null) {
            throw new RuntimeException("未找到指定ID的房源：" + propertyId);
        }
        List<String> merged = new ArrayList<>();
        try {
            if (property.getImages() != null && !property.getImages().isBlank()) {
                merged.addAll(objectMapper.readValue(property.getImages(), new TypeReference<List<String>>() {
                }));
            }
        } catch (Exception e) {
            log.warn("Failed to parse existing images for property {}", propertyId, e);
        }
        merged.addAll(newImages);
        try {
            property.setImages(objectMapper.writeValueAsString(merged));
        } catch (Exception e) {
            throw new IllegalStateException("保存图片失败", e);
        }
        propertyMapper.updateById(property);
        return property;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "properties", key = "#propertyId"),
            @CacheEvict(value = {"popularProperties", "topRatedProperties"}, allEntries = true)
    })
    public void deleteProperty(Long propertyId, Long landlordId) {
        Property property = propertyMapper.selectById(propertyId);
        if (property == null) {
            throw new RuntimeException("未找到房源");
        }

        if (!property.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("无权删除该房源");
        }

        propertyMapper.deleteById(propertyId);
        removeFromElasticsearch(propertyId);
    }

    @Cacheable(value = "properties", key = "#propertyId")
    public Property getPropertyById(Long propertyId) {
        Property property = propertyMapper.selectById(propertyId);
        if (property == null) {
            throw new RuntimeException("未找到房源");
        }
        return property;
    }

    public IPage<Property> getAllProperties(int page, int size) {
        Page<Property> pageParam = new Page<>(page + 1, size);
        QueryWrapper<Property> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("available", true);
        return propertyMapper.selectPage(pageParam, queryWrapper);
    }

    public IPage<Property> getPropertiesByLandlord(Long landlordId, int page, int size) {
        User landlord = userMapper.selectById(landlordId);
        if (landlord == null) {
            throw new RuntimeException("未找到房东");
        }
        Page<Property> pageParam = new Page<>(page + 1, size);
        QueryWrapper<Property> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("landlord_id", landlordId);
        return propertyMapper.selectPage(pageParam, queryWrapper);
    }

    public PageResponse<PropertyOccupancyDTO> getPropertyOccupancy(Long landlordId, int page, int size) {
        IPage<Property> properties = getPropertiesByLandlord(landlordId, page, size);

        PageResponse<PropertyOccupancyDTO> response = new PageResponse<>();
        response.setTotal(properties.getTotal());
        response.setSize(properties.getSize());
        response.setCurrent(properties.getCurrent());
        response.setPages(properties.getPages());

        List<Property> records = properties.getRecords();
        if (records == null || records.isEmpty()) {
            response.setRecords(new ArrayList<>());
            return response;
        }

        List<Long> propertyIds = records.stream()
                .map(Property::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, OccupancyCounter> occupancyMap = loadActiveOccupancy(propertyIds);

        List<PropertyOccupancyDTO> dtoList = records.stream().map(p -> {
            OccupancyCounter counter = occupancyMap.getOrDefault(p.getId(), new OccupancyCounter());
            int bedrooms = Optional.ofNullable(p.getBedrooms()).orElse(0);
            int occupiedRooms = counter.getOccupiedRooms();
            int remainingRooms = Math.max(bedrooms - occupiedRooms, 0);

            PropertyOccupancyDTO dto = new PropertyOccupancyDTO();
            dto.setId(p.getId());
            dto.setTitle(p.getTitle());
            dto.setCity(p.getCity());
            dto.setAddress(p.getAddress());
            dto.setPrice(p.getPrice());
            dto.setBedrooms(p.getBedrooms());
            dto.setMaxGuests(p.getMaxGuests());
            dto.setPropertyType(p.getPropertyType());
            dto.setBookingCount(p.getBookingCount());
            dto.setOccupiedRooms(occupiedRooms);
            dto.setRemainingRooms(remainingRooms);
            dto.setActiveGuests(counter.getActiveGuests());
            return dto;
        }).collect(Collectors.toList());

        response.setRecords(dtoList);
        return response;
    }

    private Map<Long, OccupancyCounter> loadActiveOccupancy(List<Long> propertyIds) {
        if (propertyIds == null || propertyIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LocalDate today = LocalDate.now();
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("property_id", propertyIds);
        queryWrapper.in("status", Arrays.asList(
                Order.OrderStatus.PENDING.name(),
                Order.OrderStatus.CONFIRMED.name(),
                Order.OrderStatus.CANCEL_REQUESTED.name()
        ));
        queryWrapper.le("check_in_date", today);
        queryWrapper.gt("check_out_date", today);

        List<Order> activeOrders = orderMapper.selectList(queryWrapper);
        Map<Long, OccupancyCounter> stats = new HashMap<>();
        for (Order order : activeOrders) {
            OccupancyCounter counter = stats.computeIfAbsent(order.getPropertyId(), key -> new OccupancyCounter());
            counter.incrementOccupiedRooms();
            counter.addGuests(Optional.ofNullable(order.getGuestCount()).orElse(0));
        }
        return stats;
    }

    private static class OccupancyCounter {
        private int occupiedRooms;
        private int activeGuests;

        void incrementOccupiedRooms() {
            this.occupiedRooms++;
        }

        void addGuests(int guests) {
            this.activeGuests += guests;
        }

        int getOccupiedRooms() {
            return occupiedRooms;
        }

        int getActiveGuests() {
            return activeGuests;
        }
    }

    public IPage<Property> searchProperties(String city, BigDecimal minPrice,
                                          BigDecimal maxPrice, Integer bedrooms,
                                          int page, int size) {
        IPage<Property> esPage = searchFromElasticsearch(city, minPrice, maxPrice, bedrooms, page, size);
        if (esPage != null && esPage.getTotal() > 0) {
            return esPage;
        }

        Page<Property> pageParam = new Page<>(page + 1, size);
        QueryWrapper<Property> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("available", true);
        if (city != null) {
            String normalizedCity = normalizeCity(city);
            queryWrapper.apply("LOWER(city) = {0}", normalizedCity);
        }
        if (minPrice != null) {
            queryWrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            queryWrapper.le("price", maxPrice);
        }
        if (bedrooms != null) {
            queryWrapper.ge("bedrooms", bedrooms);
        }
        return propertyMapper.selectPage(pageParam, queryWrapper);
    }

    private IPage<Property> searchFromElasticsearch(String city, BigDecimal minPrice,
                                                    BigDecimal maxPrice, Integer bedrooms,
                                                    int page, int size) {
        if (elasticsearchOperations == null || propertyDocumentRepository == null) {
            return null;
        }
        try {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .filter(QueryBuilders.termQuery("available", true));

            if (city != null) {
                boolQuery.filter(QueryBuilders.termQuery("city", normalizeCity(city)));
            }
            if (minPrice != null) {
                boolQuery.filter(QueryBuilders.rangeQuery("price").gte(minPrice));
            }
            if (maxPrice != null) {
                boolQuery.filter(QueryBuilders.rangeQuery("price").lte(maxPrice));
            }
            if (bedrooms != null) {
                boolQuery.filter(QueryBuilders.rangeQuery("bedrooms").gte(bedrooms));
            }

            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(PageRequest.of(page, size));

            SearchHits<PropertyDocument> hits = elasticsearchOperations.search(
                    queryBuilder.build(), PropertyDocument.class);

            if (hits == null || hits.getTotalHits() == 0) {
                Page<Property> empty = new Page<>(page + 1, size);
                empty.setTotal(0);
                empty.setRecords(new ArrayList<>());
                return empty;
            }

            List<Long> ids = hits.getSearchHits()
                    .stream()
                    .map(h -> h.getContent().getId())
                    .collect(Collectors.toList());

            List<Property> records = propertyMapper.selectBatchIds(ids);
            Map<Long, Integer> order = new HashMap<>();
            int idx = 0;
            for (Long id : ids) {
                order.put(id, idx++);
            }
            int missingCount = ids.size() - records.size();
            if (missingCount > 0) {
                List<Long> dbIds = records.stream().map(Property::getId).collect(Collectors.toList());
                List<Long> missingIds = ids.stream()
                        .filter(id -> !dbIds.contains(id))
                        .collect(Collectors.toList());
                log.warn("Elasticsearch returned {} ids not found in DB (sample: {}) , consider re-syncing index",
                        missingCount, missingIds);
            }

            records = records.stream()
                    .filter(p -> order.containsKey(p.getId()))
                    .sorted(Comparator.comparingInt(p -> order.get(p.getId())))
                    .collect(Collectors.toList());

            Page<Property> result = new Page<>(page + 1, size);
            result.setRecords(records);
            result.setTotal(hits.getTotalHits());
            return result;
        } catch (Exception e) {
            log.warn("Elasticsearch search failed, fallback to DB search", e);
            return null;
        }
    }

    @Cacheable(value = "popularProperties")
    public List<Property> getPopularProperties() {
        return propertyMapper.findTop10ByAvailableTrueOrderByBookingCountDesc();
    }

    @Cacheable(value = "topRatedProperties")
    public List<Property> getTopRatedProperties() {
        return propertyMapper.findTop10ByAvailableTrueOrderByRatingDesc();
    }

    @Transactional
    public void incrementViewCount(Long propertyId) {
        int result = propertyMapper.incrementViewCount(propertyId);
        if (result == 0) {
            throw new RuntimeException("未找到房源");
        }
    }

    private void indexToElasticsearch(Property property) {
        if (propertyDocumentRepository == null) {
            return;
        }
        try {
            propertyDocumentRepository.save(toDocument(property));
        } catch (Exception e) {
            log.warn("Failed to index property {} to Elasticsearch", property.getId(), e);
        }
    }

    private void removeFromElasticsearch(Long propertyId) {
        if (propertyDocumentRepository == null) {
            return;
        }
        try {
            propertyDocumentRepository.deleteById(propertyId);
        } catch (Exception e) {
            log.warn("Failed to remove property {} from Elasticsearch", propertyId, e);
        }
    }

    private PropertyDocument toDocument(Property property) {
        PropertyDocument doc = new PropertyDocument();
        doc.setId(property.getId());
        doc.setTitle(property.getTitle());
        doc.setDescription(property.getDescription());
        doc.setCity(normalizeCity(property.getCity()));
        doc.setPrice(property.getPrice());
        doc.setBedrooms(property.getBedrooms());
        doc.setAvailable(property.getAvailable());
        return doc;
    }

    private String normalizeCity(String city) {
        return city == null ? null : city.toLowerCase(Locale.ROOT);
    }

    public PropertyResponseDTO toResponseDTO(Property property) {
        PropertyResponseDTO dto = new PropertyResponseDTO();
        dto.setId(property.getId());
        dto.setTitle(property.getTitle());
        dto.setDescription(property.getDescription());
        dto.setCity(property.getCity());
        dto.setDistrict(property.getDistrict());
        dto.setAddress(property.getAddress());
        dto.setPrice(property.getPrice());
        dto.setBedrooms(property.getBedrooms());
        dto.setBathrooms(property.getBathrooms());
        dto.setMaxGuests(property.getMaxGuests());
        dto.setPropertyType(property.getPropertyType());
        dto.setAmenities(property.getAmenities());
        dto.setAvailable(property.getAvailable());
        dto.setLandlordId(property.getLandlordId());
        dto.setRating(property.getRating());
        dto.setReviewCount(property.getReviewCount());
        dto.setViewCount(property.getViewCount());
        dto.setBookingCount(property.getBookingCount());
        dto.setCreatedAt(property.getCreatedAt());
        dto.setUpdatedAt(property.getUpdatedAt());
        dto.setImages(property.getImages());

        List<String> base64List = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        try {
            if (property.getImages() != null && !property.getImages().isBlank()) {
                paths.addAll(objectMapper.readValue(property.getImages(), new TypeReference<List<String>>() {
                }));
            }
        } catch (Exception e) {
            log.warn("Failed to parse images json for property {}", property.getId(), e);
        }
        for (String p : paths) {
            String filename = p.replaceFirst("^/api/uploads/", "")
                    .replaceFirst("^/uploads/", "")
                    .replaceFirst("^uploads/", "");
            Path resolved = UploadUtils.getUploadDir().resolve(filename).normalize();
            if (!resolved.startsWith(UploadUtils.getUploadDir())) {
                continue;
            }
            try {
                byte[] bytes = Files.readAllBytes(resolved);
                base64List.add(Base64.getEncoder().encodeToString(bytes));
            } catch (Exception e) {
                log.warn("Failed to read image file {}", resolved, e);
            }
        }
        dto.setImagesBase64(base64List);
        return dto;
    }
}
