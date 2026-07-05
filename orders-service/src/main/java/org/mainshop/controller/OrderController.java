package org.mainshop.controller;

import lombok.AllArgsConstructor;
import org.mainshop.dto.CreateOrderRequest;
import org.mainshop.dto.OrderResponse;
import org.mainshop.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController()
@RequestMapping("api/v1/orders/")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public OrderResponse create(@RequestHeader(value = "X-User-Id", required = false) UUID userId,
                                @RequestBody CreateOrderRequest orderRequest) {
        return orderService.create(userId, orderRequest);
    }

    @GetMapping("/orders")
    public List<OrderResponse> getListOrders(@RequestHeader(value = "X-User-Id", required = false) UUID userId){
        return orderService.getListOrders(userId);
    }

    @GetMapping("/orders/{orderId}")
    public OrderResponse getOrder (@RequestHeader(value = "X-User-Id", required = false) UUID userId,
                                   @PathVariable UUID orderId){
        return orderService.getOrder(userId, orderId);
    }
}
