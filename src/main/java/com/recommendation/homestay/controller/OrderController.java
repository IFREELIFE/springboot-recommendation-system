package com.recommendation.homestay.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.recommendation.homestay.dto.ApiResponse;
import com.recommendation.homestay.dto.OrderRequest;
import com.recommendation.homestay.dto.PageResponse;
import com.recommendation.homestay.entity.Order;
import com.recommendation.homestay.security.UserPrincipal;
import com.recommendation.homestay.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
@Tag(name = "Order", description = "订单管理接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @Operation(summary = "创建订单", description = "为当前用户创建新的订单")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Order order = orderService.createOrder(request, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "订单创建成功", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "按ID获取订单", description = "根据订单ID返回订单详情")
    public ResponseEntity<?> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(new ApiResponse(true, "订单获取成功", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "按编号获取订单", description = "根据订单编号查询订单")
    public ResponseEntity<?> getOrderByNumber(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Order order = orderService.getOrderByNumber(orderNumber);
            return ResponseEntity.ok(new ApiResponse(true, "订单获取成功", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/my-orders")
    @Operation(summary = "分页获取我的订单", description = "返回当前用户的订单列表")
    public ResponseEntity<?> getMyOrders(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            IPage<Order> orders = orderService.getUserOrders(currentUser.getId(), page, size);
            PageResponse<Order> pageResponse = PageResponse.fromIPage(orders);
            return ResponseEntity.ok(new ApiResponse(true, "订单列表获取成功", pageResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/landlord")
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
    @Operation(summary = "房东订单列表", description = "查看房东名下房源的所有订单")
    public ResponseEntity<?> getLandlordOrders(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            IPage<Order> orders = orderService.getOrdersForLandlord(currentUser.getId(), page, size);
            PageResponse<Order> pageResponse = PageResponse.fromIPage(orders);
            return ResponseEntity.ok(new ApiResponse(true, "订单列表获取成功", pageResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新订单状态", description = "更新订单的状态，如已支付、取消等")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Order order = orderService.updateOrderStatus(id, status, currentUser.getId());
            return ResponseEntity.ok(new ApiResponse(true, "订单状态更新成功", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "取消订单", description = "取消指定订单")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Order order = orderService.cancelOrder(id, currentUser.getId());
            String message = order.getStatus() == Order.OrderStatus.CANCEL_REQUESTED
                    ? "已提交取消申请，等待房东审核"
                    : "订单已取消";
            return ResponseEntity.ok(new ApiResponse(true, message, order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_ADMIN','LANDLORD','ADMIN')")
    @Operation(summary = "审核退订", description = "房东审核退订请求，approve=true表示通过")
    public ResponseEntity<?> reviewCancellation(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean approve,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Order order = orderService.reviewCancellation(id, currentUser.getId(), approve);
            String message = approve ? "退订已批准" : "退订已拒绝";
            return ResponseEntity.ok(new ApiResponse(true, message, order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
