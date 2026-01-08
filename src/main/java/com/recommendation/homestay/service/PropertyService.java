package com.recommendation.homestay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recommendation.homestay.config.UploadUtils;
import com.recommendation.homestay.dto.PropertyRequest;
import com.recommendation.homestay.dto.PropertyResponseDTO;
import com.recommendation.homestay.entity.Property;
import com.recommendation.homestay.entity.PropertyDocument;
import com.recommendation.homestay.entity.User;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    private static final Logger log = LoggerFactory.getLogger(PropertyService.class);

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private UserMapper userMapper;

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
            throw new RuntimeException("Landlord not found");
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
            throw new RuntimeException("Property not found");
        }

        if (!property.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("Unauthorized to update this property");
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
            throw new RuntimeException("Property not found with ID: " + propertyId);
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
            throw new IllegalStateException("Failed to save images", e);
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
            throw new RuntimeException("Property not found");
        }

        if (!property.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("Unauthorized to delete this property");
        }

        propertyMapper.deleteById(propertyId);
        removeFromElasticsearch(propertyId);
    }

    @Cacheable(value = "properties", key = "#propertyId")
    public Property getPropertyById(Long propertyId) {
        Property property = propertyMapper.selectById(propertyId);
        if (property == null) {
            throw new RuntimeException("Property not found");
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
            throw new RuntimeException("Landlord not found");
        }
        Page<Property> pageParam = new Page<>(page + 1, size);
        QueryWrapper<Property> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("landlord_id", landlordId);
        return propertyMapper.selectPage(pageParam, queryWrapper);
    }

    public IPage<Property> searchProperties(String city, BigDecimal minPrice,
                                          BigDecimal maxPrice, Integer bedrooms,
                                          int page, int size) {
        IPage<Property> esPage = searchFromElasticsearch(city, minPrice, maxPrice, bedrooms, page, size);
        if (esPage != null) {
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
            throw new RuntimeException("Property not found");
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
