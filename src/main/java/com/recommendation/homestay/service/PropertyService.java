package com.recommendation.homestay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recommendation.homestay.dto.PropertyRequest;
import com.recommendation.homestay.entity.Property;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.mapper.PropertyMapper;
import com.recommendation.homestay.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PropertyService {

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    @CacheEvict(value = "properties", allEntries = true)
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
        return property;
    }

    @Transactional
    @CacheEvict(value = "properties", allEntries = true)
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
        return property;
    }

    @Transactional
    @CacheEvict(value = "properties", allEntries = true)
    public void deleteProperty(Long propertyId, Long landlordId) {
        Property property = propertyMapper.selectById(propertyId);
        if (property == null) {
            throw new RuntimeException("Property not found");
        }

        if (!property.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("Unauthorized to delete this property");
        }

        propertyMapper.deleteById(propertyId);
    }

    @Cacheable(value = "properties", key = "#propertyId")
    public Property getPropertyById(Long propertyId) {
        Property property = propertyMapper.selectById(propertyId);
        if (property == null) {
            throw new RuntimeException("Property not found");
        }
        return property;
    }

    @Cacheable(value = "properties", key = "'all-' + #page + '-' + #size")
    public IPage<Property> getAllProperties(int page, int size) {
        // MyBatis-Plus 正确分页查询（避免返回 LinkedHashMap）
//        IPage<Property> page = new Page<>(pageNum, pageSize);
//        return propertyMapper.selectPage(page, null); // 或自定义查询
        return null;
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
        Page<Property> pageParam = new Page<>(page + 1, size);
        QueryWrapper<Property> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("available", true);
        if (city != null) {
            queryWrapper.eq("city", city);
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
}
