-- OrbitaMarket: аналитические запросы (ТЗ п. 2.5)
-- БД: orders_db (PostgreSQL)
-- Подключение: localhost:5433, user postgres, db orders_db

-- 1. Кто и сколько потратил геокредитов (оплаченные заказы)
SELECT
    user_id,
    COUNT(*) AS paid_orders_count,
    SUM(price) AS total_spent_geocredits
FROM orders
WHERE status = 'PAID'
GROUP BY user_id
ORDER BY total_spent_geocredits DESC;

-- 2. Топ пользователей по количеству заказов (все статусы)
SELECT
    user_id,
    COUNT(*) AS orders_count,
    COUNT(*) FILTER (WHERE status = 'PAID') AS paid_count,
    COUNT(*) FILTER (WHERE status = 'PAYMENT_FAILED') AS failed_count
FROM orders
GROUP BY user_id
ORDER BY orders_count DESC;

-- 3. Статистика по типу продукта (ARCHIVE и др.)
SELECT
    product_type,
    status,
    COUNT(*) AS orders_count,
    SUM(price) AS total_price_geocredits
FROM orders
GROUP BY product_type, status
ORDER BY product_type, status;

-- 4. (доп.) Причины отказов оплаты
SELECT
    failure_reason,
    COUNT(*) AS failed_orders_count
FROM orders
WHERE status = 'PAYMENT_FAILED'
GROUP BY failure_reason;

-- 5. (Payments DB, payments_db на порту 5434) — сколько списаний обработано
SELECT user_id, COUNT(*) AS processed_count, SUM(amount) AS total_debited
FROM processed_payments
GROUP BY user_id
ORDER BY total_debited DESC;
