package org.mainshop.service;


import org.mainshop.dto.CreateOrderRequest;
import org.mainshop.dto.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse create (UUID userId, CreateOrderRequest createOrderRequest);

    List<OrderResponse> getListOrders(UUID userId);

    OrderResponse getOrder(UUID userId, UUID orderId);

}
