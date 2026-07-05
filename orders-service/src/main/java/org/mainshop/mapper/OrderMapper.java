package org.mainshop.mapper;

import org.mainshop.dto.OrderResponse;
import org.mainshop.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {
    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getStatus(),
                order.getProductType(),
                order.getPrice(),
                order.getCreatedAt()
        );
    }

    public List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponse)
                .toList();
    }
}
