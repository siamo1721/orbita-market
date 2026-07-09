-- liquibase formatted sql

-- changeset mainshop:001-account
CREATE TABLE account (
    id       BIGSERIAL PRIMARY KEY,
    user_id  UUID   NOT NULL,
    balance  BIGINT NOT NULL,
    version  BIGINT NOT NULL DEFAULT 0
);

-- changeset mainshop:002-account-user-id-unique
ALTER TABLE account ADD CONSTRAINT uk_account_user_id UNIQUE (user_id);

-- changeset mainshop:003-processed-payments
CREATE TABLE processed_payments (
    id           BIGSERIAL PRIMARY KEY,
    order_id     UUID        NOT NULL,
    user_id      UUID        NOT NULL,
    amount       BIGINT      NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL
);

-- changeset mainshop:004-processed-payments-order-id-unique
ALTER TABLE processed_payments ADD CONSTRAINT uk_processed_payments_order_id UNIQUE (order_id);

-- changeset mainshop:005-processed-payments-user-id-index
CREATE INDEX idx_processed_payments_user_id ON processed_payments (user_id);

-- changeset mainshop:006-outbox
CREATE TABLE outbox (
    id           BIGSERIAL PRIMARY KEY,
    event_id     UUID         NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    payload      JSONB        NOT NULL,
    status       VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    published_at TIMESTAMPTZ
);

-- changeset mainshop:007-outbox-event-id-unique
ALTER TABLE outbox ADD CONSTRAINT uk_outbox_event_id UNIQUE (event_id);

-- changeset mainshop:008-outbox-status-index
CREATE INDEX idx_outbox_status ON outbox (status);

-- changeset mainshop:009-inbox
CREATE TABLE inbox (
    id           BIGSERIAL PRIMARY KEY,
    event_id     UUID         NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL
);

-- changeset mainshop:010-inbox-event-id-unique
ALTER TABLE inbox ADD CONSTRAINT uk_inbox_event_id UNIQUE (event_id);
