package com.recommendation.homestay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recommendation.homestay.dto.OrderRequest;
import com.recommendation.homestay.entity.Order;
import com.recommendation.homestay.entity.Property;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.mapper.OrderMapper;
import com.recommendation.homestay.mapper.PropertyMapper;
import com.recommendation.homestay.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PropertyMapper propertyMapper;

    @Transactional
    public Order createOrder(OrderRequest request, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Property property = propertyMapper.selectById(request.getPropertyId());
        if (property == null) {
            throw new RuntimeException("Property not found");
        }

        if (!property.getAvailable()) {
            throw new RuntimeException("Property is not available");
        }

        if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }

        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Check-in date cannot be in the past");
        }

        long days = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (days < 1) {
            throw new RuntimeException("Minimum booking is 1 night");
        }

        BigDecimal totalPrice = property.getPrice().multiply(BigDecimal.valueOf(days));

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUserId(userId);
        order.setPropertyId(property.getId());
        order.setCheckInDate(request.getCheckInDate());
        order.setCheckOutDate(request.getCheckOutDate());
        order.setGuestCount(request.getGuestCount());
        order.setTotalPrice(totalPrice);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setRemarks(request.getRemarks());

        orderMapper.insert(order);

        // Update property booking count
        property.setBookingCount(property.getBookingCount() + 1);
        propertyMapper.updateById(property);

        return order;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this order");
        }

        order.setStatus(status);
        orderMapper.updateById(order);
        return order;
    }

    public Order getOrderById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        return order;
    }

    public Order getOrderByNumber(String orderNumber) {
        Order order = orderMapper.findByOrderNumber(orderNumber);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        return order;
    }

    public IPage<Order> getUserOrders(Long userId, int page, int size) {
        Page<Order> pageParam = new Page<>(page + 1, size);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("created_at");
        IPage<Order> orders = orderMapper.selectPage(pageParam, queryWrapper);
        attachProperties(orders);
        return orders;
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED || order.getStatus() == Order.OrderStatus.COMPLETED) {
            throw new RuntimeException("Order already completed or cancelled");
        }

        if (order.getStatus() == Order.OrderStatus.CANCEL_REQUESTED) {
            throw new RuntimeException("Cancellation already requested and pending review");
        }

        boolean hasStarted = order.getCheckInDate().isBefore(LocalDate.now()) || order.getCheckInDate().isEqual(LocalDate.now());
        if (hasStarted) {
            order.setStatus(Order.OrderStatus.CANCEL_REQUESTED);
        } else {
            order.setStatus(Order.OrderStatus.CANCELLED);
        }
        orderMapper.updateById(order);
        return order;
    }

    public IPage<Order> getOrdersForLandlord(Long landlordId, int page, int size) {
        List<Property> properties = propertyMapper.selectList(new QueryWrapper<Property>().eq("landlord_id", landlordId));
        if (properties == null || properties.isEmpty()) {
            Page<Order> empty = new Page<>(page + 1, size);
            empty.setTotal(0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }
        Set<Long> propertyIds = properties.stream().map(Property::getId).collect(Collectors.toSet());
        Page<Order> pageParam = new Page<>(page + 1, size);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("property_id", propertyIds).orderByDesc("created_at");
        IPage<Order> orders = orderMapper.selectPage(pageParam, queryWrapper);
        attachPropertiesAndUsers(orders);
        return orders;
    }

    @Transactional
    public Order reviewCancellation(Long orderId, Long landlordId, boolean approve) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        Property property = propertyMapper.selectById(order.getPropertyId());
        if (property == null) {
            throw new RuntimeException("Property not found for order");
        }
        if (!property.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("Unauthorized to review this order");
        }
        if (order.getStatus() != Order.OrderStatus.CANCEL_REQUESTED) {
            throw new RuntimeException("No cancellation request to review");
        }
        order.setStatus(approve ? Order.OrderStatus.CANCELLED : Order.OrderStatus.CANCEL_REJECTED);
        orderMapper.updateById(order);
        return order;
    }

    private void attachProperties(IPage<Order> orders) {
        Set<Long> propertyIds = new HashSet<>();
        orders.getRecords().forEach(order -> {
            if (order.getPropertyId() != null) {
                propertyIds.add(order.getPropertyId());
            }
        });
        if (!propertyIds.isEmpty()) {
            Map<Long, Property> propertyMap = propertyMapper.selectBatchIds(propertyIds)
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Property::getId, Function.identity(), (existing, replacement) -> existing));
            orders.getRecords().forEach(order -> order.setProperty(propertyMap.get(order.getPropertyId())));
        }
    }

    private void attachPropertiesAndUsers(IPage<Order> orders) {
        Set<Long> propertyIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        orders.getRecords().forEach(order -> {
            if (order.getPropertyId() != null) {
                propertyIds.add(order.getPropertyId());
            }
            if (order.getUserId() != null) {
                userIds.add(order.getUserId());
            }
        });
        Map<Long, Property> propertyMap = Collections.emptyMap();
        if (!propertyIds.isEmpty()) {
            propertyMap = propertyMapper.selectBatchIds(propertyIds)
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Property::getId, Function.identity(), (existing, replacement) -> existing));
        }
        Map<Long, User> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            userMap = userMapper.selectBatchIds(userIds)
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(User::getId, Function.identity(), (existing, replacement) -> existing));
        }
        Map<Long, Property> finalPropertyMap = propertyMap;
        Map<Long, User> finalUserMap = userMap;
        orders.getRecords().forEach(order -> {
            order.setProperty(finalPropertyMap.get(order.getPropertyId()));
            order.setUser(finalUserMap.get(order.getUserId()));
        });
    }
}
