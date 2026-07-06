package org.mainshop.service;

import org.mainshop.kafka.event.OrderPaymentRequested;

public interface PaymentProcessingService {

    void processPaymentRequested(OrderPaymentRequested event);
}
