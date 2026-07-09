# Таблица триажа ИБ (ТЗ раздел 8)

Краткая версия для вставки в отчёт. Полный разбор: [SECURITY.md](SECURITY.md)

| ID | Инструмент | Находка | Критичность | ТР/FP/риск | Решение |
|----|------------|---------|-------------|------------|---------|
| 01 | Gitleaks | Нет утечек секретов в git | — | FP | — |
| 02 | Semgrep | 0 SAST findings | — | FP | — |
| 03 | Ручной | Пароль `postgres` в compose | Средняя | ТР | `.env` в prod |
| 04 | Ручной | Нет auth, только X-User-Id | Высокая | риск | По ТЗ допустимо |
| 05 | Ручной | Подмена user_id клиентом | Высокая | ТР | JWT в prod |
| 06 | Ручной | Kafka PLAINTEXT | Средняя | риск | TLS в prod |
| 07 | Ручной | PostgreSQL на host ports | Средняя | ТР | Убрать expose |
| 08 | Ручной | Kafka :9092 на host | Низкая | риск | Только debug |
| 09 | Ручной | HTTP без TLS | Средняя | риск | HTTPS в prod |
| 10 | Ручной | ddl-auto=update | Низкая | риск | Миграции в prod |
| 11 | Ручной | Нет rate limit | Средняя | риск | Gateway limiter |
| 12 | Ручной | Нет лимита размера body | Низкая | ТР | max body size |
| 13 | Ручной | show-sql=false | — | FP | OK |
| 14 | Ручной | Docker non-root user | — | FP | OK |
| 15 | Ручной | SQL injection (JPA) | — | FP | Параметры ORM |
| 16 | Ручной | Скрытие stack trace в API | — | FP | OK |
| 17 | Ручной | Kafka consumer RuntimeException | Низкая | ТР | DLQ/monitoring |
| 18 | Ручной | Нет encryption at rest | Средняя | риск | TDE в prod |
| 19 | Ручной | Нет CORS | Низкая | риск | При web-UI |
| 20 | Ручной | Actuator не открыт | — | FP | OK |
| 21 | Ручной | Demo UUID в README | Низкая | риск | Тестовые данные |
| 22 | Ручной | processed_payments UNIQUE | — | FP | Идемпотентность |
| 23 | Ручной | Inbox по event_id | — | FP | Anti-replay |
| 24 | Ручной | Gateway без healthcheck | Низкая | риск | Опционально |
| 25 | Ручной | gradle-wrapper.jar в repo | Низкая | риск | Стандарт Gradle |

