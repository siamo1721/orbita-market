package org.mainshop.kafka;

public final class KafkaTopics {

    public static final String ORDER_PAYMENT_REQUESTED = "order.payment.requested";
    public static final String ORDER_PAYMENT_COMPLETED = "order.payment.completed";
    public static final String ORDER_PAYMENT_FAILED = "order.payment.failed";

    private KafkaTopics() {
    }
}
