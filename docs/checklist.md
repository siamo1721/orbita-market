# Чек-лист сценариев (ТЗ п. 7.1)

Проверка через **API Gateway** (`http://localhost:8080`), инструмент — curl.

Дата последней проверки: **07.07.2026**

| # | Сценарий | Ожидаемый результат | Статус |
|---|----------|---------------------|--------|
| 1 | Happy path: счёт → пополнение 1000 → заказ 120 | `PAID`, баланс **880** | ✅ |
| 2 | Недостаточно средств: баланс 50, заказ 120 | `PAYMENT_FAILED`, баланс **50**, `failure_reason: INSUFFICIENT_BALANCE` | ✅ |
| 3 | Повтор `OrderPaymentRequested` с тем же `order_id` | баланс не списан повторно (`processed_payments` — 1 строка на order_id) | ✅ |
| 4 | Два заказа по 400 при балансе 1000 | оба `PAID`, баланс **200**, баланс ≥ 0 | ✅ |
| 5 | Повторный `POST /accounts` для того же `user_id` | `409 ACCOUNT_ALREADY_EXISTS`, дубликат счёта не создаётся | ✅ |

---

## Как воспроизвести

### Сценарий 1

```bash
export USER_ID="a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
# create → top-up 1000 → order 120 → sleep 3 → GET order → PAID, balance 880
```

### Сценарий 2

```bash
export USER_ID="c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a33"
# create → top-up 50 → order 120 → PAYMENT_FAILED, balance 50
```

### Сценарий 3

Проверка в БД Payments:

```sql
SELECT order_id, amount FROM processed_payments WHERE user_id = '<uuid>';
-- одна строка на каждый order_id
```

### Сценарий 4

```bash
export USER_ID="d1eebc99-9c0b-4ef8-bb6d-6bb9bd380a44"
# top-up 1000 → два POST /orders/orders по 400 параллельно → оба PAID, balance 200
```

### Сценарий 5

```bash
curl -X POST http://localhost:8080/payments/accounts -H "X-User-Id: ${USER_ID}"
curl -X POST http://localhost:8080/payments/accounts -H "X-User-Id: ${USER_ID}"
# → 409
```

---

