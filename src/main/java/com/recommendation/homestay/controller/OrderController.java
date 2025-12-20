package com.recommendation.homestay.controller;

import com.recommendation.homestay.dto.ApiResponse;
import com.recommendation.homestay.dto.OrderRequest;
import com.recommendation.homestay.entity.Order;
import com.recommendation.homestay.security.UserPrincipal;
import com.recommendation.homestay.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("isAuthenticated()")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Order order = orderService.createOrder(request, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Order created successfully", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Order order = orderService.getOrderById(id, currentUser.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Order retrieved successfully", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<?> getOrderByNumber(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Order order = orderService.getOrderByOrderNumber(orderNumber, currentUser.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Order retrieved successfully", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders = orderService.getUserOrders(currentUser.getId(), pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Order order = orderService.updateOrderStatus(id, status, currentUser.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Order status updated successfully", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            orderService.cancelOrder(id, currentUser.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Order cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
