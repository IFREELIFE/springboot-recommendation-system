package com.recommendation.homestay.service;

import com.recommendation.homestay.dto.PropertyRequest;
import com.recommendation.homestay.entity.Property;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.repository.PropertyRepository;
import com.recommendation.homestay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @CacheEvict(value = "properties", allEntries = true)
    public Property createProperty(PropertyRequest request, Long landlordId) {
        User landlord = userRepository.findById(landlordId)
                .orElseThrow(() -> new RuntimeException("Landlord not found"));

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
        property.setLandlord(landlord);
        property.setAvailable(true);

        return propertyRepository.save(property);
    }

    @Transactional
    @CacheEvict(value = "properties", allEntries = true)
    public Property updateProperty(Long propertyId, PropertyRequest request, Long landlordId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!property.getLandlord().getId().equals(landlordId)) {
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

        return propertyRepository.save(property);
    }

    @Transactional
    @CacheEvict(value = "properties", allEntries = true)
    public void deleteProperty(Long propertyId, Long landlordId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!property.getLandlord().getId().equals(landlordId)) {
            throw new RuntimeException("Unauthorized to delete this property");
        }

        propertyRepository.delete(property);
    }

    @Cacheable(value = "properties", key = "#propertyId")
    public Property getPropertyById(Long propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
    }

    @Cacheable(value = "properties", key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Property> getAllProperties(Pageable pageable) {
        return propertyRepository.findByAvailableTrue(pageable);
    }

    public Page<Property> getPropertiesByLandlord(Long landlordId, Pageable pageable) {
        User landlord = userRepository.findById(landlordId)
                .orElseThrow(() -> new RuntimeException("Landlord not found"));
        return propertyRepository.findByLandlord(landlord, pageable);
    }

    public Page<Property> searchProperties(String city, BigDecimal minPrice, 
                                          BigDecimal maxPrice, Integer bedrooms, 
                                          Pageable pageable) {
        return propertyRepository.searchProperties(city, minPrice, maxPrice, bedrooms, pageable);
    }

    @Cacheable(value = "popularProperties")
    public List<Property> getPopularProperties() {
        return propertyRepository.findTop10ByAvailableTrueOrderByBookingCountDesc();
    }

    @Cacheable(value = "topRatedProperties")
    public List<Property> getTopRatedProperties() {
        return propertyRepository.findTop10ByAvailableTrueOrderByRatingDesc();
    }

    @Transactional
    public void incrementViewCount(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        property.setViewCount(property.getViewCount() + 1);
        propertyRepository.save(property);
    }
}
