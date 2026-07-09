# Контракты событий Kafka

Зафиксированные имена топиков и форматы сообщений (ТЗ п. 4.2).

## Топики

| Топик | Направление | Назначение |
|-------|-------------|------------|
| `order.payment.requested` | Orders → Payments | запрос списания за заказ |
| `order.payment.completed` | Payments → Orders | списание успешно |
| `order.payment.failed` | Payments → Orders | списание не удалось |

---

## OrderPaymentRequested

Публикует **Orders Service** (через transactional outbox).

```json
{
  "event_id": "550e8400-e29b-41d4-a716-446655440000",
  "order_id": "550e8400-e29b-41d4-a716-446655440001",
  "user_id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "amount": 120,
  "occurred_at": "2026-07-07T15:00:00Z"
}
```

---

## OrderPaymentCompleted

Публикует **Payments Service** (через transactional outbox).

```json
{
  "event_id": "550e8400-e29b-41d4-a716-446655440002",
  "order_id": "550e8400-e29b-41d4-a716-446655440001",
  "user_id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "amount": 120,
  "new_balance": 880
}
```

---

## OrderPaymentFailed

Публикует **Payments Service**.

```json
{
  "event_id": "550e8400-e29b-41d4-a716-446655440003",
  "order_id": "550e8400-e29b-41d4-a716-446655440001",
  "user_id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "reason": "INSUFFICIENT_BALANCE"
}
```

Возможные значения `reason`:

- `INSUFFICIENT_BALANCE`
- `INVALID_PAYLOAD`
- `INVALID_PRICE`

---

## Гарантии доставки (ТЗ п. 4.3)

| Проблема | Решение |
|----------|---------|
| Заказ сохранён, Kafka недоступна | Transactional Outbox в Orders |
| Ответ Payments не ушёл в Kafka | Transactional Outbox в Payments |
| Повторное событие с тем же `event_id` | Inbox (Orders, Payments) |
| Повторный запрос оплаты с тем же `order_id` | `processed_payments` (UNIQUE order_id) |
| Параллельные списания | `@Version` на `Account` (optimistic locking) |

---

## Consumer groups

| Сервис | group-id |
|--------|----------|
| Orders | `orders-service` |
| Payments | `payments-service` |
