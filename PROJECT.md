# OrbitaMarket — план проекта

**Учебный проект**  
**Платформа:** заказ спутниковых продуктов (архивные снимки) с оплатой в геокредитах.

---

## Цель проекта

Разработать ядро платформы OrbitaMarket:

- приём заказов (Orders Service);
- биллинг в геокредитах (Payments Service);
- асинхронное согласованное списание через Kafka при нагрузке;
- единая точка входа через API Gateway.

---

## Стейкхолдеры

| Стейкхолдер | Интерес |
|-------------|---------|
| **Оператор ДЗЗ** | Заказать архивный снимок по AOI, оплатить геокредитами, видеть статус заказа |
| **Аналитик / внутренняя команда** | Пополнить счёт, оформить несколько заказов, получить PAID или понятную причину отказа |
| **Администратор платформы** | Поднять всё одной командой `docker compose up`, смотреть статистику в PostgreSQL |

---

## Архитектура (кратко)

```
Клиент → API Gateway :8080
           ├─ HTTP → Orders Service :8081 → PostgreSQL orders_db
           └─ HTTP → Payments Service :8082 → PostgreSQL payments_db

Orders ←── Kafka ──→ Payments
         (order.payment.requested / completed / failed)
```

Подробнее: `README.md`, диаграммы `docs/c4/`.

---

## Гарантии (по ТЗ п. 4.3)

| Механизм | Где |
|----------|-----|
| Transactional Outbox | Orders (requested), Payments (completed/failed) |
| Inbox по `event_id` | Orders, Payments |
| Идемпотентное списание по `order_id` | `processed_payments` |
| Optimistic locking баланса | `Account.version` (@Version) |

---

## Репозиторий

```
orbita-market/
├── api-gateway/
├── orders-service/
├── payments-service/
├── docker-compose.yml
├── PROJECT.md
├── README.md
└── docs/
    ├── analytics.sql
    ├── checklist.md
    ├── events.md
    └── c4/
```

---

## Roadmap до front-end

План развития проекта: от готового backend-ядра до веб-интерфейса для оператора ДЗЗ.


### Фаза 8 — Backend polish (перед UI)

Закрыть «дыры», без которых front-end будет неудобен или хрупок.

| # | Задача | Зачем для UI | Приоритет |
|---|--------|--------------|-----------|
| 8.1 | CORS в API Gateway | браузер с `localhost:5173` сможет ходить на `:8080` | **обязательно** |
| 8.2 | `GET /orders/orders` — пагинация или лимит | список заказов не разрастается бесконечно | желательно |
| 8.3 | Стабильный контракт ошибок (`ErrorResponse`) в README/OpenAPI | UI показывает понятные сообщения | желательно |
| 8.4 | OpenAPI/Swagger (хотя бы gateway или per-service) | генерация типов для TS, документация для диплома | желательно |
| 8.5 | Liquibase миграции вместо `ddl-auto=update` | предсказуемая схема БД при деплое | ✅ |
| 8.6 | Экспорт C4 в PNG/PDF | вставка в отчёт без PlantUML у проверяющего | для отчёта |
| 8.7 | Push autotests repo на GitHub | ТЗ п. 7.2, ссылка в README | для сдачи |

**Критерий готовности к front-end:** Gateway поднят, CORS включён, все 5 сценариев из `docs/checklist.md` проходят через `:8080`, API задокументировано.

---

### Фаза 9 — Front-end (веб-кабинет оператора)

Отдельное приложение (рекомендуется **отдельный репозиторий** `orbita-market-ui` или папка `frontend/` в monorepo).

#### 9.1. Каркас

| Задача | Детали |
|--------|--------|
| Стек | React + Vite + TypeScript (или Vue 3 — на выбор) |
| HTTP-клиент | `fetch` / axios, base URL `http://localhost:8080` |
| Состояние | React Query / TanStack Query — polling статуса заказа |
| Стили | Tailwind или MUI — быстрый учебный UI |
| Docker | `nginx` + static build, сервис в `docker-compose` за gateway или на `:3000` |

#### 9.2. Экраны (MVP)

