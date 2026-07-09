-- liquibase formatted sql

-- changeset mainshop:001-orders
CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    order_id        UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    product_type    VARCHAR(255),
    price           BIGINT       NOT NULL,
    status          VARCHAR(255),
    payload         JSONB,
    failure_reason  VARCHAR(255),
    created_at      TIMESTAMPTZ  NOT NULL
);

-- changeset mainshop:002-orders-order-id-unique
ALTER TABLE orders ADD CONSTRAINT uk_orders_order_id UNIQUE (order_id);

-- changeset mainshop:003-orders-user-id-index
CREATE INDEX idx_orders_user_id_created_at ON orders (user_id, created_at DESC);

-- changeset mainshop:004-outbox
CREATE TABLE outbox (
    id           BIGSERIAL PRIMARY KEY,
    event_id     UUID         NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    payload      JSONB        NOT NULL,
    status       VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    published_at TIMESTAMPTZ
);

-- changeset mainshop:005-outbox-event-id-unique
ALTER TABLE outbox ADD CONSTRAINT uk_outbox_event_id UNIQUE (event_id);

-- changeset mainshop:006-outbox-status-index
CREATE INDEX idx_outbox_status ON outbox (status);

-- changeset mainshop:007-inbox
CREATE TABLE inbox (
    id           BIGSERIAL PRIMARY KEY,
    event_id     UUID         NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL
);

-- changeset mainshop:008-inbox-event-id-unique
ALTER TABLE inbox ADD CONSTRAINT uk_inbox_event_id UNIQUE (event_id);
