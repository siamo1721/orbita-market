# OrbitaMarket

Учебный проект: микросервисная платформа заказа спутниковых архивных снимков с оплатой в **геокредитах**.

## Состав

| Сервис | Порт | Назначение |
|--------|------|------------|
| API Gateway | 8080 | единая точка входа (клиент ходит сюда) |
| Orders Service | 8081 | заказы, жизненный цикл, Kafka producer/consumer |
| Payments Service | 8082 | счета, баланс, списание |
| PostgreSQL (orders) | 5433 | БД заказов |
| PostgreSQL (payments) | 5434 | БД платежей |
| Kafka | 9092 | асинхронные события оплаты |

## Требования

- Docker + Docker Compose
- Java 21 (для локальной сборки без Docker)

## Запуск

```bash
git clone <repo-url>
cd orbita-market
cp .env.example .env   # при первом запуске; пароли и порты — в .env
docker compose up --build -d
```

Переменные окружения (PostgreSQL, Kafka, порты) описаны в [`.env.example`](.env.example).  
Файл `.env` **не коммитится** в git.

Проверка:

```bash
curl http://localhost:8080/payments/accounts/balance \
  -H "X-User-Id: a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
# если счёта нет — сначала POST /payments/accounts
```

## Идентификация пользователя

По ТЗ пользователь задаётся заголовком **`X-User-Id`** в формате **UUID**.

Демо-пользователь для примеров:

```
a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11
```

Полноценной аутентификации нет — Gateway пробрасывает заголовок как есть.

## API через Gateway

Базовый URL: `http://localhost:8080`

### Payments (`/payments/...`)

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/payments/accounts` | создать счёт |
| POST | `/payments/accounts/top-up` | пополнение `{"amount": 1000}` |
| GET | `/payments/accounts/balance` | баланс |

### Orders (`/orders/...`)

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/orders/orders` | создать заказ (триггер async оплаты) |
| GET | `/orders/orders` | список заказов пользователя |
| GET | `/orders/orders/{order_id}` | заказ по id |

### Пример: happy path

```bash
export USER_ID="a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"

curl -X POST "http://localhost:8080/payments/accounts" -H "X-User-Id: ${USER_ID}"
curl -X POST "http://localhost:8080/payments/accounts/top-up" \
  -H "X-User-Id: ${USER_ID}" -H "Content-Type: application/json" -d '{"amount": 1000}'

curl -X POST "http://localhost:8080/orders/orders" \
  -H "X-User-Id: ${USER_ID}" -H "Content-Type: application/json" \
  -d '{"product_type":"ARCHIVE","price":120,"payload":{"aoi":"test","capture_date":"2026-01-01","sensor_type":"OPTICAL"}}'

sleep 3
export ORDER_ID="<order_id из ответа>"
curl "http://localhost:8080/orders/orders/${ORDER_ID}" -H "X-User-Id: ${USER_ID}"
# ожидаем status: PAID

curl "http://localhost:8080/payments/accounts/balance" -H "X-User-Id: ${USER_ID}"
# ожидаем balance: 880
```

## Асинхронная оплата

1. `POST /orders/orders` → заказ `PAYMENT_PENDING`, запись в outbox
2. Orders публикует `order.payment.requested` в Kafka
3. Payments списывает геокредиты, публикует `completed` или `failed`
4. Orders обновляет статус на `PAID` / `PAYMENT_FAILED`

Контракты событий: [docs/events.md](docs/events.md)

## Сборка без Docker

```bash
cd orders-service && ./gradlew bootJar
cd payments-service && ./gradlew bootJar
cd api-gateway && ./gradlew bootJar
```

## Документация

| Файл | Содержание |
|------|------------|
| [PROJECT.md](PROJECT.md) | цель, стейкхолдеры, roadmap |
| [docs/checklist.md](docs/checklist.md) | чек-лист сценариев 7.1 |
| [docs/events.md](docs/events.md) | Kafka-топики и JSON-события |
| [docs/analytics.sql](docs/analytics.sql) | SQL-запросы для отчёта |
| [docs/c4/](docs/c4/) | диаграммы C4 (PlantUML) |
| [docs/security/](docs/security/) | Gitleaks, Semgrep, триаж ИБ |

## Автотесты (отдельный репозиторий)

Репозиторий: `../orbita-market-autotests` (ТЗ п. 7.2 — не внутри этого repo).

```bash
docker compose up --build -d   # здесь, в orbita-market
cd ../orbita-market-autotests
./gradlew test allureReport
./gradlew allureServe
```

## Валюта

1 геокредит = 1 условная единица оплаты внутри платформы (упрощение для учебного проекта).
