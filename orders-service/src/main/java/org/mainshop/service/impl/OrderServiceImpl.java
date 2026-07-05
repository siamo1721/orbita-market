package org.mainshop.service.impl;

import lombok.AllArgsConstructor;
import org.mainshop.dto.CreateOrderRequest;
import org.mainshop.dto.OrderResponse;
import org.mainshop.entity.Order;
import org.mainshop.enums.ErrorType;
import org.mainshop.enums.OrderStatus;
import org.mainshop.enums.ProductType;
import org.mainshop.exception.BusinessException;
import org.mainshop.mapper.OrderMapper;
import org.mainshop.repository.OrderRepository;
import org.mainshop.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse create(UUID userId, CreateOrderRequest createOrderRequest) {

        if (userId == null) {
            throw new BusinessException(
                    ErrorType.MISSING_USER_ID,
                    "userId отсутствует"
            );
        }

        if (createOrderRequest == null) {
            throw new BusinessException(
                    ErrorType.INVALID_PAYLOAD,
                    "Отсутствует paylod"
            );
        }

        if (createOrderRequest.productType() != ProductType.ARCHIVE) {
            throw new BusinessException(
                    ErrorType.UNKNOWN_PRODUCT_TYPE,
                    "Неверный тип продукта"
            );
        }

        if (createOrderRequest.payload() == null
                || createOrderRequest.payload().get("aoi") == null
                || createOrderRequest.payload().get("capture_date") == null
                || createOrderRequest.payload().get("sensor_type") == null
        ) {
            throw new BusinessException(
                    ErrorType.INVALID_PAYLOAD,
                    "Неверный paylod"
            );
        }

        if (createOrderRequest.price() == null || createOrderRequest.price() <= 0) {
            throw new BusinessException(
                    ErrorType.INVALID_PRICE,
                    "Цена не может быть меньше или равна 0"
            );
        }

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setUserId(userId);
        order.setProductType(createOrderRequest.productType());
        order.setPrice(createOrderRequest.price());
        order.setPayload(createOrderRequest.payload());
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setCreatedAt(Instant.now());

        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Override
    public List<OrderResponse> getListOrders(UUID userId) {
        if (userId == null) {
            throw new BusinessException(
                    ErrorType.MISSING_USER_ID,
                    "userId отсутствует"
            );
        }

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return orderMapper.toResponseList(orders);
    }

    @Override
    public OrderResponse getOrder(UUID userId, UUID orderId) {
        if (userId == null) {
            throw new BusinessException(
                    ErrorType.MISSING_USER_ID,
                    "userId отсутствует"
            );
        }
        if (orderId == null) {
            throw new BusinessException(
                    ErrorType.ORDER_NOT_FOUND,
                    "заказ отсутствует"
            );
        }

        return orderMapper.toResponse(
                orderRepository.findByOrderIdAndUserId(orderId, userId).orElseThrow(
                        () -> new BusinessException(
                                ErrorType.ORDER_NOT_FOUND,
                                "заказ отсутствует"
                        ))
        );
    }
}