```
┌─────────────────────────────────────────────────────────┐
│  OrbitaMarket                              [User UUID ▼] │
├─────────────────────────────────────────────────────────┤
│  Баланс: 880 геокредитов    [Пополнить]                 │
├─────────────────────────────────────────────────────────┤
│  Новый заказ (ARCHIVE)                                   │
│  AOI: [________]  Дата: [____]  Сенсор: [OPTICAL ▼]     │
│  Цена: 120                    [Оформить заказ]           │
├─────────────────────────────────────────────────────────┤
│  Мои заказы                                              │
│  id │ product │ price │ status        │ failure_reason  │
│  …  │ ARCHIVE │ 120   │ PAID          │ —               │
│  …  │ ARCHIVE │ 400   │ PAYMENT_FAILED│ INSUFFICIENT…   │
└─────────────────────────────────────────────────────────┘
```

| Экран / блок | API | Поведение |
|--------------|-----|-----------|
| Выбор пользователя | — | поле UUID → заголовок `X-User-Id` (демо без OAuth) |
| Создание счёта | `POST /payments/accounts` | при 409 — счёт уже есть, идём дальше |
| Баланс | `GET /payments/accounts/balance` | обновлять после top-up и после PAID |
| Пополнение | `POST /payments/accounts/top-up` | модалка с суммой |
| Новый заказ | `POST /orders/orders` | payload: `aoi`, `capture_date`, `sensor_type` |
| Список заказов | `GET /orders/orders` | таблица, сортировка по дате |
| Детали заказа | `GET /orders/orders/{id}` | polling каждые 1–2 с, пока `PAYMENT_PENDING` |
| Ошибки | `ErrorResponse` | toast: `INSUFFICIENT_BALANCE`, `ACCOUNT_ALREADY_EXISTS` и т.д. |

#### 9.3. UX-потоки (happy + sad path)

1. **Первый вход:** ввести UUID → создать счёт → пополнить → заказ → дождаться `PAID` → баланс уменьшился.
2. **Недостаточно средств:** заказ дороже баланса → статус `PAYMENT_FAILED`, показать `failure_reason`.
3. **Параллельные заказы:** два быстрых клика «Оформить» → оба заказа в списке, финальный баланс корректный.

#### 9.4. Интеграция с инфраструктурой

```yaml
# docker-compose.yml (концепт)
frontend:
  build: ./frontend
  ports:
    - "3000:80"
  depends_on:
    - api-gateway
```

- Front-end **не** ходит напрямую в Orders/Payments — только через Gateway.
- В dev: Vite proxy `/payments` и `/orders` → `localhost:8080`.
- CORS: Gateway разрешает origin фронта.

#### 9.5. Что сознательно не делаем в MVP UI

| Не в scope | Почему |
|------------|--------|
| Полноценный login/OAuth | по ТЗ достаточно `X-User-Id` |
| TASKING / MONITORING продукты | backend только ARCHIVE |
| WebSocket / SSE | достаточно polling `GET order by id` |
| Карта AOI | текстовое поле AOI для диплома достаточно |
| Админ-панель | analytics.sql в psql для отчёта |

---

### Фаза 10 — После front-end (опционально)

| Задача | Смысл |
|--------|-------|
| E2E (Playwright/Cypress) | UI + backend сквозные сценарии |
| Keycloak / JWT в Gateway | замена заголовка UUID на реальную auth |
| Карта (Leaflet) для AOI | визуальный выбор области |
| CI/CD | GitHub Actions: build + test + docker push |
| Мониторинг | Prometheus/Grafana или хотя бы health в compose |

---

### Временная шкала (ориентир)

```
Неделя 1–4   Фазы 0–7     backend + docs + security     ✅ сделано
Неделя 5     Фаза 8       CORS, OpenAPI, polish         🔄
Неделя 6–7   Фаза 9       front-end MVP (4–5 экранов)   ⏳
Неделя 8     Фаза 10      E2E, отчёт, защита            ⏳
```

---

### Зависимости: что блокирует front-end

```
                    ┌──────────────┐
                    │  CORS (8.1)  │◄── без этого браузер блокирует запросы
                    └──────┬───────┘
                           │
    ┌──────────────────────┼──────────────────────┐
    ▼                      ▼                      ▼
Gateway :8080        Стабильный API          docker compose up
(фаза 3 ✅)          (фазы 1–2 ✅)           (фаза 0 ✅)
                           │
                           ▼
                    ┌──────────────┐
                    │  Front-end   │
                    │  (фаза 9)    │
                    └──────────────┘
```

**Следующий шаг:** фаза 8.1 — добавить CORS filter в `api-gateway`, затем scaffold React/Vite проекта.