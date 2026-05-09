CREATE TABLE fee_rule (
rule_id UUID PRIMARY KEY,
user_id VARCHAR(100),
user_type VARCHAR(30),
transaction_type VARCHAR(30),
source_currency VARCHAR(3),
destination_currency VARCHAR(3),
effective_date TIMESTAMP NOT NULL,
active BOOLEAN NOT NULL DEFAULT TRUE,
description VARCHAR(500),
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_fee_rule_dimensions
    ON fee_rule (user_id, user_type, transaction_type, source_currency, destination_currency)
    WHERE active = TRUE;